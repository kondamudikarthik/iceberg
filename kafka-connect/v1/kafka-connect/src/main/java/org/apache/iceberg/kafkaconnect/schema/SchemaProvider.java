package org.apache.iceberg.kafkaconnect.schema;

import org.apache.iceberg.Schema;
import org.apache.kafka.connect.sink.SinkRecord;

public interface SchemaProvider {

    Schema getKeySchema(SinkRecord sinkRecord);

    Schema getValueSchema(SinkRecord sinkRecord);
}
