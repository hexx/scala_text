package controllers

import java.util.UUID
import play.api.cache.CacheApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.ActionBuilder
import play.api.mvc.Controller
import play.api.mvc.Cookie
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.WrappedRequest
import twitter4j.auth.AccessToken
import scala.concurrent.Future

case class TwitterLoginRequest[A](sessionId: String, accessToken: Option[AccessToken], request: Request[A]) extends WrappedRequest[A](request)

trait TwitterLoginController extends Controller {
  val cache: CacheApi

  val sessionIdName = "mojipic.sessionId"

  def TwitterLoginAction = new ActionBuilder[TwitterLoginRequest] {
    def invokeBlock[A](request: Request[A], block: TwitterLoginRequest[A] => Future[Result]) = {
      val sessionIdOpt = request.cookies.get(sessionIdName).map(_.value)
      val accessToken = sessionIdOpt.flatMap(cache.get[AccessToken])
      val sessionId = sessionIdOpt.getOrElse(UUID.randomUUID().toString)
      val result = block(TwitterLoginRequest(sessionId, accessToken, request))
      result.map(_.withCookies(Cookie(sessionIdName, sessionId, Some(30 * 60))))
    }
  }
}
