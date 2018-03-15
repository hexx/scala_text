package controllers

import javax.inject.Inject
import infrastructure.twitter.TwitterAuthenticator
import infrastructure.twitter.TwitterException
import play.api.Configuration
import play.api.cache.CacheApi
import scala.concurrent.duration._

class OAuthController @Inject() (
  twitterAuthenticator: TwitterAuthenticator,
  configuration: Configuration,
  val cache: CacheApi
) extends TwitterLoginController {

  val documentRootUrl = configuration.getString("mojipic.documentrooturl").getOrElse(
    throw new IllegalStateException("mojipic.documentrooturl is not set.")
  )

  def login = TwitterLoginAction { request =>
    try {
      val callbackUrl = documentRootUrl + routes.OAuthController.oauthCallback(None).url
      val authenticationUrl = twitterAuthenticator.startAuthentication(request.sessionId, callbackUrl)
      Redirect(authenticationUrl)
    } catch {
      case e: TwitterException => BadRequest(e.message)
    }
  }

  def oauthCallback(verifierOpt: Option[String]) = TwitterLoginAction { request =>
    try {
      verifierOpt.map(twitterAuthenticator.getAccessToken(request.sessionId, _)) match {
        case Some(accessToken) =>
          cache.set(request.sessionId, accessToken, 30.minutes)
          Redirect(documentRootUrl + routes.EntryPointController.index().url)
        case None => BadRequest(s"Could not get OAuth verifier. SessionId: ${request.sessionId}")
      }
    } catch {
      case e: TwitterException => BadRequest(e.message)
    }
  }

  def logout = TwitterLoginAction { request =>
    cache.remove(request.sessionId)
    Redirect(documentRootUrl + routes.EntryPointController.index().url)
  }
}
