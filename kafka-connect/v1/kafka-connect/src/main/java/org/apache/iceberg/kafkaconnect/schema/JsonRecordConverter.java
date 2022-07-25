package org.apache.iceberg.kafkaconnect.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.types.Types;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class JsonRecordConverter  {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.stitchfix.datahighway.sficebergconnector.schema.JsonRecordConverter.class);

    private final Schema schema;
    private final ObjectMapper mapper;
    private final Table table;

    public JsonRecordConverter(Table table) {
        this.table = table;
        this.mapper = new ObjectMapper();
        this.schema = table.schema();
    }

    public GenericRecord convert(SinkRecord record) throws IOException {
//        if (schema == null) {
//            schema = schemaProvider.getSchema(record);
//        }
        return toIcebergRecord(schema, record);
    }

    private GenericRecord toIcebergRecord(Schema schema, SinkRecord record) throws IOException {
        byte[] recordValue;
        if (record.value() instanceof byte[]) {
            recordValue = (byte[]) record.value();
        } else {
            recordValue = convertObjectToBytes(record.value());
        }
        JsonNode valueNode = mapper.readTree(recordValue);
        Types.StructType structType = schema.asStruct();
        GenericRecord genericRecord = GenericRecord.create(structType);
        for (Types.NestedField field : structType.fields()) {
            String fieldName = field.name();
            if (valueNode == null || !valueNode.has(fieldName) || valueNode.get(fieldName) == null) {
                genericRecord.setField(fieldName, null);
            } else {
                genericRecord.setField(fieldName, toIcebergValue(field, valueNode.get(fieldName)));

            }
        }
        return genericRecord;
    }

    private Object toIcebergValue(Types.NestedField field, JsonNode node) {
        LOGGER.info("Mapping Field {}, Type {}", field.name(), field.type());
        final Object val;
        switch (field.type().typeId()) {
            case INTEGER: // int 4 bytes
                val = node.isNull() ? null : node.asInt();
                break;
            case LONG: // long 8 bytes
                val = node.isNull() ? null : node.asLong();
                break;
            case FLOAT: // float is represented in 32 bits,
                val = node.isNull() ? null : node.floatValue();
                break;
            case DOUBLE: // double is represented in 64 bits
                val = node.isNull() ? null : node.asDouble();
                break;
            case BOOLEAN:
                val = node.isNull() ? null : node.asBoolean();
                break;
            case STRING:
                // if the node is not a value node (method isValueNode returns false), convert it to string.
                val = node.isValueNode() ? node.asText(null) : node.toString();
                break;
            case BINARY:
                try {
                    val = node.isNull() ? null : ByteBuffer.wrap(node.binaryValue());
                } catch (IOException e) {
                    LOGGER.error("Failed to convert binary value to iceberg value, field:" + field.name(), e);
                    throw new RuntimeException("Failed Processing Event!", e);
                }
                break;
            case LIST:
                val = mapper.convertValue(node, ArrayList.class);
                break;
            case MAP:
                val = mapper.convertValue(node, Map.class);
                break;
//            case STRUCT:
//                // create it as struct, nested type
//                // recursive call to get nested data/record
//                val = toIcebergRecord(field.type().asStructType(), node);
//                break;
            default:
                // default to String type
                // if the node is not a value node (method isValueNode returns false), convert it to string.
                val = node.isValueNode() ? node.asText(null) : node.toString();
                break;
        }
        LOGGER.info("Returning value {}", val);
        return val;
    }

    public  byte[] convertObjectToBytes(Object obj) throws IOException {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        try (ObjectOutputStream ois = new ObjectOutputStream(boas)) {
            ois.writeObject(obj);
            return boas.toByteArray();
        }
    }



}
