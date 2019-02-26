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

import java.util.UUID;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.rest.kafka.connect.rest.HttpClientFactory;
import io.rest.kafka.connect.rest.domain.ReSTContext;
import io.rest.kafka.connect.rest.domain.Consumer;

/**
 * 
 * @author Dhananjay
 *
 */
public abstract class BackingStore {
	
	private static final Logger log = LoggerFactory.getLogger(BackingStore.class);
	
	protected final ReSTContext authContext;
	
	protected Consumer backingConsumer;
	
	protected static final ObjectMapper MAPPER = new ObjectMapper();
	
	protected final AsyncHttpClient asyncHttpClient;
	
	public BackingStore(final ReSTContext authContext) {
		this.authContext = authContext;
		this.asyncHttpClient =HttpClientFactory.getInstance();
	}
	
	
	protected void init() {
		try {
			ListenableFuture<Response> lf = this.asyncHttpClient.preparePost(this.authContext.getRestUrl()+"/consumers/"+UUID.randomUUID()+"-"+
					this.authContext.getTenantId())
					.setBody("{\"name\": \"consumer-"+UUID.randomUUID()+"-"+this.authContext.getTenantId()+"\", \"format\": \"json\","
							+ " \"auto.offset.reset\": \"earliest\", "
							+ " \"auto.commit.enable\":\"false\"}")
					.setHeader("Content-Type", "application/vnd.kafka.v2+json")
					.setHeader("Auth", this.authContext.getCredinfo())
		 			.setHeader("tenant_id", this.authContext.getTenantId())
					.execute();
			
			//Hold until response is received...
			Response response = lf.get();
			if(response.getStatusCode() == HttpResponseStatus.OK.code()) {
				String responseBody = response.getResponseBody();
				this.backingConsumer = MAPPER.readValue(responseBody, Consumer.class);
				//Subscribe to connect offset topic for the given id
				this.asyncHttpClient.preparePost(this.backingConsumer.getBaseUri()+"/subscription")
				.setBody("{\"topics\":[\""+ this.getBackingTopicName()+"\"]}")
				.setHeader("Content-Type","application/vnd.kafka.v2+json")
				.execute(new AsyncCompletionHandler<String>() {
					@Override
					public String onCompleted(Response response) throws Exception {
						//String str = response.getResponseBody(); 
						if(response.getStatusCode() == HttpResponseStatus.NO_CONTENT.code()) {
							return HttpResponseStatus.NO_CONTENT.toString();
						}else {
							return HttpResponseStatus.BAD_REQUEST.toString();
						}
					}
				});
			}else {
				log.debug(" create consumer  Failed with "+response.getStatusCode());
				log.debug("create consumer failure body "+response.getResponseBody());
			}
		} catch (Exception e) {
			log.warn("Exception while querying backing store for "+this.getBackingTopicName(),e);
		} 
	}
	
	protected abstract String getBackingTopicName();
	
	
	protected void sleep(int attempt) {
		try {
			Thread.sleep(5000*attempt);
		} catch (InterruptedException e) {
			log.warn("Exception while waiting to invoke backing store method on attempt "+attempt ,e);
		}
	}
	
	protected ListenableFuture<Response> executeConsumer(){
		return this.asyncHttpClient.prepareGet(this.backingConsumer.getBaseUri()+"/records")
				.setHeader("Accept", "application/vnd.kafka.json.v2+json")
				.setHeader("Auth", this.authContext.getCredinfo())
	 			.setHeader("member_id", this.authContext.getTenantId())
				.execute();
	}
	
	protected void deleteConsumer() {
		try {
			this.asyncHttpClient.prepareDelete(this.backingConsumer.getBaseUri())
					.setHeader("Content-Type", "application/vnd.kafka.v2+json")
					.setHeader("Auth", this.authContext.getCredinfo())
		 			.setHeader("member_id", this.authContext.getTenantId())
					.execute();
		} catch (Exception e) {
			log.warn("Exception while delete consumer for ",e);
		}
	}
	
}
