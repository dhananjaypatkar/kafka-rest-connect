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

package io.rest.kafka.connect.rest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Dhananjay
 *
 */
public class ConnecctStatus {

	@JsonProperty("key")
	private StatusKey key;
	
	@JsonProperty("value")
	private StatusValue value;

	@JsonProperty("key")
	public StatusKey getKey() {
		return key;
	}

	@JsonProperty("key")
	public void setKey(StatusKey key) {
		this.key = key;
	}

	@JsonProperty("value")
	public StatusValue getValue() {
		return value;
	}

	@JsonProperty("value")
	public void setValue(StatusValue value) {
		this.value = value;
	}
	
}
