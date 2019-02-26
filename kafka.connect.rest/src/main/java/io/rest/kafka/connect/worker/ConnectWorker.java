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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.rest.kafka.connect.connector.Connector;
import io.rest.kafka.connect.connector.Task;
import io.rest.kafka.connect.rest.domain.ConnectConfig;
import io.rest.kafka.connect.rest.domain.ReSTContext;
import io.rest.kafka.connect.storage.OffsetBackingStore;
import io.rest.kafka.connect.util.ConnectorFactory;

/**
 * This is connect worker class responsible for 
 *  1. Instantiating connector 
 *  2. Identifying connector instances to run based on number of tasks defined on the connector 
 *  3. Instantiate the source tasks for the connector
 *  4. Create Connect worker and associate source task to the worker.
 *  5. Submit the worker task created to the executor.
 *  
 *  Note : There must be only single instance of the Connect worker per runtime.
 * 
 * 
 * @author Dhananjay
 *
 */
public class ConnectWorker {

	private static final Logger log = LoggerFactory.getLogger(ConnectWorker.class);
	
	private final ExecutorService executor;
	
	private final ConnectorFactory connectorFactory;
	
	private final OffsetBackingStore offsetBackingStore;
	
	//private final StatusBackingStore statusBackingStore;
	
	//private final String restURL;
	
	private final ReSTContext authContext;
	
	public ConnectWorker(final OffsetBackingStore offsetBackingStore,final ReSTContext restContext) {
		connectorFactory = new ConnectorFactory();
		this.executor = Executors.newCachedThreadPool();
		this.offsetBackingStore = offsetBackingStore;
		this.authContext = restContext;
	}
	
	
	/**
	 * @param connectorConfigs
	 * 
	 */
	public void startConnect(Collection<ConnectConfig> connectorConfigs) {
		if(!connectorConfigs.isEmpty()) {
			for (ConnectConfig connectConfig : connectorConfigs) {
				//TODO Check with statusBackingStore for the status of this connector...
				Connector connector = connectorFactory.newConnector(connectConfig.getConnectorClass());
				log.debug("Instantiated connector "+connector +" for config "+connectConfig.getName());
				connector.start(connectConfig);
				List<ConnectConfig> taskSettings = connector.taskConfigs();
				log.debug("Tasks to be instantiated for this connector "+connectConfig.getName() +" counts "+taskSettings.size());
				for (ConnectConfig taskSetting : taskSettings) {
					Task connectTask = connectorFactory.newTask(connector.taskClass());
					log.debug("Instantiated task "+connectTask + " with task settings as "+taskSetting);
					connectTask.setOffsetBackingStore(offsetBackingStore);
					connectTask.start(taskSetting);
					log.debug("After starting the connector "+taskSetting.getName());
					WorkerTask workerTask = new WorkerTask(connectTask,this.offsetBackingStore,this.authContext);
					this.executor.submit(workerTask);
				}
			}
		}else {
			log.warn("In connect worker did not find any configurations...");
		}
	}
	
}
