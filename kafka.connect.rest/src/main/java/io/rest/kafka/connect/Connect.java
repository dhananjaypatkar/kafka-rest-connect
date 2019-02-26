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

package io.rest.kafka.connect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.rest.kafka.connect.rest.domain.ConnectConfig;
import io.rest.kafka.connect.rest.domain.ConnectOffset;
import io.rest.kafka.connect.rest.domain.ReSTConfigs;
import io.rest.kafka.connect.rest.domain.ReSTContext;
import io.rest.kafka.connect.storage.ConfigBackingStore;
import io.rest.kafka.connect.storage.OffsetBackingStore;
import io.rest.kafka.connect.worker.ConnectWorker;

/**
 * Bootstrap class for Kafka ReST connect..
 * 
 * @author Dhananjay
 *
 */
public class Connect {
	
	private static final Logger log = LoggerFactory.getLogger(Connect.class);

	public static void main(String[] args) {
		try {
		String configProp = args[0];
		Properties configs = new Properties();	
		configs.load(new FileInputStream(new File(configProp)));
		
		String memberId = configs.getProperty(ReSTConfigs.TENANT_ID.name()); //"20190101";//args[0];
		String restUrl = configs.getProperty(ReSTConfigs.REST_URL.name());//"https://cqrintwebapp.cqrintase.p.azurewebsites.net";//args[1];
		String user = configs.getProperty(ReSTConfigs.USER_ID.name());
		String credentials = configs.getProperty(ReSTConfigs.CREDENTIALS.name());
		String base64 = DatatypeConverter.printBase64Binary((user+":"+credentials).getBytes());
		ReSTContext authContext = new ReSTContext(base64,memberId,restUrl);

		ConfigBackingStore configBackingStore = new ConfigBackingStore(authContext);
		log.debug("Initialized configuration backing store for member "+memberId);
		OffsetBackingStore offsetBackingStore = new OffsetBackingStore(authContext);
		log.debug("Initialized offset backing store for member "+memberId);
		Collection<ConnectConfig> connectConfigs = configBackingStore.readAll();
		log.debug("got all connect configurations for member "+memberId);
		log.debug("Before calling offset backing store load all");
		Map<String, ConnectOffset> offMap = offsetBackingStore.offsetAsMap();
		log.debug("aflter calling offset backing store load all"+offMap);
		ConnectWorker connectWorker = new ConnectWorker(offsetBackingStore, authContext);
		log.debug("Initialized connect worker for "+memberId+" with "+restUrl);
		connectWorker.startConnect(connectConfigs);
		log.debug("Successfully started the connect worker ...for member "+memberId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
