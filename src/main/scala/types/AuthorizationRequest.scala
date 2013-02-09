package org.nisshiee.crowd4s

case class AuthorizationRequest (
   username: String
  ,authorizedGroups: Traversable[String]
)
