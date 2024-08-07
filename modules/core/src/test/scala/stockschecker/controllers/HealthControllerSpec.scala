package stockschecker.controllers

import cats.effect.IO
import kirill5k.common.http4s.test.HttpRoutesWordSpec
import kirill5k.common.cats.Clock
import org.http4s.*
import org.http4s.implicits.*

import java.time.Instant
import scala.concurrent.duration.*

class HealthControllerSpec extends HttpRoutesWordSpec {

  val ipAddress = "127.0.0.1"
  val ts = Instant.parse("2020-01-01T00:00:00Z")

  given clock: Clock[IO] = Clock.mock[IO](ts)

  "A HealthController" when {
    "GET /health/status" should {
      "return status of the app" in {
        val controller = HealthController[IO]("stocks-checker", ts, ipAddress, Some("v0.0.1"))

        val response = for
          _ <- clock.sleep(1.day + 2.hours + 30.minutes + 10.seconds)
          req = Request[IO](uri = uri"/health/status", method = Method.GET)
          res <- controller.routes.orNotFound.run(req)
        yield res

        val responseBody =
          s"""{
             |"service": "stocks-checker",
             |"startupTime": "$ts",
             |"appVersion": "v0.0.1",
             |"upTime": "1d2h30m10s",
             |"serverIpAddress": "$ipAddress"
             |}""".stripMargin
        response mustHaveStatus(Status.Ok, Some(responseBody))
      }
    }
    
  }
}
