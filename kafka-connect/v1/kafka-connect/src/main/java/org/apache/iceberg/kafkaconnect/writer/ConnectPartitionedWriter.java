package org.apache.iceberg.kafkaconnect.writer;

import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.PartitionedWriter;

public class ConnectPartitionedWriter extends PartitionedWriter<Record> {
    private final PartitionKey partitionKey;
    InternalRecordWrapper wrapper;

    public ConnectPartitionedWriter(PartitionSpec spec, FileFormat format,
                                    GenericAppenderFactory appenderFactory,
                                    OutputFileFactory outputFileFactory, FileIO io,
                                    long targetFileSizeBytes, Schema schema) {
        super(spec, format, appenderFactory, outputFileFactory, io, targetFileSizeBytes);
        this.partitionKey = new PartitionKey(spec, schema);
        this.wrapper = new InternalRecordWrapper(schema.asStruct());
    }


    @Override
    protected PartitionKey partition(Record row) {
        partitionKey.partition(wrapper.wrap(row));
        return partitionKey;
    }
}
