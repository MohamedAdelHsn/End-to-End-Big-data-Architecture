import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients
import java.io._
import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;


object TwitterProducer {

  def main(args: Array[String]): Unit =
    {
    
      val consumerKey = getTwitterAccess().getProperty("CONSUMER_KEY")
      val consumerSecret = getTwitterAccess.getProperty("CONSUMER_SECRET")
      val accessToken = getTwitterAccess.getProperty("ACCESS_TOKEN")
      val accessSecret = getTwitterAccess.getProperty("ACCESS_TOKEN_SECRET")
      val kafkaTweetTopic = "unProccessedTweets"

      val props = new Properties();
			props.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "my-twitterKafka-app")
			props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
			props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
			props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,classOf[StringSerializer].getName)
			props.setProperty(ProducerConfig.ACKS_CONFIG, "1")
      
      val producer = new KafkaProducer[String, String](props)

      val twitterConfig = MyTwitterUtils.config(
        consumerKey,
        consumerSecret,
        accessToken,
        accessSecret)

      val twitterListener = MyTwitterUtils.simpleStatusListener(
        producer,
        kafkaTweetTopic)

      val stream = MyTwitterUtils.createStream(
        twitterConfig,
        twitterListener)

    
    
    }
  
  
  def getTwitterAccess() : Properties = 
  {
    
    val reader=new FileReader("/home/hdpadmin/workspace/mytwitterApp/twConf.properties");  
      
    val  p =new Properties();  
    p.load(reader);  
    
    p
    
  }

}