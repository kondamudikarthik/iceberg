package org.apache.iceberg.kafkaconnect.writer;

import com.google.common.primitives.Ints;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.UnpartitionedWriter;
import org.apache.kafka.connect.data.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;


public class IcebergWriterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.stitchfix.datahighway.sficebergconnector.writer.IcebergWriterFactory.class);

    private final Table table;
    private final org.apache.iceberg.Schema schema;
    private final long targetFileSizeBytes;
    private final FileFormat fileFormat;
    private final Schema keySchema;
    private final Schema valueSchema;
    private final List<Integer> equalityFieldIds;
    private final boolean upsert;

    public IcebergWriterFactory(
            Table table, org.apache.iceberg.Schema schema,
            Schema keySchema,
            Schema valueSchema,
            long targetFileSize,
            FileFormat fileFormat,
            List<Integer>equalityFieldIds,
            boolean upsert
    ) {

        this.table = table;
        this.schema = schema;
        this.targetFileSizeBytes = targetFileSize;
        this.fileFormat = fileFormat;
        this.keySchema = keySchema;
        this.valueSchema = valueSchema;
        this.equalityFieldIds = equalityFieldIds;
        this.upsert = upsert;
    }

    public TaskWriter<Record> genericRecordWriter() {
        PartitionSpec spec = table.spec();
        org.apache.iceberg.Schema schema = table.schema();
        FileIO io = table.io();
        LOGGER.info("Got the table io {}", io);
        Map<String, String> tableProps = table.properties();
        // Check to see if Sink Task can provide a id to feed to OutputFileFactory
        OutputFileFactory outputFileFactory = OutputFileFactory.builderFor(table, new Random().nextInt(), new Random().nextInt()).build();
        // TODO: 7/14/22
        // Create appender factory based on equality fields, figure out what equality fields is
        // add upsert mode, and decide to use Delta writer or normal writer
        // Factory::newappender returns an appender which TaskWriter::write uses to write record,
        // figure out what the GenericAppender returns for us to use

        GenericAppenderFactory appenderFactory = new GenericAppenderFactory(
                table.schema(),
                table.spec(),
                Ints.toArray(table.schema().identifierFieldIds()),
                table.schema(),
                table.schema());

        // Initialize a task writer to write INSERT only.
        if (spec.isUnpartitioned()) {
            return new UnpartitionedWriter<>(spec, fileFormat, appenderFactory, outputFileFactory, io, targetFileSizeBytes);
        } else {
            return new com.stitchfix.datahighway.sficebergconnector.writer.ConnectPartitionedWriter(spec, fileFormat, appenderFactory, outputFileFactory,
                                                      io, targetFileSizeBytes, schema);
        }
    }



}
