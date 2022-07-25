package org.apache.iceberg.kafkaconnect.writer;

import com.google.common.collect.Lists;
import com.stitchfix.datahighway.sficebergconnector.schema.JsonRecordConverter;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import static org.apache.iceberg.TableProperties.*;

// Consider the iceberg topic partition writer to be some sort of a delegate where in
// it takes string representation of table and validates it to an iceberg table name
// IcebergWriterFactory::create will create a TaskWriter (internal to iceberg ),
// we would have our own class with extends PartitionedFanoutWriter (internal to iceberg) which does the job of
// writing to Iceberg
// Our class could be SinkRecordPartitionedFanoutWriter which uses some kind of a wrapper which extracts the partitioned
// key(iceberg) from the SinkRecord, also we should have some variant of a writer with no partition key(iceberg)
//
//        return new UnpartitionedWriter<>(spec, format, appenderFactory, outputFileFactory, io, targetFileSizeBytes);
//        } else {
//        return new RowDataPartitionedFanoutWriter(spec, format, appenderFactory, outputFileFactory,
//        io, targetFileSizeBytes, schema, flinkSchema);

public class IcebergTopicPartitionWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.stitchfix.datahighway.sficebergconnector.writer.IcebergTopicPartitionWriter.class);

    private final String nameSpace;
    private final String tableName;
    private final Table table;
    private final Catalog catalog;
    private Queue<SinkRecord> buffer;
    private  TaskWriter<Record> writer;
    private JsonRecordConverter recordConverter;

    public IcebergTopicPartitionWriter(
            String nameSpace,
            String tableName,
            Catalog catalog

    ) {
        this.nameSpace = nameSpace;
        this.tableName = tableName;
        this.catalog = catalog;
        this.table = toIcebergTable(nameSpace, tableName);
        this.recordConverter = new JsonRecordConverter(table);
    }

    private TaskWriter<Record> createWriter() {
        Schema schema = table.schema();
        org.apache.kafka.connect.data.Schema keySchema = null;
        org.apache.kafka.connect.data.Schema valueSchema = null;
        long targetFileSize = getTargetFileSize(table.properties());
        FileFormat fileFormat = getFileFormat(table.properties());
        com.stitchfix.datahighway.sficebergconnector.writer.IcebergWriterFactory writerFactory = new com.stitchfix.datahighway.sficebergconnector.writer.IcebergWriterFactory(table, schema, keySchema, valueSchema,
                                                                      targetFileSize, fileFormat,
                                                                      Lists.newArrayList(table.schema().identifierFieldIds()),
                                                                      false);
        return writerFactory.genericRecordWriter();
    }

    private Table toIcebergTable(String nameSpace, String tableName) {
        return catalog.loadTable(TableIdentifier.of(nameSpace, tableName));
    }

    private long getTargetFileSize(Map<String, String> properties) {
        return PropertyUtil.propertyAsLong(properties,
                                           TableProperties.WRITE_TARGET_FILE_SIZE_BYTES, WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);

    }

    private static FileFormat getFileFormat(Map<String, String> properties) {
        String formatString = properties.getOrDefault(DEFAULT_FILE_FORMAT, DEFAULT_FILE_FORMAT_DEFAULT);
        return FileFormat.valueOf(formatString.toUpperCase(Locale.ENGLISH));
    }

    public void add(SinkRecord sinkRecord) {
        buffer.add(sinkRecord);
    }

    public void write() {
        while (!buffer.isEmpty()) {
            Record icebergRecord;
            SinkRecord record = buffer.poll();
            try {
                icebergRecord = recordConverter.convert(record);
                LOGGER.info("Converted Sink record to Iceberg record, {}", icebergRecord);
            } catch (IOException e) {
                LOGGER.error("Error while converting SinkRecord to Iceberg {}", record);
                e.printStackTrace();
                throw new ConnectException(e.getMessage());
            }
            if (writer == null) {
                writer = createWriter();
            }
            try {
                writer.write(icebergRecord);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ConnectException(e.getCause());
            }
        }
    }
}
