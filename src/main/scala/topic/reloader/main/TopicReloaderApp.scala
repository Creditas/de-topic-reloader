package topic.reloader.main

import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.struct
import topic.reloader.WrappedSession
import topic.reloader.lib.model.AppName
import za.co.absa.abris.avro.functions.to_avro
import za.co.absa.abris.avro.read.confluent.SchemaManagerFactory
import za.co.absa.abris.avro.registry.{LatestVersion, SchemaSubject}
import za.co.absa.abris.config.{AbrisConfig, ToAvroConfig}

object TopicReloaderApp extends App {

  val log = LogManager.getRootLogger
  log.setLevel(Level.WARN)

  log.warn(args)

  val submissionId = args(0)
  val env = args(1)
  val topicName = args(2)
  //  val endTime = args(3) //TODO: faltando adicionar a data final de processamento

  val conf = ConfigFactory.load().getConfig(env)

  val splittedTopicName = topicName.split("\\.")
  val domain = splittedTopicName(1)
  val subdomain = splittedTopicName(2)
  val eventName = splittedTopicName(3)

  //TODO: incluir passwords na parameter store
  val SR_PASSWORD = ""
  val KAFKA_PASSWORD = ""

  val inputDir = conf.getString("input-path-pattern")
    .replace("EVENT_NAME", eventName)
    .replace("SUBDOMAIN", subdomain)
    .replace("DOMAIN", domain)

  val spark: SparkSession = WrappedSession.buildSparkSession(AppName("topic-reloader"))

  val schemaRegistryAddr = conf.getString("schema-registry-address")
  val registryConfig = Map(
    AbrisConfig.SCHEMA_REGISTRY_URL -> schemaRegistryAddr,
    "basic.auth.credentials.source" -> "USER_INFO",
    "basic.auth.user.info" -> (conf.getString("schema-registry-api-key") + ":" + SR_PASSWORD)
  )
  val schemaManager = SchemaManagerFactory.create(registryConfig)


  val reloadedTopicName = s"$topicName-reloaded"
  val subject = SchemaSubject.usingTopicNameStrategy(reloadedTopicName)
  val latestSchema = schemaManager.getSchemaBySubjectAndVersion(subject, LatestVersion())

  val toAvroConfig1: ToAvroConfig = AbrisConfig
    .toConfluentAvro
    .downloadSchemaByLatestVersion
    .andTopicNameStrategy(reloadedTopicName)
    .usingSchemaRegistry(registryConfig)

  val sourceDF = spark.read
    .format("avro")
    .option("mergeSchema", "true")
    .load(inputDir)
    .drop("version", "year", "month", "day") //partitioning columns, they're not on the original event.

  sourceDF.printSchema()
  sourceDF.show()
  println(s"src count: ${sourceDF.count()}")

  val allColumns = struct(sourceDF.columns.head, sourceDF.columns.tail: _*)
  val avroDF = sourceDF.select(to_avro(allColumns, toAvroConfig1) as 'value)

  avroDF.printSchema()
  avroDF.show()
  println(s"dst count: ${avroDF.count()}")

  avroDF.write
    .format("kafka")
    .option("topic", reloadedTopicName)
    .option("kafka.bootstrap.servers", conf.getString("kafka-bootstrap-servers"))
    .option("kafka.security.protocol", "SASL_SSL")
    .option("kafka.sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + conf.getString("kafka-api-key") + "\" password=\"" + KAFKA_PASSWORD + "\";")
    .option("kafka.sasl.mechanism", "PLAIN")
    .save()
}
