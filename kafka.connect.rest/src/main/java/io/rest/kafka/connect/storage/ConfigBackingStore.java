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

package io.rest.kafka.connect.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.rest.kafka.connect.rest.domain.ReSTContext;
import io.rest.kafka.connect.rest.domain.ConnectConfig;

/**
 * This is the Configuration store of the connector configuration
 * This gets loaded at the start up.
 * 
 * Connect configuration is stored in {@link ConnectConfig}
 * 
 * @author Dhananjay
 *
 */
public class ConfigBackingStore extends BackingStore {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigBackingStore.class);

	private final Map<String, ConnectConfig> configMap = new ConcurrentHashMap<>();
	
	public ConfigBackingStore(final ReSTContext authContext) {
		super(authContext);	
	}

	@Override
	protected String getBackingTopicName() {
		return "connect-configs"+"-"+this.authContext.getTenantId();
	}

	public Collection<ConnectConfig> readAll(){
		this.init();
		int attempt = 1;
		while(this.configMap.isEmpty()) {
			loadConfigs(attempt);
			attempt++;
		}
		//Delete the consumer and free up resources on the ReST Proxy
		this.deleteConsumer();
		return this.configMap.values();
	}

	private void loadConfigs(final int attempt) {
		ListenableFuture<Response> records = executeConsumer();
		Response response;
		try {
			response = records.get();
			if(response.getStatusCode() == HttpResponseStatus.OK.code() && 
					!"[]".equals(response.getResponseBody())) {
				
				JsonNode offsetJson = MAPPER.readTree(response.getResponseBody());
				
				if(offsetJson.isArray()) {
					ArrayNode offsetArray = (ArrayNode)offsetJson;
					Iterator<JsonNode> itrOffset = offsetArray.iterator();
					while(itrOffset.hasNext()) {
						JsonNode configNode = itrOffset.next();
						String key = configNode.get("key").get("name").asText();
						ConnectConfig config = MAPPER.readValue(configNode.get("value").toString(),
								ConnectConfig.class);
						config.setName(key);
						this.configMap.put(key, config);
					}
				}
			}else {
				log.warn(" Consume Records Failed with "+response.getStatusCode());
				log.warn("Consume response failure body "+response.getResponseBody());
			}
			
		} catch (InterruptedException | ExecutionException | IOException e) {
			log.error("Exception while loading connect configuration ",e);
			sleep(attempt);
		}
	}
	
}