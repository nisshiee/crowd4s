package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.control.Exception.allCatch

trait Authenticate {

  import Authenticate._

  def authenticate(username: String, password: Password)(implicit conn: CrowdConnection) =
    CrowdHttp.postJson(path, Map("username" -> username), toJson(password))
      .flatMap(parseResponse)
}

object Authenticate {

  val path = "/rest/usermanagement/1/authentication.json"

  def toJson(pass: Password): JValue = {
    implicit val formats = DefaultFormats
    Extraction.decompose(pass)
  }

  def parseResponse: ((Int, String)) => Validation[RequestError, AuthenticationResult] = {
    case (200, json) => parseUser(json) map AuthenticationResult.Success.apply
    case (400, json) => parseFailure(json)
    case (401, _) => Unauthorized.failure
    case (403, _) => Forbidden.failure
    case _ => UnknownError.failure
  }


  val camelize: PartialFunction[JField, JField] = {
    case JField("first-name", v) => JField("firstName", v)
    case JField("last-name", v) => JField("lastName", v)
    case JField("display-name", v) => JField("displayName", v)
  }

  def parseUser(json: String): Validation[JsonParseError, User] = {
    implicit val formats = DefaultFormats
    allCatch opt {
      parse(json)
        .transformField(camelize)
        .extract[User]
    } toSuccess JsonParseError
  }

  def parseFailure(json: String): Validation[JsonParseError, AuthenticationResult] = {
    implicit val formats = DefaultFormats
    allCatch opt {
      parse(json).extract[AuthenticationResult.Failure]
    } toSuccess JsonParseError
  }
}
