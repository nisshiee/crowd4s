package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.control.Exception.allCatch

trait GetUser {

  import GetUser._

  def getUser(username: String)(implicit conn: CrowdConnection) =
    CrowdHttp.get(path, Map("username" -> username)) flatMap parseResponse
}

object GetUser {

  import ResponseParseHelper._

  val path = "/rest/usermanagement/1/user.json"

  def parseResponse = parseBasicGetResponse(parseUser)

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
}

