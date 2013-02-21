package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.control.Exception.allCatch

trait Authorize {

  import Authorize._

  def authorize
    (username: String, password: Password)
    (implicit authorizedGroup: AuthorizedGroup, conn: CrowdConnection, http: CrowdHttp) =
      Crowd.authenticate(username, password) flatMap checkAuthenticationResult(authorizedGroup)
}

object Authorize {

  import AuthenticationResult.{ Success => AtSuccess, Failure => AtFailure }
  import AuthorizationResult.{ Success => ArSuccess }
  import AuthorizationResult.{ AuthenticationFailure => AAFailure }
  import AuthorizationResult.{ AuthorizationFailure => ArFailure }

  def checkAuthenticationResult
      (authorized: AuthorizedGroup)
      (res: AuthenticationResult)
      (implicit conn: CrowdConnection, http: CrowdHttp)
      : Validation[CrowdError, AuthorizationResult] = res match {
        case AtSuccess(u) => Crowd.getDirectGroupList(u.name) flatMap { belonged =>
          searchAuthorizedGroup(authorized, belonged)(u)
        }
        case e: AtFailure => AAFailure(e).success
      }

  def searchAuthorizedGroup
      (authorized: AuthorizedGroup, belonged: Seq[String])
      (user: User)
      (implicit conn: CrowdConnection, http: CrowdHttp)
      : Validation[CrowdError, AuthorizationResult] =
        authorized.names.toStream filter (belonged contains _) map checkActive collectFirst {
          case Success(Some(g)) => ArSuccess(user, g).success
          case Failure(e) => e.failure
        } getOrElse ArFailure(user).success

  def checkActive(groupname: String)(implicit conn: CrowdConnection, http: CrowdHttp) =
    Crowd.getGroup(groupname) map { g => g.active option g }
}
