import java.text.SimpleDateFormat
import java.util.Properties;
import java.io.{ByteArrayOutputStream}
import org.joda.time.{DateTime, Days}


object SpamDetector {
  
  
   val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")

  def detect(tweet: Map[String, Any]): Boolean = {
    {
      // Remove recently created users = Remove Twitter users who's profile was created less than a day ago
      Days.daysBetween(
        new DateTime(formatter.parse(tweet.get("UserCreated").mkString).getTime),
        DateTime.now).getDays > 1
    } & {
      // Users That Create Little Content =  Remove users who have only ever created less than 50 tweets
      tweet.get("UserStatusCount").mkString.toInt > 70
    } & {
      // Remove Users With Few Followers
      tweet.get("UserFollowersRatio").mkString.toFloat > 0.20
    }  & {
      // Remove messages with a Large Numbers Of HashTags
      tweet.get("Text").mkString.split(" ").filter(_.startsWith("#")).length < 10
    }  
     
    
  }
  
  
  
}