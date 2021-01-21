import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming._
import org.apache.spark.sql.types._
import org.apache.spark.sql.cassandra._
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import com.datastax.oss.driver.api.core.uuid.Uuids
import com.datastax.spark.connector._
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}

case class DeviceData(country: String, city_key: String, incident_type: String, incident_latitude: Double, incident_longitude: Double, incident_description: String)

object StreamHandler {
	def main(args: Array[String]) {

		// initialize Spark
		val spark = SparkSession
			.builder
			.appName("Stream Handler")
            .config("spark.cassandra.connection.host", "172.31.48.39")
			.config("spark.cassandra.connection.port", "9042")
			.getOrCreate()

		import spark.implicits._

		// read from Kafka
		val inputDF = spark
			.readStream
			.format("kafka")
			.option("kafka.bootstrap.servers", "172.31.48.35:9092")
			.option("subscribe", "crimes")
			.load()



		val rawDF = inputDF.selectExpr("CAST(value AS STRING)").as[String]

		val expandedDF = rawDF.map(row => row.split(","))
			.map(row => DeviceData(
				row(1),
				row(2),
				row(3),
				row(4).toDouble,
				row(5).toDouble,
				row(6),
			))


		val makeUUID = udf(() => Uuids.timeBased().toString)

		val summaryWithIDs = expandedDF.withColumn("uuid", makeUUID())

		val query = summaryWithIDs
			.writeStream
			.trigger(Trigger.ProcessingTime("5 seconds"))
			.foreachBatch { (batchDF: DataFrame, batchID: Long) =>
				println(s"Writing to Cassandra $batchID")
				batchDF.write
					.cassandraFormat("crimes", "keyspace01") // table, keyspace
					.mode("append")
					.save()

                if (batchDF.count > 0){
                    val indexer = new StringIndexer()
                        .setInputCol("incident_type")
                        .setOutputCol("indexed_incident_type")
                        .fit(batchDF);

                    val assembler = new VectorAssembler()
                        .setInputCols(Array("incident_latitude", "incident_longitude"))
                        .setOutputCol("features")

                    val indexed = indexer.transform(batchDF);

                    val incidentTypeConverter = new IndexToString()
                        .setInputCol("prediction")
                        .setOutputCol("incident_type_predicted")
                        .setLabels(indexer.labels)

                    //Logistic regression model
                    val lr = new RandomForestClassifier()
                        .setLabelCol("indexed_incident_type")
                        .setFeaturesCol("features")
                        .setNumTrees(10)

                    val pipeline = new Pipeline()
                        .setStages(Array(assembler, lr,incidentTypeConverter));

                    val lrModel = pipeline.fit(indexed);

                    lrModel.transform(batchDF)
                    .select("uuid", "city_key", "country", "incident_longitude", "incident_latitude","probability", "prediction", "incident_type_predicted")
                    // .collect()
                    .write
                    .cassandraFormat("crimes_prediction", "keyspace01")
                    .mode("append")
					.save()

                    // foreach { case Row(city_key: String, country: String,incident_longitude: Double,incident_latitude:Double ,probability:Vector,prediction: Double, incident_type_predicted: String) =>
                    //     println(s"($city_key, $country, $incident_longitude, $incident_latitude), probability=$probability, prediction=$prediction incident_type_predicted=$incident_type_predicted")
                    // }

                }


			}
			.outputMode("update")
			.start()
		query.awaitTermination()



	}
}