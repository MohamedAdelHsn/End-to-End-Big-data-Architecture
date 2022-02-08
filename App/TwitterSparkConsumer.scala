// Apache Spark/SparkStreaming Packages needed
import org.apache.spark.{SparkConf , SparkContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{StreamingContext , Seconds }
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.rdd.RDD


// Apache kafka packages needed
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.producer.{KafkaProducer  , ProducerConfig, ProducerRecord , Callback , RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer;


// java utils Packages needed
import java.text.SimpleDateFormat
import java.util.Properties;
import java.io.{ByteArrayOutputStream}


// logger package 
import org.apache.log4j.{Logger , Level}


// json4s packages to (parse / write) json format
import org.json4s.jackson.Serialization
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{Formats, ShortTypeHints ,DefaultFormats}
import org.joda.time.{DateTime, Days}
import org.json4s.native.Serialization.write
import org.json4s.native.Json
import org.json4s.DefaultFormats

// other packages
import SpamDetector.detect


object TwitterSparkConsumer extends Serializable {
 
  
  def main(args :Array[String]) : Unit = 
  {

      val kafkaParams = Map[String, Object](
        "bootstrap.servers" -> "localhost:9092",
        "key.deserializer" -> classOf[StringDeserializer],
        "value.deserializer" -> classOf[StringDeserializer],
        "group.id" -> "myGroupId",
        "auto.offset.reset" -> "latest",
        "enable.auto.commit" -> (false: java.lang.Boolean))

      Logger.getLogger("org").setLevel(Level.ERROR);

      val conf = new SparkConf()
        .setAppName("Twitter-Streaming-DataAnalytics-App")
        .setMaster("local[*]")
        .set("spark.ui.port", "4040")
        .set("spark.streaming.blockInterval", "500ms") 
        .set("spark.scheduler.mode", "FAIR")
        .set("spark.streaming.backpressure.enabled", "true")
        .set("spark.streaming.receiver.maxRate", "40")
        .set("spark.streaming.kafka.maxRatePerPartition", "20")

      val ssc = new StreamingContext(conf, Seconds(1)) 

      val KDStream = KafkaUtils.createDirectStream[String, String](
        ssc,
        PreferConsistent,
        Subscribe[String, String](Array("unProccessedTweets"), kafkaParams))

      // parse tweets json incoming from kafka into scala Map
      val TweetMap: DStream[scala.collection.immutable.Map[String, Any]] = KDStream
        .map(tweet => JsonParser.parseJsonToMap(tweet.value()))

      /*
       * 
       *  detect the sentiment of every tweet and classify the tweets to
         ["NOT_UNDERSTOOD" ,"VERY_NEGATIVE" , "NEGATIVE" , "NEUTRAL" , "POSITIVE" ,"VERY_POSITIVE"]
       
      */

      val TweetMapWithSentiment: DStream[scala.collection.immutable.Map[String, Any]] = TweetMap
        .map {

          tweetMap =>

            val text = tweetMap("Text").toString()
            tweetMap + ("SentimentOfTweet" -> SentimentUtils.detectSentiment(text))

        }

      // detect if this tweet spam or not based on some business rules
      val tweetMapWithSpamDetector = TweetMapWithSentiment.map {

        tweetMap =>
          {

             detect(tweetMap) match {
              case true => tweetMap + ("Spam" -> false)
              case _    => tweetMap + ("Spam" -> true)
            }

          }
      }

      // write the outputs to another kafka topic called (ProccessedTweets)
      tweetMapWithSpamDetector.foreachRDD { rdd =>

        if (!rdd.isEmpty()) {

          rdd.foreachPartition { partitionOfRecords =>

            val props = new Properties();
            props.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "my-twitterKafka-app")
            props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
            props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
            props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
            props.setProperty(ProducerConfig.ACKS_CONFIG, "1")

            val producer = new KafkaProducer[String, String](props)

            partitionOfRecords.foreach { tweetMap =>

              // convert every map of tweets into json format to publish them to kafka topic
              val json = Json(DefaultFormats).write(tweetMap)

              producer.send(new ProducerRecord[String, String]("ProccessedTweets", json))

            }

            producer.close()

          }

        }

      }

      
      ssc.start()// Start the computation
      ssc.awaitTermination()
    
    
  }

 

      
}