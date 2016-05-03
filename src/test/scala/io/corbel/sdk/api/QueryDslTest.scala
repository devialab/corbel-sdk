package io.corbel.sdk.api

import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import org.json4s.native.Serialization._
import org.scalatest.{Matchers, FlatSpec}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class QueryDslTest extends FlatSpec with Matchers {

  import QueryDsl._

  behavior of "QueryDsl"

  it should "build single value nodes" in {
    val q = Query("f1" -> $eq("abc"))
    q.nodes(0).field should be("f1")
  }

  it should "build multi value nodes" in {
    val q = Query("f2" -> $in(Seq(1, 2, 4)))
    q.nodes(0).field should be ("f2")
  }

  it should "serialize to JSON" in {
    implicit val format = DefaultFormats + new QuerySerializer

    val q = Query("f2" -> $in(Seq(1, 2, 4)))
    write(q) should be("[{\"$in\":{\"f2\":[1,2,4]}}]")

    val q2 = Query("f2" -> $in(Seq(1, 2, 4)), "f3" -> $eq("a"))
    write(q2) should be("[{\"$in\":{\"f2\":[1,2,4]}},{\"$eq\":{\"f3\":\"a\"}}]")

    val q3 = Query("f2" -> $in(Seq(1, 2, 4)), "f3" -> $eq(2))
    write(q3) should be("[{\"$in\":{\"f2\":[1,2,4]}},{\"$eq\":{\"f3\":2}}]")

    val q4 = Query("f2" -> $in(Seq(1, 2, 4)), "f3" -> $eq(2.5))
    write(q4) should be("[{\"$in\":{\"f2\":[1,2,4]}},{\"$eq\":{\"f3\":2.5}}]")
  }

}
