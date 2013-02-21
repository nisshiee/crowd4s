package org.nisshiee.crowd4s

trait DefaultEnv {

  implicit def http: CrowdHttp = CrowdHttp
}
