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

package io.rest.kafka.connect.rest;

import java.util.concurrent.ExecutionException;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.rest.kafka.connect.rest.domain.ReSTContext;

/**
 * 
 * @author Dhananjay
 *
 *
 */
public class KafkaRestJsonProducer {
	
	private static final Logger log = LoggerFactory.getLogger(KafkaRestJsonProducer.class);

	private final ReSTContext authContext;

	private final ObjectMapper MAPPER = new ObjectMapper();
	
	private final AsyncHttpClient asyncHttpClient ;

	public KafkaRestJsonProducer(final ReSTContext authContext) {
		this.authContext = authContext;
		this.asyncHttpClient = HttpClientFactory.getInstance();
	}

	private BoundRequestBuilder createProducer(final String topic) {
		 return asyncHttpClient.preparePost(this.authContext.getRestUrl() + "/topics/" + topic)
					.setHeader("Accept", "application/vnd.kafka.v2+json")
					.setHeader("Content-Type", "application/vnd.kafka.json.v2+json")
		 			.setHeader("auth", this.authContext.getCredinfo())
		 			.setHeader("tenant_id", this.authContext.getTenantId());
	
	}
	
	public HttpResponseStatus sendRecords(final String topic,final ArrayNode records) {
		//TODO add javarx retry
		try {
			if(records != null && (records.size() != 0)) {
			// Wrap it in records
			ObjectNode recordNodes = JsonNodeFactory.instance.objectNode();
			recordNodes.set("records", records);
			String data = MAPPER.writeValueAsString(recordNodes);
			log.debug("in send records " + records.size());
			ListenableFuture<HttpResponseStatus> responseStatus = createProducer(topic).setBody(data).
					execute(new AsyncCompletionHandler<HttpResponseStatus>() {
				@Override
				public HttpResponseStatus onCompleted(Response response) throws Exception {
					
					if (response.getStatusCode() == HttpResponseStatus.OK.code()) {
						log.debug("Producer is in success ");
					} else {
						log.error(" Produce Records Failed with " + response.getStatusCode());
						//log.error("Produce response failure body " + response.getResponseBody());
					}
					return HttpResponseStatus.valueOf(response.getStatusCode());
				}

				@Override
				public State onHeadersReceived(HttpHeaders headers) throws Exception {
					log.debug("Headers records " + headers);
					return super.onHeadersReceived(headers);
				}
			});
			return responseStatus.get();
			}else {
				log.debug("No records to be sent..empty arraynode is passed ");
			}
			
		} catch (JsonProcessingException | InterruptedException | ExecutionException e) {
			log.error("Exception while sending records to the topic ",e);
		}
		return HttpResponseStatus.SERVICE_UNAVAILABLE;
	}
}