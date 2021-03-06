package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.control.Exception.allCatch

trait GetGroupList {

  import GetGroupList._

  def getDirectGroupList(username: String)(implicit conn: CrowdConnection, http: CrowdHttp) =
    http.get(directPath, Map("username" -> username)) flatMap parseResponse

  def getNestedGroupList(username: String)(implicit conn: CrowdConnection,  http: CrowdHttp) =
    http.get(nestedPath, Map("username" -> username)) flatMap parseResponse
}

object GetGroupList {

  import ResponseParseHelper._

  val directPath = "/rest/usermanagement/1/user/group/direct.json"
  val nestedPath = "/rest/usermanagement/1/user/group/nested.json"

  def parseResponse = parseBasicGetResponse(parseGroupList)

  def parseGroupList(json: String): Validation[JsonParseError, Seq[String]] = {
    implicit val formats = DefaultFormats
    type JsonVld[A] = Validation[JsonParseError, A]
    implicit val semigroup = Semigroup.firstSemigroup[JsonParseError]
    allCatch opt {
      parse(json) \ "groups" match {
        case JArray(l) => (l map parseGroupAst).sequence[JsonVld, String]
        case _ => JsonParseError.failure
      }
    } getOrElse JsonParseError.failure
  }

  def parseGroupAst(ast: JValue): Validation[JsonParseError, String] = ast \ "name" match {
    case JString(s) => s.success
    case _ => JsonParseError.failure
  }
}
