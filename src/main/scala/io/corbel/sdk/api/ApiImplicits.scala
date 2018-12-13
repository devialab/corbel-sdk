package io.corbel.sdk.api

import org.json4s.DefaultFormats
import org.json4s.native.Serialization._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait ApiImplicits {

  implicit val format = DefaultFormats + new QuerySerializer()

  implicit def paramsToMap(params: RequestParams): Map[String, String] = {
    var map = Map.empty[String, String]
    for(query <- params.query){
      map += "api:query" -> write(query)
    }
    map += "api:page" -> params.page.toString
    if(params.pageSize > 0) {
      map += "api:pageSize" -> params.pageSize.toString
    }
    params.sort.map(sort => {
      val field = sort.field
      val direction = sort.direction
      map += "api:sort" -> write(field -> direction.toString)
    })
    map
  }

}
