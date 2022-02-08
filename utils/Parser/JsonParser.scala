
import org.json4s.jackson.Serialization
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{Formats, ShortTypeHints ,DefaultFormats}


object JsonParser {
  
   def parseJsonToMap(text: String): scala.collection.immutable.Map[String, Any] =
    {

      implicit val formats = DefaultFormats
      val parsedJson = parse(text)

      Map(
        "StatusId" -> (parsedJson \ "StatusId").extract[Long],
        "UseId" -> (parsedJson \ "UseId").extract[String],
        "UserScreenName" -> (parsedJson \ "UserScreenName").extract[String],
        "UserFriendsCount" -> (parsedJson \ "UserFriendsCount").extract[Long],
        "UserFavouritesCount" -> (parsedJson \ "UserFavouritesCount").extract[Long],
        "UserFollowersCount" -> (parsedJson \ "UserFollowersCount").extract[Long],
        "UserLang" -> (parsedJson \ "UserLang").extract[String],
        "UserDesc" -> (parsedJson \ "UserDesc").extract[String],
        "UserLocation" -> (parsedJson \ "UserLocation").extract[String],
        "UserStatusCount" -> (parsedJson \ "UserStatusCount").extract[Int],
        "UserCreated" -> (parsedJson \ "UserCreated").extract[String],
        "Text" -> (parsedJson \ "Text").extract[String],
        "StatusCreatedAt" -> (parsedJson \ "StatusCreatedAt").extract[String],
        "UserFollowersRatio" -> (parsedJson \ "UserFollowersRatio").extract[String].toDouble
      )
        

    }
  
}