package io.corbel.sdk.api

import org.json4s.JsonAST.JArray
import org.json4s.JsonAST.JBool
import org.json4s.JsonAST.JInt
import org.json4s.JsonAST.JNull
import org.json4s.JsonAST.JString
import org.json4s.JsonAST.JValue
import org.json4s._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class Query(nodes: QueryNode*)

case class QueryNode(field: String, operation: QueryOperation)

sealed trait QueryOperation {
  val value: JValue
}

case class $eq(value: JValue) extends QueryOperation
case class $gt(value: JValue) extends QueryOperation
case class $gte(value: JValue) extends QueryOperation
case class $in(value: JArray) extends QueryOperation
case class $nin(value: JArray) extends QueryOperation
case class $all(value: JArray) extends QueryOperation
case class $lt(value: JValue) extends QueryOperation
case class $lte(value: JValue) extends QueryOperation
case class $ne(value: JValue) extends QueryOperation
case class $like(value: JValue) extends QueryOperation
case class $exists(value: JValue) extends QueryOperation
case class $size(value: JValue) extends QueryOperation

object QueryDsl extends Implicits with DoubleMode with JsonDSL{
  implicit def pairToQueryNode(pair: (String, QueryOperation)): QueryNode = QueryNode(pair._1, pair._2)
}

class QuerySerializer extends CustomSerializer[Query](format => (
 {
   case _ => ??? //deserialization not implemented
 },
  {
    case Query(nodes @ _*) => JArray(nodes.map(QuerySerializer.serialize).toList)
  }

))

private object QuerySerializer {
  import org.json4s.JsonDSL._
  implicit val format = DefaultFormats

  def serialize(node: QueryNode): JValue = node match {
    case QueryNode(field, op:QueryOperation) =>
      op.getClass.getSimpleName -> (field -> op.value)
    case _ => JNull
  }

}