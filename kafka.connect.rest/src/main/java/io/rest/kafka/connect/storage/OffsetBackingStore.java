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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.rest.kafka.connect.rest.domain.ReSTContext;
import io.rest.kafka.connect.rest.domain.ConnectOffset;
import io.rest.kafka.connect.rest.domain.OffsetKey;
import io.rest.kafka.connect.rest.domain.OffsetValue;

/**
 * 
 * @author Dhananjay
 *
 */
public class OffsetBackingStore extends BackingStore {

	private static final Logger log = LoggerFactory.getLogger(OffsetBackingStore.class);
	
	private final Map<String,ConnectOffset> offsetMap = new ConcurrentHashMap<>();
	
	
	public OffsetBackingStore(final ReSTContext authContext) {
		super(authContext);
	}
	
	@Override
	public String getBackingTopicName() {
		return "connect-offsets"+"-"+this.authContext.getTenantId();
	}
	
	
	
	private void loadConnectOffsets(final int attempt) {
		ListenableFuture<Response> offsetRecordsResponse = executeConsumer();
		try {
			Response response = offsetRecordsResponse.get();
			if(response.getStatusCode() == HttpResponseStatus.OK.code() && !"[]".equals(response.getResponseBody())) {
				log.debug(response.getResponseBody());
				JsonNode offsetJson = MAPPER.readTree(response.getResponseBody());
				if(offsetJson.isArray()) {
					ArrayNode offsetArray = (ArrayNode)offsetJson;
					Iterator<JsonNode> itrOffset = offsetArray.iterator();
					while(itrOffset.hasNext()) {
						JsonNode offsetNode = itrOffset.next();
						OffsetKey key = MAPPER.readValue(offsetNode.get("key").toString(), OffsetKey.class);//offsetNode.get("key").get("name").asText();
						OffsetValue offsetValue = MAPPER.readValue(offsetNode.get("value").toString(), OffsetValue.class);
						ConnectOffset connectOffset = new ConnectOffset();
						connectOffset.setKey(key); 
						connectOffset.setValue(offsetValue);
						this.offsetMap.put(key.getName(),connectOffset);
					}
				}
			}else {
				log.warn(" Consume Records Failed with "+response.getStatusCode());
				log.warn("Consume response failure body "+response.getResponseBody());
			}
			
		} catch (Exception e) {
			log.error("Exception while loading offset configuration ",e);
			this.sleep(attempt);
		}
	}
	
	/**
	 * @param table
	 * @return
	 */
	public ConnectOffset readOffset(String table) {
		this.init();
		int attempt = 1;
		while(this.offsetMap.isEmpty()) {
			this.loadConnectOffsets(attempt);
			attempt++;
		}
		this.deleteConsumer();
		return this.offsetMap.get(table);
	}
	
	/**
	 * @param table
	 */
	public void writeOffset(ConnectOffset connectOffset) {
		log.debug("Before writting offset in offsetbacking store "+connectOffset);
		this.offsetMap.put(connectOffset.getKey().getName(), connectOffset);
		log.debug("after writting offset in offsetbacking store "+connectOffset);
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String,ConnectOffset> offsetAsMap(){
		init();
		int attempt = 1;
		while(this.offsetMap.isEmpty()) {
			this.loadConnectOffsets(attempt);
			attempt++;
		}
		return this.offsetMap; 
	}
	
}
