package io.corbel.sdk.api

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class RequestParams(query: Option[Query] = None, page: Int = 0, pageSize: Int = -1)
