package computerdatabase

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  private val config = ConfigFactory.load
  private val baseUrl = config.getString("baseUrl").split(",")
  private val url = "/api/v1/hello"

  val httpConf: HttpProtocolBuilder = http
    .baseUrls(baseUrl.map(_.trim).toList)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling")

  val baseline: ScenarioBuilder = scenario("Baseline")
    .exec { session =>
      val requestBody: String = "{\"message\":\"hello\"}"

      val session2 = session
        .set("service_request", requestBody)
      session2
    }
    .exec(
      http("Baseline")
        .post(url)
        .header("Content-Type", "application/json")
        .body(StringBody("#{service_request}"))
    )

  setUp(
    baseline.inject(constantConcurrentUsers(10).during(10.minutes))
  ).protocols(httpConf)
}
