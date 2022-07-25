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

import com.stitchfix.datahighway.sficebergconnector.iceberg.IcebergCatalogFactory;
import com.stitchfix.datahighway.sficebergconnector.iceberg.IcebergTopicPartitionWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.iceberg.catalog.Catalog;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcebergSinkTask extends SinkTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(IcebergSinkTask.class);
    private String tableName;
    private String nameSpace;
    private String icebergCatalogImplClass;
    private Catalog catalog;
    private Map<TopicPartition, IcebergTopicPartitionWriter> topicPartitionWriters = new HashMap();

    public IcebergSinkTask() {
    }

    public String version() {
        return null;
    }

    public void start(Map<String, String> props) {
        this.tableName = (String)props.get("iceberg.table");
        this.nameSpace = (String)props.get("iceberg.namespace");
        this.icebergCatalogImplClass = (String)props.get("iceberg.catalog.impl.class");
        this.catalog = IcebergCatalogFactory.catalog(props);
    }

    public void open(Collection<TopicPartition> partitions) {
        LOGGER.info("Got partitions to process {}", partitions);
        Iterator var2 = partitions.iterator();

        while(var2.hasNext()) {
            TopicPartition tp = (TopicPartition)var2.next();
            this.topicPartitionWriters.put(tp, new IcebergTopicPartitionWriter(this.nameSpace, this.tableName, this.catalog));
        }

    }

    public void put(Collection<SinkRecord> records) {
        Iterator var2 = records.iterator();

        while(var2.hasNext()) {
            SinkRecord sinkRecord = (SinkRecord)var2.next();
            String topic = sinkRecord.topic();
            int partition = sinkRecord.kafkaPartition();
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            if (sinkRecord.value() != null) {
                ((IcebergTopicPartitionWriter)this.topicPartitionWriters.get(topicPartition)).add(sinkRecord);
            }
        }

        var2 = this.topicPartitionWriters.entrySet().iterator();

        while(var2.hasNext()) {
            Entry<TopicPartition, IcebergTopicPartitionWriter> tpEntry = (Entry)var2.next();
            IcebergTopicPartitionWriter icebergTopicPartitionWriter = (IcebergTopicPartitionWriter)tpEntry.getValue();
            icebergTopicPartitionWriter.write();
        }

    }

    public void stop() {
    }
}
