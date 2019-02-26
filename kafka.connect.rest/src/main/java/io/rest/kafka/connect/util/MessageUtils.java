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

package io.rest.kafka.connect.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.rest.kafka.connect.rest.SimpleJsonConvertor;
import io.rest.kafka.connect.rest.domain.ConnectOffset;

/**
 * 
 * @author Dhananjay
 *
 */
public class MessageUtils {

	private static ObjectMapper MAPPER = new ObjectMapper();
	
	private static final SimpleJsonConvertor keyConverter;
	private static final SimpleJsonConvertor valueConverter;
	static {
		keyConverter = new SimpleJsonConvertor();
		valueConverter = new SimpleJsonConvertor();
		Map<String, Object> converterMap = new HashMap<>();
		converterMap.put("key.converter.schemas.enable", false);
		converterMap.put("value.converter.schemas.enable", false);
		keyConverter.configure(converterMap, true);
		valueConverter.configure(converterMap, true);
	}
	
	public static JsonNode convertValueToJson(final Schema schema,final Struct payload
			,final Schema keySchema, final Struct key,final ConnectOffset connectOffset,
			final String topic) throws IOException {
		JsonNode jn = valueConverter.toJsonNode( schema, payload);
		JsonNode jnk = keyConverter.toJsonNode( keySchema, key);
		ObjectNode envelope = JsonNodeFactory.instance.objectNode();
	    envelope.set("key", jnk);
	    envelope.set("value", jn);
	    ObjectNode parentEnvelope = JsonNodeFactory.instance.objectNode();
	    parentEnvelope.set("message", envelope);
	    parentEnvelope.set("topic", JsonNodeFactory.instance.textNode(topic));
	    if(connectOffset != null) {
	   	    //parentEnvelope.set("offset",MAPPER.readTree(MAPPER.writeValueAsString(connectOffset)));
	    	/*ObjectNode offsetEnvelope = JsonNodeFactory.instance.objectNode();
	    	offsetEnvelope.set("key", MAPPER.readTree(MAPPER.writeValueAsString(connectOffset.getKey())));
	    	offsetEnvelope.set("value", MAPPER.readTree(MAPPER.writeValueAsString(connectOffset.getValue())));*/
	    	parentEnvelope.set("offset", MAPPER.readTree(MAPPER.writeValueAsString(connectOffset)));
	    }
	    return parentEnvelope;
	}
	
	
	public static Schema getKeySchema(String keyField) {
		SchemaBuilder builder = SchemaBuilder.struct().name("key");
		builder.field(keyField, Schema.OPTIONAL_STRING_SCHEMA);
		return builder.build();
	}

	public static Struct getKey(String keyField,String memberid, Schema schema) {
		Struct struct = new Struct(schema);
		struct.put(keyField, memberid);
		return struct;
	}

	
	public static List<String> convertCsvToList(final String csv){
		String[] elements = csv.split(",");
		return Arrays.asList(elements);
	}
	
	public static String convertListToCsv(final List<String> elements) {
	    StringBuilder result = new StringBuilder();
	    boolean first = true;
	    for (String elem : elements) {
	      if (first) {
	        first = false;
	      } else {
	        result.append(",");
	      }
	      result.append(elem);
	    }
	    return result.toString();
	}
	
	
	public static String getOffsetKey(final String table,final String connector) {
		return connector+"-"+table;
	}
	
	/*public static void main(String[] args) {
		System.out.println(getOffsetKey("GOAL_PROBLEM", "CPS-WITH-DB_UPDATED_DATE-50001-DocumentRelated-1-INCR"));
	}*/
}
