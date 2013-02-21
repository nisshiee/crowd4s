package org.nisshiee.crowd4s

sealed trait CrowdError

sealed trait ConnectionError extends CrowdError
case object ConnectionError extends ConnectionError

sealed trait RequestError extends CrowdError

case object Unauthorized extends RequestError
case object Forbidden extends RequestError

case class NotFound (
   reason: String
  ,message: String
) extends RequestError

sealed trait JsonParseError extends RequestError
case object JsonParseError extends JsonParseError

case object UnknownError extends RequestError
