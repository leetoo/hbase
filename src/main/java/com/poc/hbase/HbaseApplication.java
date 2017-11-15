package com.poc.hbase;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.*;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog;
import org.apache.spark.sql.execution.datasources.hbase.SparkHBaseConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class HbaseApplication {

    private static Logger logger = LoggerFactory.getLogger(HbaseApplication.class);

	public static void main(String[] args) throws IOException {
		ConfigurableApplicationContext context = SpringApplication.run(HbaseApplication.class, args);
        /*Connection connection = context.getBean(Connection.class);
        if (connection != null) {
            Table table = connection.getTable(TableName.valueOf("test"));
            Scan scan = new Scan();
            ResultScanner scanner = table.getScanner(scan);
            for (Result result: scanner) {
                logger.info(Bytes.toString(result.getValue("cf".getBytes(), "a".getBytes())));
                logger.info(Bytes.toString(result.getValue("cf".getBytes(), "b".getBytes())));
                logger.info(Bytes.toString(result.getValue("cf".getBytes(), "c".getBytes())));
            }
        } else {
            logger.error("Bean initialization failed");
        }*/

        String tableName = "test";

		SparkConf conf = new SparkConf().setAppName("POC").setMaster("local[2]");
		conf.set("spark.hbase.host", "localhost");
        SparkContext sparkContext = new SparkContext(conf);

        /*org.apache.hadoop.conf.Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.master", "localhost:60000");
        configuration.setInt("timeout", 120000);
        configuration.set("hbase.zookeeper.quorum", "localhost");
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set(TableInputFormat.INPUT_TABLE, tableName);

        RDD rdd = sparkContext.newAPIHadoopRDD(configuration, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
        logger.info("Table count: {}", rdd.count());
        sparkContext.stop();*/

        //JavaSparkContext javaSparkContext = new JavaSparkContext("local", "POC", conf);


        SparkSession session = SparkSession.builder().master("local")
                .appName("Word Count")
                //.config(conf)
                .sparkContext(sparkContext)
                //.config("hbase.master", "localhost:60000")
                //.config("timeout", 120000)
                //.config("hbase.zookeeper.quorum", "localhost")
                //.config("hbase.zookeeper.property.clientPort", "2181")
                .getOrCreate();

        SQLContext sqlContext = session.sqlContext();

        /*final String hb_staging_table = "HbStagingTable";
        final String staging_table = "StagingTable";
        final String insert_sql = "INSERT INTO TABLE " + staging_table + " VALUES (\"strcol\" , \"bytecol\" , \"shortcol\" , \"intcol\" ," +
                "  \"longcol\" , \"floatcol\" , \"doublecol\")";
        final String retrieve_sql = "SELECT * FROM " + staging_table;
        String create_sql = "CREATE TABLE " + staging_table + " (strcol STRING, bytecol STRING, shortcol STRING, intcol STRING, longcol STRING, floatcol STRING, doublecol STRING) TBLPROPERTIES(" +
                "'hbaseTableName'='" + hb_staging_table +"'," +
                "'keyCols'='doublecol;strcol;intcol'," +
                "'nonKeyCols'='bytecol,cf1,hbytecol;shortcol,cf1,hshortcol;longcol,cf2,hlongcol;floatcol,cf2,hfloatcol')";

        session.sql(create_sql).collect();
        session.sql(insert_sql).collect();
        List<Row> rows = session.sql(retrieve_sql).collectAsList();*/

        String catalog = "{\"table\":{\"namespace\":\"default\",\"name\":\"test\"},\"rowkey\":\"key\",\"columns\":{\"col0\":{\"cf\":\"rowkey\",\"col\":\"key\",\"type\":\"string\"},\"col1\":{\"cf\":\"cf\",\"col\":\"a\",\"type\":\"string\"},\"col2\":{\"cf\":\"cf\",\"col\":\"b\",\"type\":\"string\"},\"col3\":{\"cf\":\"cf\",\"col\":\"c\",\"type\":\"string\"}}}";

        Map<String, String> map = new HashMap<>();
        map.put("catalog", catalog);

        //List<Row> rows = sqlContext.read().options(map).format("org.apache.spark.sql.execution.datasources.hbase").load().collectAsList();
        //System.out.println(rows.toString());

        Dataset<Row> dataSet = sqlContext.read().options(map).format("org.apache.spark.sql.execution.datasources.hbase").load();

        dataSet.createOrReplaceTempView("table");

        List<Row> rows =  sqlContext.sql("select * from table").collectAsList();

        System.out.println(rows.toString());

        sparkContext.stop();

    }
}