package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._
import org.json4s._

class AuthenticateSpec extends Specification with DataTables { def is =

  "GetUser"                                                                     ^
    "parseFailure"                                                              ^
      "valid json is parsed to Success(Failure)"                                ! e1^
      "invalid json is parsed to Failure(JsonParseError)"                       ! e2^
                                                                                p^
    "toJson"                                                                    ! e3^
    "parseResponse"                                                             ^
      "known status code"                                                       ! e4^
      "unknown status code"                                                     ! e5^
                                                                                end

  import Authenticate._

  val failureJson = """{"reason":"INVALID_USER_AUTHENTICATION","message":"Failed to authenticate principal, password was invalid"}"""
  val invalidFailureJson = """{"reason":"INVALID_USER_AUTHENTICATION","message":"Failed to authenticate principal, password was invalid""""
  val failureObj = AuthenticationResult.Failure("INVALID_USER_AUTHENTICATION", "Failed to authenticate principal, password was invalid")

  def e1 = parseFailure(failureJson).toOption must beSome.which(failureObj ==)

  def e2 = parseFailure(invalidFailureJson).toEither must beLeft.like {
    case JsonParseError => ok
    case _ => ko
  }

  def e3 = {
    val pass: Password = "password"
    toJson(pass) must beLike {
      case JObject(JField("value", JString("password")) :: Nil) => ok
      case _ => ko
    }
  }

  val validUserJson = """{"expand":"attributes","link":{"href":"https://example.com/crowd/rest/usermanagement/1/user?username=hoge","rel":"self"},"name":"hoge","first-name":"Taro","last-name":"Hoge","display-name":"Taro Hoge","email":"hoge@example.com","password":{"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/password?username=hoge","rel":"edit"}},"active":true,"attributes":{"attributes":[],"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/attribute?username=hoge","rel":"self"}}}"""


  val validUser = User("hoge", "Taro", "Hoge", "Taro Hoge", "hoge@example.com", true)
  val validSuccess = AuthenticationResult.Success(validUser)

  def e4 =
    "response"           | "result"             |
    (200, validUserJson) ! validSuccess.success |
    (400, failureJson)   ! failureObj.success   |
    (401, "hoge")        ! Unauthorized.failure |
    (403, "hoge")        ! Forbidden.failure    |> { (response, result) =>
      parseResponse(response) must equalTo(result)
    }

  def e5 =
    "statusCode" | "reulst"             |
    201          ! UnknownError.failure |
    202          ! UnknownError.failure |
    203          ! UnknownError.failure |
    402          ! UnknownError.failure |
    404          ! UnknownError.failure |
    405          ! UnknownError.failure |
    500          ! UnknownError.failure |
    503          ! UnknownError.failure |> { (code, result) =>
      parseResponse(code -> "hoge") must equalTo(result)
    }
}
