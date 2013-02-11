package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.FieldSerializer._
import scala.util.control.Exception.allCatch

object ResponseParseHelper {

  def parseNotFound[A](json: String): Validation[RequestError, A] = {
    implicit val formats = DefaultFormats
    val err = allCatch opt {
      parse(json).extract[NotFound]
    } getOrElse UnknownError

    err.failure[A]
  }
}
