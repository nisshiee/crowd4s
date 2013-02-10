package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._
import org.json4s._

class GetUserSpec extends Specification with DataTables { def is =

  "GetUser"                                                                     ^
    "camelize"                                                                  ^
      "target of camelize"                                                      ! e1 ^
      "not target of camelize"                                                  ! e2 ^
                                                                                p^
    "parseUser"                                                                 ^
      "valid json is parsed to Success(User)"                                   ! e3^
      "invalid json is parsed to Failure(JsonParseError)"                       ! e4^
                                                                                p^
    "parseResponse"                                                             ^
      "known status code"                                                       ! e5^
      "unknown status code"                                                     ! e6^
                                                                                end

  import GetUser._

  def e1 =
    "fieldName"    || "resultFieldName" |
    "first-name"   !! "firstName"       |
    "last-name"    !! "lastName"        |
    "display-name" !! "displayName"     |> { (fieldName, resultFieldName) =>
      camelize(JField(fieldName, JInt(1))) must beLike {
        case JField(f, _) => f must equalTo(resultFieldName)
      }
    }

  def e2 =
    camelize.lift(JField("dummy-name", JInt(1))) must beNone


  val validUserJson = """{"expand":"attributes","link":{"href":"https://example.com/crowd/rest/usermanagement/1/user?username=hoge","rel":"self"},"name":"hoge","first-name":"Taro","last-name":"Hoge","display-name":"Taro Hoge","email":"hoge@example.com","password":{"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/password?username=hoge","rel":"edit"}},"active":true,"attributes":{"attributes":[],"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/attribute?username=hoge","rel":"self"}}}"""


  val validUser = User("hoge", "Taro", "Hoge", "Taro Hoge", "hoge@example.com", true)


  val invalidUserJson = """{"expand":"attributes","link":{"href":"https://example.com/crowd/rest/usermanagement/1/user?username=hoge","rel":"self"},"name":"hoge","first-name":"Taro","last-name":"Hoge","display-name":"Taro Hoge","email":"hoge@example.com","password":{"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/password?username=hoge","rel":"edit"}},"attributes":{"attributes":[],"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/attribute?username=hoge","rel":"self"}}}"""


  def e3 =
    parseUser(validUserJson).toOption must beSome.which(validUser ==)

  def e4 =
    parseUser(invalidUserJson).toEither must beLeft.like {
      case JsonParseError => ok
      case _ => ko
    }

  val notFoundJson = """{"reason":"USER_NOT_FOUND","message":"User <hoge> does not exist"}"""
  val notFound = NotFound("USER_NOT_FOUND", "User <hoge> does not exist")

  def e5 =
    "response"           | "result"             |
    (200, validUserJson) ! validUser.success    |
    (401, "hoge")        ! Unauthorized.failure |
    (403, "hoge")        ! Forbidden.failure    |
    (404, notFoundJson)  ! notFound.failure     |> { (response, result) =>
      parseResponse(response) must equalTo(result)
    }

  def e6 =
    "statusCode" | "reulst"             |
    201          ! UnknownError.failure |
    202          ! UnknownError.failure |
    203          ! UnknownError.failure |
    400          ! UnknownError.failure |
    402          ! UnknownError.failure |
    405          ! UnknownError.failure |
    500          ! UnknownError.failure |
    503          ! UnknownError.failure |> { (code, result) =>
      parseResponse(code -> "hoge") must equalTo(result)
    }
}
