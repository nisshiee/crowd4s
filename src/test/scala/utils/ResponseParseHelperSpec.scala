package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._
import org.json4s._

class ResponseParseHelperSpec extends Specification with DataTables { def is =

  "ResponseParseHelper"                                                         ^
    "parseNotFound"                                                             ^
      "valid json is parsed to Failure(NotFound)"                               ! e1^
      "invaild json is parsed to Failure(UnknownError)"                         ! e2^
                                                                                p^
    "parseBasicGetResponse"                                                     ^
      "known status code"                                                       ! e3^
      "unknown status code"                                                     ! e4^
                                                                                p^
    "camelizeUser"                                                              ^
      "target of camelize"                                                      ! e5 ^
      "not target of camelize"                                                  ! e6 ^
                                                                                p^
    "parseUser"                                                                 ^
      "valid json is parsed to Success(User)"                                   ! e7^
      "invalid json is parsed to Failure(JsonParseError)"                       ! e8^
                                                                                end

  import ResponseParseHelper._

  val notFoundJson = """{"reason":"USER_NOT_FOUND","message":"User <hoge> does not exist"}"""
  val notFound = NotFound("USER_NOT_FOUND", "User <hoge> does not exist")

  def e1 = {
    parseNotFound(notFoundJson).toEither must beLeft.like {
      case NotFound(reason, message) =>
        (reason must equalTo("USER_NOT_FOUND")) and
          (message must equalTo("User <hoge> does not exist"))
    }
  }

  def e2 = {
    val json = """{"reason":"USER_NOT_FOUND"}"""
    parseNotFound(json).toEither must beLeft.like {
      case UnknownError => ok
      case _ => ko
    }
  }

  def parseDummy: String => Validation[JsonParseError, Boolean] = _ => true.success

  def e3 =
    "response"          | "result"             |
    (200, "dummy")      ! true.success    |
    (401, "hoge")       ! Unauthorized.failure |
    (403, "hoge")       ! Forbidden.failure    |
    (404, notFoundJson) ! notFound.failure     |> { (response, result) =>
      parseBasicGetResponse(parseDummy)(response) must equalTo(result)
    }

  def e4 =
    "statusCode" | "reulst"             |
    201          ! UnknownError.failure |
    202          ! UnknownError.failure |
    203          ! UnknownError.failure |
    400          ! UnknownError.failure |
    402          ! UnknownError.failure |
    405          ! UnknownError.failure |
    500          ! UnknownError.failure |
    503          ! UnknownError.failure |> { (code, result) =>
      parseBasicGetResponse(parseDummy)(code -> "hoge") must equalTo(result)
    }

  def e5 =
    "fieldName"    || "resultFieldName" |
    "first-name"   !! "firstName"       |
    "last-name"    !! "lastName"        |
    "display-name" !! "displayName"     |> { (fieldName, resultFieldName) =>
      camelizeUser(JField(fieldName, JInt(1))) must beLike {
        case JField(f, _) => f must equalTo(resultFieldName)
      }
    }

  def e6 =
    camelizeUser.lift(JField("dummy-name", JInt(1))) must beNone


  val validUserJson = """{"expand":"attributes","link":{"href":"https://example.com/crowd/rest/usermanagement/1/user?username=hoge","rel":"self"},"name":"hoge","first-name":"Taro","last-name":"Hoge","display-name":"Taro Hoge","email":"hoge@example.com","password":{"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/password?username=hoge","rel":"edit"}},"active":true,"attributes":{"attributes":[],"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/attribute?username=hoge","rel":"self"}}}"""


  val validUser = User("hoge", "Taro", "Hoge", "Taro Hoge", "hoge@example.com", true)


  val invalidUserJson = """{"expand":"attributes","link":{"href":"https://example.com/crowd/rest/usermanagement/1/user?username=hoge","rel":"self"},"name":"hoge","first-name":"Taro","last-name":"Hoge","display-name":"Taro Hoge","email":"hoge@example.com","password":{"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/password?username=hoge","rel":"edit"}},"attributes":{"attributes":[],"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/attribute?username=hoge","rel":"self"}}}"""


  def e7 =
    parseUser(validUserJson).toOption must beSome.which(validUser ==)

  def e8 =
    parseUser(invalidUserJson).toEither must beLeft.like {
      case JsonParseError => ok
      case _ => ko
    }
}
