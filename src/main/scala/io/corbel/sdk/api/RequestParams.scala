package io.corbel.sdk.api

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */

object Direction extends Enumeration {
  val ASC = Value("ASC")
  val DESC = Value("DESC")
}

case class SortParams(direction: Direction.Value, field: String)

case class RequestParams(query: Option[Query] = None, page: Int = 0, pageSize: Int = -1, sort: Option[SortParams] = None)
