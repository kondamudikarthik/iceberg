package org.apache.iceberg.kafkaconnect.schema;

import com.stitchfix.datahighway.sficebergconnector.IcebergSinkConnectorConfig;
import io.confluent.connect.avro.AvroData;
import org.apache.avro.Schema;
import org.apache.kafka.connect.sink.SinkRecord;

public class AvroSchemaProvider  {

    private final IcebergSinkConnectorConfig connectorConfig;
    private final AvroData avroData;

    public AvroSchemaProvider(IcebergSinkConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
        this.avroData = new AvroData(this.connectorConfig.avroDataConfig());

    }

    public Schema getKeySchema(SinkRecord sinkRecord) {
        return this.avroData.fromConnectSchema(sinkRecord.keySchema());

    }

    public Schema getValueSchema(SinkRecord sinkRecord) {
        return this.avroData.fromConnectSchema(sinkRecord.valueSchema());
    }
}

