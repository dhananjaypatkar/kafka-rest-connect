/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rest.kafka.connect.worker;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.rest.kafka.connect.connector.Task;
import io.rest.kafka.connect.rest.KafkaRestJsonProducer;
import io.rest.kafka.connect.rest.domain.ConnectOffset;
import io.rest.kafka.connect.rest.domain.ReSTContext;
import io.rest.kafka.connect.storage.OffsetBackingStore;

/**
 * This is the Connect worker task class responsible for
 *  1. Invoking data pull from the source task instance
 *     Example : In case of JdbcSourceTask, this worker task will invoke task.poll() method
 *     to get the data for the give polling interval
 *  2. Once data is received by the worker task, it will prepare it to be sent via kafka ReST
 *  3. This class uses, {@link KafkaRestJsonProducer} to publish json messages to the kafka ReST end point  
 *  4. Once the messages [batch of messages defined by batch.size parameter on connector configuration] are published
 *     this will extract time stamp / incrementing value from the last json message produced and store it as a offset.
 *  5. Offset is stored at 2 places, 1 copy is in runtime memory to reduce a ReST call.   
 *   
 * 
 * @author Dhananjay
 *
 */
public class WorkerTask implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(WorkerTask.class);

	private Task connectTask;

	private KafkaRestJsonProducer kafkaRestJsonProducer;

	private OffsetBackingStore offsetBackingStore;

	private ObjectMapper MAPPER = new ObjectMapper();
	
	private final ReSTContext restContext;

	public WorkerTask(final Task connectTask, final OffsetBackingStore offsetBackingStore, final ReSTContext restContext) {
		this.connectTask = connectTask;
		this.restContext = restContext;
		this.kafkaRestJsonProducer = new KafkaRestJsonProducer(this.restContext);
		this.offsetBackingStore = offsetBackingStore;
	}

	@Override
	public void run() {
		try {
			while (true) {
				log.debug("Before invoking poll on the task..");
				List<JsonNode> records = this.connectTask.poll();
				log.debug("After invoking poll on the task..");
				ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
				JsonNode lastNode = null;
				for (JsonNode jsonNode : records) {
					lastNode = jsonNode;
					arrayNode.add(jsonNode.get("message"));
				}
				if (lastNode != null) {
					String topic = lastNode.get("topic").asText();
					HttpResponseStatus response = null;
					
					do {
						response = this.kafkaRestJsonProducer.sendRecords(topic, arrayNode);
					} while (((response != null) && (response != HttpResponseStatus.OK)));

					// Once the records are published , then commit the offset for this batch
					JsonNode currentOffset = lastNode.get("offset");
					if (currentOffset != null) {
						ArrayNode offsetArray = JsonNodeFactory.instance.arrayNode(1);
						offsetArray.add(currentOffset);
						System.out.println("Offset to be commited for this batch " + currentOffset.toString());
						do {
							//write offset to the offset topic
							response = this.kafkaRestJsonProducer.sendRecords(offsetBackingStore.getBackingTopicName(), offsetArray);
						} while (((response != null) && (response != HttpResponseStatus.OK)));
						
						//Update the in-memory copy of the offset map
						offsetBackingStore.writeOffset(MAPPER.readValue(currentOffset.toString(), ConnectOffset.class));
					}
				} else {
					throw new RuntimeException("ReST must take Rest......");
				}
			}
		} catch (Throwable e) {
			log.error("Task {} threw an uncaught and unrecoverable exception", this.connectTask, e);
            log.error("Task is being killed and will not recover until manually restarted");
			this.notifyAll();
			if (e instanceof Error)
				throw (Error) e;
			log.error("Exception while executing workertask ", e);
		}
	}

}
