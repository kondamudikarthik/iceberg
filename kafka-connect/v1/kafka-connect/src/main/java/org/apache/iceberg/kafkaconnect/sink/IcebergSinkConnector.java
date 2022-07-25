/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.apache.iceberg.kafkaconnect.sink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcebergSinkConnector extends SinkConnector {
    private static final Logger LOG = LoggerFactory.getLogger(IcebergSinkTask.class);
    private Map<String, String> configProps;
    private IcebergSinkConnectorConfig config;

    public IcebergSinkConnector() {
    }

    IcebergSinkConnector(IcebergSinkConnectorConfig config) {
        this.config = config;
    }

    public void start(Map<String, String> props) {
        this.configProps = new HashMap(props);
        this.config = new IcebergSinkConnectorConfig(props);
        LOG.info("Starting Iceberg Sink connector");
    }

    public Class<? extends Task> taskClass() {
        return IcebergSinkTask.class;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
        Map<String, String> taskProps = new HashMap(this.configProps);
        List<Map<String, String>> taskConfigs = new ArrayList(maxTasks);

        for(int i = 0; i < maxTasks; ++i) {
            taskConfigs.add(taskProps);
        }

        return taskConfigs;
    }

    public void stop() {
        LOG.info("Stopping the Iceberg Sink Connector {}", this.version());
    }

    public ConfigDef config() {
        return IcebergSinkConnectorConfig.getConfig();
    }

    public String version() {
        return "1.1.1";
    }
}
