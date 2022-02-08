import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import org.apache.kafka.clients.producer.{KafkaProducer , ProducerRecord , Callback , RecordMetadata}
import org.apache.kafka.clients
import scala.concurrent.OnCompleteRunnable
import org.json4s.jackson.Serialization
import org.json4s.native.Serialization.write
import org.json4s.{Formats, NoTypeHints ,DefaultFormats}
import java.io.{ByteArrayOutputStream}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import java.text.SimpleDateFormat
import twitter4j.FilterQuery
import twitter4j.TwitterStream


object MyTwitterUtils {

 /**
 * configure the twitter api
 *
 * @param twitter4j.oauth.consumerKey
 * @param twitter4j.oauth.consumerSecret
 * @param twitter4j.oauth.accessToken
 * @param twitter4j.oauth.accessTokenSecret
 */

  
  def config(
    consumerKey:       String,
    consumerSecret:    String,
    accessToken:       String,
    accessTokenSecret: String): ConfigurationBuilder =
    {

      new ConfigurationBuilder()
        .setOAuthConsumerKey(consumerKey)
        .setOAuthConsumerSecret(consumerSecret)
        .setOAuthAccessToken(accessToken)
        .setOAuthAccessTokenSecret(accessTokenSecret)

    }

  /**
   * Create a twitter stream with the given twitter api config and listener (which publishes to kafka)
   *
   * @param config   Twitter api config created using api secret keys
   * @param listener Listener which publishes to kafka.
   */

  
  def createStream(
    config:   ConfigurationBuilder,
    listener: StatusListener): Unit = {

    val twitterStream = new TwitterStreamFactory(config.build()).getInstance
  
    twitterStream.addListener(listener)
    
    twitterStream.sample()
    
    println("Stream Created")
  }
  
  def simpleStatusListener(kafkaProducer: KafkaProducer[String,  String],
                           kafkaTopic: String): StatusListener = {

    new StatusListener() {

      override def onStallWarning(warning: StallWarning): Unit = {}

      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

      override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}

      //when a tweet comes in get the tweetInfo and publish it as jsonString to kafka
      override def onStatus(status: Status): Unit = {

        
      val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")

     def dumpTweet :TweetCase = TweetCase(
         
           status.getId,
           status.getUser.getId,
           status.getUser.getScreenName,
           status.getUser.getFriendsCount.toLong,
           status.getUser.getFavouritesCount.toLong,
           status.getUser.getFollowersCount.toLong,
           status.getLang,
           status.getUser.getDescription,
           status.getUser.getLocation,
           status.getUser.getStatusesCount,
           formatter.format(status.getUser.getCreatedAt.getTime).toString(),
           status.getText,
           formatter.format(status.getCreatedAt.getTime).toString(),
           (status.getUser.getFollowersCount.toDouble / status.getUser.getFriendsCount.toDouble).toString(),
           //status.getPlace.getCountry
                 
           
         )
         
        

        val tweetMsg = new JsonWriter[TweetCase]().convertObjToJson(dumpTweet)
        send(kafkaProducer , kafkaTopic, tweetMsg)
        
       /*
       val c = new NewClass()
       val json = c.convertJavaToJson(dumpTweet)
       println(json)
      
       publishTweetMsg(kafkaProducer , kafkaTopic, json)
      */
        
      }

      override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}

      override def onException(ex: Exception): Unit = {}
    }

  }

  private def send(kafkaProducer: KafkaProducer[String, String], kafkaTopic: String, msg: String) =
    {
      val tweetMessage = new ProducerRecord[String, String](kafkaTopic, msg)

      kafkaProducer.send(tweetMessage , new Callback() {

        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {

          if (exception != null) {

            println(s"Cannot publish massage to ${kafkaTopic} Caused by: ${exception.getMessage}")

          } else {
            val jsonSuccessSent = s"""
            
            |{
            |  "topicName":"${metadata.topic()}",
            |  "partition":"${metadata.partition().toString}",
            |  "msgOffset":"${metadata.offset().toString()}",
            |  "timestamp":"${metadata.timestamp().toString()}"
            |}
            |""".stripMargin

            println(s"Published Successfully : ${jsonSuccessSent}")

          }

        }

      })

    }
  
  /*
  class NewClass {
  val objectMapper = new ObjectMapper() with ScalaObjectMapper
  objectMapper.registerModule(DefaultScalaModule)

  def convertJavaToJson(obj:TweetCase):String = {
    val out = new ByteArrayOutputStream()
    objectMapper.writeValue(out, obj)
    out.toString
}
}
* */

  
  
   case class TweetCase(
                                StatusId:Long     
                              , UseId:Long 
                              , UserScreenName:String    
                              , UserFriendsCount:Long 
                              , UserFavouritesCount:Long
                              , UserFollowersCount:Long
                              , UserLang:String
                              , UserDesc:String
                              , UserLocation:String
                              , UserStatusCount:Int
                              , UserCreated:String
                              , Text:String
                              , StatusCreatedAt:String
                              , UserFollowersRatio:String
                              //, CountryCode:String
                              ){
     
     
    
    override def toString() :String = 
    {
      s"{UserId : ${this.UseId} , UserScreenName : ${this.UserScreenName}}" 
      
    }

}
}
   
  
