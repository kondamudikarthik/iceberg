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

import io.confluent.connect.avro.AvroDataConfig;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.iceberg.aws.dynamodb.DynamoDbCatalog;
import org.apache.iceberg.aws.glue.GlueCatalog;
import org.apache.iceberg.hive.HiveCatalog;
import org.apache.iceberg.jdbc.JdbcCatalog;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

public class IcebergSinkConnectorConfig extends AbstractConfig {

    public static final String ICEBERG_NAMESPACE_CONFIG = "iceberg.namespace";
    public static final String ICEBERG_NAMESPACE_CONFIG_DOC = "iceberg.namespace";
    public static final String ICEBERG_TABLE_CONFIG = "iceberg.table";
    public static final String ICEBERG_TABLE_CONFIG_DOC = "iceberg.table";
    public static final String ICEBERG_CATALOG_IMPL_CLASS_CONFIG = "iceberg.catalog.impl.class";
    public static final String ICEBERG_CATALOG_IMPL_CLASS_CONFIG_DOC = "iceberg.catalog.impl.class";
    public static final String ICEBERG_CATALOG_URI = "iceberg.catalog.uri";
    public static final String ICEBERG_CATALOG_JDBC_USER = "iceberg.catalog.jdbc.user";
    public static final String ICEBERG_CATALOG_JDBC_USER_DEFAULT = "";
    public static final String ICEBERG_CATALOG_JDBC_PASSWORD = "iceberg.catalog.jdbc.password";
    public static final String ICEBERG_CATALOG_JDBC_PASSWORD_DEFAULT = "";
    public static final String ICEBERG_CATALOG_JDBC_USER_DOC = "iceberg.catalog.jdbc.user";
    public static final String ICEBERG_CATALOG_JDBC_PASSWORD_DOC = "iceberg.catalog.jdbc.password";
    public static final String ICEBERG_CATALOG_WAREHOUSE_DIR = "iceberg.catalog.warehouse.dir";
    public static final String TOPIC_NAME_DOC = "iceberg.catalog.impl.class";
    public static final String SCHEMA_CACHE_SIZE_CONFIG = "schemas.cache.config";
    public static final String SCHEMA_CACHE_SIZE_DOC = "The size of the schema cache used in the Avro converter.";
    public static final int SCHEMA_CACHE_SIZE_DEFAULT = 1000;
    public static final String ENHANCED_AVRO_SCHEMA_SUPPORT_CONFIG = "enhanced.avro.schema.support";
    public static final boolean ENHANCED_AVRO_SCHEMA_SUPPORT_DEFAULT = true;
    public static final String CONNECT_META_DATA_CONFIG = "connect.meta.data";
    public static final boolean CONNECT_META_DATA_DEFAULT = true;
    private static final Collection<Object> ICEBERG_CATALOG_IMPL_VALID_VALUES = Arrays.asList(JdbcCatalog.class, HiveCatalog.class, JdbcCatalog.class, DynamoDbCatalog.class, GlueCatalog.class);
    private static final String ENHANCED_AVRO_SCHEMA_SUPPORT_DOC = "";
    private static final String ICEBERG_CATALOG_URI_DOC = "uri to connect to catalog";

    public IcebergSinkConnectorConfig(ConfigDef definition, Map<?, ?> originals) {
        super(definition, originals);
    }

    public IcebergSinkConnectorConfig(Map<String, String> props) {
        super(getConfig(), props);
    }

    public AvroDataConfig avroDataConfig() {
        Map<String, Object> props = new HashMap();
        props.put("schemas.cache.config", this.get("schemas.cache.config"));
        props.put("enhanced.avro.schema.support", this.get("enhanced.avro.schema.support"));
        props.put("connect.meta.data", this.get("connect.meta.data"));
        return new AvroDataConfig(props);
    }

    public static ConfigDef getConfig() {
        ConfigDef configDef = new ConfigDef();
        configDef.define("iceberg.catalog.impl.class", Type.STRING, Importance.LOW, "iceberg.catalog.impl.class");
        configDef.define("iceberg.catalog.uri", Type.STRING, Importance.HIGH, "uri to connect to catalog");
        configDef.define("iceberg.namespace", Type.STRING, Importance.HIGH, "iceberg.namespace");
        configDef.define("iceberg.table", Type.STRING, Importance.HIGH, "iceberg.table");
        configDef.define("iceberg.catalog.jdbc.user", Type.STRING, "", Importance.LOW, "iceberg.catalog.jdbc.user");
        configDef.define("iceberg.catalog.jdbc.password", Type.STRING, "", Importance.LOW, "iceberg.catalog.jdbc.password");
        configDef.define("schemas.cache.config", Type.INT, 1000, Importance.LOW, "The size of the schema cache used in the Avro converter.");
        configDef.define("connect.meta.data", Type.BOOLEAN, true, Importance.LOW, "Toggle for enabling/disabling connect converter to add its meta data to the output schema or not");
        configDef.define("enhanced.avro.schema.support", Type.BOOLEAN, true, Importance.LOW, "");
        return configDef;
    }
}
