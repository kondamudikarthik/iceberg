package org.apache.iceberg.kafkaconnect.catalog;

import com.stitchfix.datahighway.sficebergconnector.IcebergSinkConnectorConfig;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.dynamodb.DynamoDbCatalog;
import org.apache.iceberg.aws.glue.GlueCatalog;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.hive.HiveCatalog;
import org.apache.iceberg.jdbc.JdbcCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class IcebergCatalogFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.stitchfix.datahighway.sficebergconnector.writer.IcebergCatalogFactory.class);

    public static Catalog catalog(Map<String, String> props) {
        Catalog catalog = null;
        String catalogClassName = props.get(IcebergSinkConnectorConfig.ICEBERG_CATALOG_IMPL_CLASS_CONFIG);
        LOGGER.info("Got the catalog class {}", catalogClassName);
        if (catalogClassName.equals(JdbcCatalog.class.getName())) {
                catalog = jdbcCatalog(props);
        } else if (catalogClassName.equals(HiveCatalog.class.getName())) {
            catalog = hiveCatalog(props);
        } else if (catalogClassName.equals(DynamoDbCatalog.class.getName())) {
            catalog = dynamoDbCatalog(props);
        } else if (catalogClassName.equals(GlueCatalog.class.getName())) {
            catalog = glueCatalog(props);
        }
        else {
            throw new RuntimeException(String.format("Un supported Catalog class %s", catalogClassName));
        }

        return catalog;
    }

    private static Catalog glueCatalog(Map<String, String> props) {
        return null;
    }

    private static Catalog dynamoDbCatalog(Map<String, String> props) {
        return null;
    }

    private static Catalog hiveCatalog(Map<String, String> props) {
        String uri = props.get(IcebergSinkConnectorConfig.ICEBERG_CATALOG_URI);
        String warehousePath = "s3://stitchfix.aa.dwizzy/dw_icebergs";
        Map<String, String> properties = new HashMap<>();
        props.put(CatalogProperties.URI, uri);
        props.put(CatalogProperties.WAREHOUSE_LOCATION, warehousePath);
        props.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
        Catalog catalog = new HiveCatalog();
        LOGGER.info("Initializing Hive Catalog with properties: {}", properties);
        catalog.initialize("hive", properties);
        return catalog;
    }

    private static Catalog jdbcCatalog(Map<String, String> props) {
        Map<String, String> properties = new HashMap<>();
        properties.put(CatalogProperties.CATALOG_IMPL, JdbcCatalog.class.getName());
        properties.put(CatalogProperties.URI, "jdbc:postgresql://postgres:5432/demo_catalog");
        properties.put(JdbcCatalog.PROPERTY_PREFIX + "user", "admin");
        properties.put(JdbcCatalog.PROPERTY_PREFIX + "password", "password");
        properties.put(CatalogProperties.WAREHOUSE_LOCATION, "/home/iceberg/warehouse");
        properties.put(CatalogProperties.FILE_IO_IMPL, HadoopFileIO.class.getName());
        Catalog catalog = new JdbcCatalog();
        catalog.initialize("jdbc", properties);
        return catalog;
    }
}
