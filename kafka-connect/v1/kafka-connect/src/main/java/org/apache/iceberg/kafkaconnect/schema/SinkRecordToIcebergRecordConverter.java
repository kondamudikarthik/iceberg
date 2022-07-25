package org.apache.iceberg.kafkaconnect.schema;

import org.apache.iceberg.data.GenericRecord;
import org.apache.kafka.connect.sink.SinkRecord;

import java.io.IOException;

public interface SinkRecordToIcebergRecordConverter {

    GenericRecord convert(SinkRecord record) throws IOException;
}
