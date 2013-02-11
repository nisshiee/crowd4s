package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.FieldSerializer._
import scala.util.control.Exception.allCatch

object ResponseParseHelper {

  def parseNotFound[A](json: String): Validation[RequestError, A] = {
    implicit val formats = DefaultFormats
    val err = allCatch opt {
      parse(json).extract[NotFound]
    } getOrElse UnknownError

    err.failure[A]
  }

  def parseBasicGetResponse[A](okParse: String => Validation[JsonParseError, A]): ((Int, String)) => Validation[RequestError, A] = {
    case (200, json) => okParse(json)
    case (401, _) => Unauthorized.failure
    case (403, _) => Forbidden.failure
    case (404, json) => parseNotFound(json)
    case _ => UnknownError.failure
  }

  val camelizeUser: PartialFunction[JField, JField] = {
    case JField("first-name", v) => JField("firstName", v)
    case JField("last-name", v) => JField("lastName", v)
    case JField("display-name", v) => JField("displayName", v)
  }

  def parseUser(json: String): Validation[JsonParseError, User] = {
    implicit val formats = DefaultFormats
    allCatch opt {
      parse(json)
        .transformField(camelizeUser)
        .extract[User]
    } toSuccess JsonParseError
  }
}
