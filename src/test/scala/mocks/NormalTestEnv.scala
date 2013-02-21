package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._

object NormalTestEnv extends DefaultEnv {

  implicit def conn = CrowdConnection("https://example.net/crowd", "appname", "apppass")

  implicit def testhttp(implicit c: Case): CrowdHttp = new CrowdHttp {

    def get
      (path: String, params: Map[String, String])
      (implicit conn: CrowdConnection)
      : Validation[ConnectionError, (Int, String)] = path match {

        // getUser
        case "/rest/usermanagement/1/user.json" => (for {
          username <- params.get("username") toSuccess (400 -> """{"reason":"ILLEGAL_ARGUMENT","message":"username query parameter must be given"}""")
          userjson <- c.users.get(username) toSuccess (404 -> s"""{"reason":"USER_NOT_FOUND","message":"User <${username}> does not exist"}""")
        } yield (200 -> userjson)).valueOr(identity).success

        // getGroup
        case "/rest/usermanagement/1/group.json" => (for {
          groupname <- params.get("groupname") toSuccess (400 -> """{"reason":"ILLEGAL_ARGUMENT","message":"groupname query parameter must be given"}""")
          groupjson <- c.groups.get(groupname) toSuccess (404 -> s"""{"reason":"GROUP_NOT_FOUND","message":"Group <${groupname}> does not exist"}""")
        } yield (200 -> groupjson)).valueOr(identity).success

        // getDirectGroupList
        case "/rest/usermanagement/1/user/group/direct.json" => (for {
          username <- params.get("username") toSuccess (400 -> """{"reason":"ILLEGAL_ARGUMENT","message":"username query parameter must be given"}""")
          groupListJson = c.directGroupLists.get(username) | """{"expand":"group","groups":[]}"""
        } yield (200 -> groupListJson)).valueOr(identity).success

        // getNestedGroupList
        case "/rest/usermanagement/1/user/group/nested.json" => (for {
          username <- params.get("username") toSuccess (400 -> """{"reason":"ILLEGAL_ARGUMENT","message":"username query parameter must be given"}""")
          groupListJson = c.nestedGroupLists.get(username) | """{"expand":"group","groups":[]}"""
        } yield (200 -> groupListJson)).valueOr(identity).success
      }

   
    def postJson
      (path: String, params: Map[String, String], body: JValue)
      (implicit conn: CrowdConnection)
      : Validation[ConnectionError, (Int, String)] = path match {

        // authenticate
        case "/rest/usermanagement/1/authentication.json" => (for {
          username <- params.get("username") toSuccess (400 -> """{"reason":"ILLEGAL_ARGUMENT","message":"username query parameter must be given"}""")
          _ <- c.inactiveUsers contains username option (400 -> s"""{"reason":"INACTIVE_ACCOUNT","message":"Account with name <$username> is inactive"}""") toFailure true
          password <- c.authentications.get(username) toSuccess (400 -> s"""{"reason":"USER_NOT_FOUND","message":"User <$username> does not exist"}""")
          result <- body match {
            case JObject(List(JField("value", JString(p)))) if p == password.value =>
              c.users.get(username) toSuccess (500 -> "Testcase ERROR")
            case JObject(List(JField("value", JString(_)))) =>
              (400 -> """{"reason":"INVALID_USER_AUTHENTICATION","message":"Failed to authenticate principal, password was invalid"}""").failure
            case _ => (500 -> "Unexpected JSON").failure
          }
        } yield (200 -> result)).valueOr(identity).success
      }
  }

  case class Case (
     users: Map[String, String]
    ,inactiveUsers: Set[String]
    ,groups: Map[String, String]
    ,directGroupLists: Map[String, String]
    ,nestedGroupLists: Map[String, String]
    ,authentications: Map[String, Password]
  )

  val case01 = Case (
    Map (
      "user01" -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/user?username=user01","rel":"self"},"name":"user01","first-name":"01","last-name":"User","display-name":"User01","email":"user01@example.net","password":{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/user/password?username=user01","rel":"edit"}},"active":true,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/user/attribute?username=user01","rel":"self"}}}"""
      ,"user90" -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/user?username=user90","rel":"self"},"name":"user90","first-name":"90","last-name":"User","display-name":"User90","email":"user90@example.net","password":{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/user/password?username=user90","rel":"edit"}},"active":false,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/user/attribute?username=user90","rel":"self"}}}"""
    )

    ,Set("user90")

    ,Map(
      "group01" -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group01","rel":"self"},"name":"group01","description":"The first group","type":"GROUP","active":true,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group/attribute?groupname=group01","rel":"self"}}}"""
      ,"group02" -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group02","rel":"self"},"name":"group02","description":"The second group","type":"GROUP","active":true,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group/attribute?groupname=group02","rel":"self"}}}"""
      ,"group03" -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group03","rel":"self"},"name":"group03","description":"The third group","type":"GROUP","active":true,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group/attribute?groupname=group03","rel":"self"}}}"""
      ,"group04" -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group04","rel":"self"},"name":"group04","description":"The forth group","type":"GROUP","active":true,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group/attribute?groupname=group04","rel":"self"}}}"""
      ,"group90" -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group90","rel":"self"},"name":"group90","description":"The inactive group","type":"GROUP","active":false,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group/attribute?groupname=group90","rel":"self"}}}"""
    )

    ,Map(
      "user01" -> """{"expand":"group","groups":[{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group01","rel":"self"},"name":"group01"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group02","rel":"self"},"name":"group02"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group90","rel":"self"},"name":"group90"}]}"""
    )

    ,Map(
      "user01" -> """{"expand":"group","groups":[{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group01","rel":"self"},"name":"group01"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group02","rel":"self"},"name":"group02"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group03","rel":"self"},"name":"group03"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group90","rel":"self"},"name":"group90"}]}"""
    )

    ,Map(
       "user01" -> "pass01"
      ,"user90" -> "pass90"
    )
  )
}
