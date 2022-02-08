
import java.io.{ByteArrayOutputStream}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

class JsonWriter[A]() {
  
    val objectMapper = new ObjectMapper() with ScalaObjectMapper
    objectMapper.registerModule(DefaultScalaModule)

    def convertObjToJson(obj: A): String = {
      val out = new ByteArrayOutputStream()
      objectMapper.writeValue(out, obj)
      out.toString
    }
    
    
    /*
    def JsonParser(jsonString:String , schema:Map[String , Object]) = 
    {
      
      
      
      
      
    }
    */
    
    
  }