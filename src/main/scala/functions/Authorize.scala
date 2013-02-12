package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.control.Exception.allCatch

trait Authorize {

  import Authorize._

  def authorize
    (username: String, password: Password)
    (implicit authorizedGroups: Seq[String], conn: CrowdConnection) =
      Crowd.authenticate(username, password) flatMap checkAuthenticationResult(authorizedGroups)
}

object Authorize {

  import AuthenticationResult.{ Success => AtSuccess, Failure => AtFailure }
  import AuthorizationResult.{ Success => ArSuccess }
  import AuthorizationResult.{ AuthenticationFailure => AAFailure }
  import AuthorizationResult.{ AuthorizationFailure => ArFailure }

  def checkAuthenticationResult
      (authorized: Seq[String])
      (res: AuthenticationResult)
      (implicit conn: CrowdConnection)
      : Validation[CrowdError, AuthorizationResult] = res match {
        case AtSuccess(u) => Crowd.getDirectGroupList(u.name) flatMap { belonged =>
          searchAuthorizedGroup(authorized, belonged)(u)
        }
        case e: AtFailure => AAFailure(e).success
      }

  def searchAuthorizedGroup
      (authorized: Seq[String], belonged: Seq[String])
      (user: User)
      (implicit conn: CrowdConnection)
      : Validation[CrowdError, AuthorizationResult] =
        authorized.toStream filter (belonged contains _) map checkActive collectFirst {
          case Success(Some(g)) => ArSuccess(user, g).success
          case Failure(e) => e.failure
        } getOrElse ArFailure(user).success

  def checkActive(groupname: String)(implicit conn: CrowdConnection) =
    Crowd.getGroup(groupname) map { g => g.active option g }
}
