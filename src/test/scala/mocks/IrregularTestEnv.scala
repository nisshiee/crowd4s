package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._

object IrregularTestEnv extends DefaultEnv {

  implicit def conn = CrowdConnection("https://example.net/crowd", "appname", "apppass")

  implicit def testhttp(implicit c: Case): CrowdHttp = new CrowdHttp {

    def get
      (path: String, params: Map[String, String])
      (implicit conn: CrowdConnection)
      : Validation[ConnectionError, (Int, String)] = path match {

        // getUser
        case "/rest/usermanagement/1/user.json" =>
          if (c.errors contains GetUser)
            ConnectionError.failure
          else
            (200 -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/user?username=user01","rel":"self"},"name":"user01","first-name":"01","last-name":"User","display-name":"User01","email":"user01@example.net","password":{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/user/password?username=user01","rel":"edit"}},"active":true,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/user/attribute?username=user01","rel":"self"}}}""").success


        // getGroup
        case "/rest/usermanagement/1/group.json" =>
          if (c.errors contains GetGroup)
            ConnectionError.failure
          else
            (200 -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group01","rel":"self"},"name":"group01","description":"The first group","type":"GROUP","active":true,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group/attribute?groupname=group01","rel":"self"}}}""").success


        // getDirectGroupList
        case "/rest/usermanagement/1/user/group/direct.json" =>
          if (c.errors contains GetGroupList)
            ConnectionError.failure
          else
            (200 -> """{"expand":"group","groups":[{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group01","rel":"self"},"name":"group01"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group02","rel":"self"},"name":"group02"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group90","rel":"self"},"name":"group90"}]}""").success


        // getNestedGroupList
        case "/rest/usermanagement/1/user/group/nested.json" =>
          if (c.errors contains GetGroupList)
            ConnectionError.failure
          else
            (200 -> """{"expand":"group","groups":[{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group01","rel":"self"},"name":"group01"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group02","rel":"self"},"name":"group02"},{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/group?groupname=group90","rel":"self"},"name":"group90"}]}""").success


      }


    def postJson
      (path: String, params: Map[String, String], body: JValue)
      (implicit conn: CrowdConnection)
      : Validation[ConnectionError, (Int, String)] = path match {

        // authenticate
        case "/rest/usermanagement/1/authentication.json" =>
          if (c.errors contains Authenticate)
            ConnectionError.failure
          else
            (200 -> """{"expand":"attributes","link":{"href":"https://example.net/crowd/rest/usermanagement/1/user?username=user01","rel":"self"},"name":"user01","first-name":"01","last-name":"User","display-name":"User01","email":"user01@example.net","password":{"link":{"href":"https://example.net/crowd/rest/usermanagement/1/user/password?username=user01","rel":"edit"}},"active":true,"attributes":{"attributes":[],"link":{"href":"https://example.net/crowd/rest/usermanagement/1/user/attribute?username=user01","rel":"self"}}}""").success


      }
  }
          

  implicit class Case(val errors: Set[Func]) extends AnyVal

  val allError: Case = Set[Func](
     GetUser
    ,GetGroup
    ,GetGroupList
    ,Authenticate
  )

  sealed trait Func
  case object GetUser extends Func
  case object GetGroup extends Func
  case object GetGroupList extends Func
  case object Authenticate extends Func
}
