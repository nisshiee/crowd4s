package org.nisshiee.crowd4s

sealed trait AuthorizationResult extends Any

object AuthorizationResult {

  import AuthenticationResult.{ Failure => AtFailure }

  case class Success(user: User, group: Group) extends AuthorizationResult
  case class AuthenticationFailure(fail: AtFailure) extends AnyVal with AuthorizationResult
  case class AuthorizationFailure(user: User) extends AnyVal with AuthorizationResult
}
