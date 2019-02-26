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

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "connector.class", "tasks.max", "table.whitelist", "connection.url", "mode", "cqr.member.id",
		"validate.non.null", "timestamp.column.name", "topic.prefix", "batch.max.rows", "poll.interval.ms","incrementing.column.name",
		"bootstrap.server", "numeric.precision.mapping", "is.ondemand","table.types","schema.pattern","query" })
/**
 * 
 * @author 212473687
 *
 */
public class ConnectConfig {

	@JsonProperty("connector.class")
	private String connectorClass;
	@JsonProperty("tasks.max")
	private String tasksMax;
	@JsonProperty("table.whitelist")
	private String tableWhitelist;
	@JsonProperty("connection.url")
	private String connectionUrl;
	@JsonProperty("mode")
	private String mode;
	@JsonProperty("cqr.member.id")
	private String cqrMemberId;
	@JsonProperty("validate.non.null")
	private String validateNonNull;
	@JsonProperty("timestamp.column.name")
	private String timestampColumnName;
	@JsonProperty("topic.prefix")
	private String topicPrefix;
	@JsonProperty("batch.max.rows")
	private String batchMaxRows;
	@JsonProperty("poll.interval.ms")
	private String pollIntervalMs;
	@JsonProperty("bootstrap.server")
	private String bootstrapServer;
	@JsonProperty("numeric.precision.mapping")
	private String numericPrecisionMapping;
	@JsonProperty("is.ondemand")
	private String isOndemand;
	@JsonProperty("table.types")
	private String tableType;
	@JsonProperty("schema.pattern")
	private String schemaPattern;
	@JsonProperty("incrementing.column.name")
	private String incrementingColumnName;
	@JsonProperty("query")
	private String query;
	
	private String name;
	
	@JsonProperty("connector.class")
	public String getConnectorClass() {
		return connectorClass;
	}

	@JsonProperty("connector.class")
	public void setConnectorClass(String connectorClass) {
		this.connectorClass = connectorClass;
	}

	@JsonProperty("tasks.max")
	public String getTasksMax() {
		return tasksMax;
	}

	@JsonProperty("tasks.max")
	public void setTasksMax(String tasksMax) {
		this.tasksMax = tasksMax;
	}

	@JsonProperty("table.whitelist")
	public String getTableWhitelist() {
		return tableWhitelist;
	}

	@JsonProperty("table.whitelist")
	public void setTableWhitelist(String tableWhitelist) {
		this.tableWhitelist = tableWhitelist;
	}

	@JsonProperty("connection.url")
	public String getConnectionUrl() {
		return connectionUrl;
	}

	@JsonProperty("connection.url")
	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	@JsonProperty("mode")
	public String getMode() {
		return mode;
	}

	@JsonProperty("mode")
	public void setMode(String mode) {
		this.mode = mode;
	}

	@JsonProperty("cqr.member.id")
	public String getCqrMemberId() {
		return cqrMemberId;
	}

	@JsonProperty("cqr.member.id")
	public void setCqrMemberId(String cqrMemberId) {
		this.cqrMemberId = cqrMemberId;
	}

	@JsonProperty("validate.non.null")
	public String getValidateNonNull() {
		return validateNonNull;
	}

	@JsonProperty("validate.non.null")
	public void setValidateNonNull(String validateNonNull) {
		this.validateNonNull = validateNonNull;
	}

	@JsonProperty("timestamp.column.name")
	public String getTimestampColumnName() {
		return timestampColumnName;
	}

	@JsonProperty("timestamp.column.name")
	public void setTimestampColumnName(String timestampColumnName) {
		this.timestampColumnName = timestampColumnName;
	}

	@JsonProperty("topic.prefix")
	public String getTopicPrefix() {
		return topicPrefix;
	}

	@JsonProperty("topic.prefix")
	public void setTopicPrefix(String topicPrefix) {
		this.topicPrefix = topicPrefix;
	}

	@JsonProperty("batch.max.rows")
	public String getBatchMaxRows() {
		return batchMaxRows;
	}

	@JsonProperty("batch.max.rows")
	public void setBatchMaxRows(String batchMaxRows) {
		this.batchMaxRows = batchMaxRows;
	}

	@JsonProperty("poll.interval.ms")
	public String getPollIntervalMs() {
		return pollIntervalMs;
	}

	@JsonProperty("poll.interval.ms")
	public void setPollIntervalMs(String pollIntervalMs) {
		this.pollIntervalMs = pollIntervalMs;
	}

	@JsonProperty("bootstrap.server")
	public String getBootstrapServer() {
		return bootstrapServer;
	}

	@JsonProperty("bootstrap.server")
	public void setBootstrapServer(String bootstrapServer) {
		this.bootstrapServer = bootstrapServer;
	}

	@JsonProperty("numeric.precision.mapping")
	public String getNumericPrecisionMapping() {
		return numericPrecisionMapping;
	}

	@JsonProperty("numeric.precision.mapping")
	public void setNumericPrecisionMapping(String numericPrecisionMapping) {
		this.numericPrecisionMapping = numericPrecisionMapping;
	}

	@JsonProperty("is.ondemand")
	public String getIsOndemand() {
		return isOndemand;
	}

	@JsonProperty("is.ondemand")
	public void setIsOndemand(String isOndemand) {
		this.isOndemand = isOndemand;
	}
	@JsonProperty("table.types")
	public String getTableType() {
		return tableType;
	}
	@JsonProperty("table.types")
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	@JsonProperty("schema.pattern")
	public String getSchemaPattern() {
		return schemaPattern;
	}

	@JsonProperty("schema.pattern")
	public void setSchemaPattern(String schemaPattern) {
		this.schemaPattern = schemaPattern;
	}

	public ConnectConfig copy() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(mapper.writeValueAsString(this), ConnectConfig.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@JsonProperty("incrementing.column.name")
	public String getIncrementingColumnName() {
		return incrementingColumnName;
	}

	@JsonProperty("incrementing.column.name")
	public void setIncrementingColumnName(String incrementingColumnName) {
		this.incrementingColumnName = incrementingColumnName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ConnectConfig [connectorClass=" + connectorClass + ", tasksMax=" + tasksMax + ", tableWhitelist="
				+ tableWhitelist + ", connectionUrl=" + connectionUrl + ", cqrMemberId=" + cqrMemberId
				+ ", timestampColumnName=" + timestampColumnName + ", topicPrefix=" + topicPrefix + ", batchMaxRows="
				+ batchMaxRows + ", pollIntervalMs=" + pollIntervalMs + "]";
	}

	@JsonProperty("query")
	public String getQuery() {
		return query;
	}

	@JsonProperty("query")
	public void setQuery(String query) {
		this.query = query;
	}
	
}