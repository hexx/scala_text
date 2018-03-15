package controllers

import infrastructure.twitter.TwitterAuthenticator
import infrastructure.twitter.TwitterException
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._
import twitter4j.auth.AccessToken
import scala.concurrent.duration._

class OAuthControllerSpec extends PlaySpec with MockitoSugar {

  trait Setup {
    val sessionId = "abc"
    val sessionIdName = "mojipic.sessionId"
    val screenName = "Randy"
    val twitterId = 123L
    val cookie = Cookie(sessionIdName, sessionId)
    val documentRootUrl = "http://okumin.com"
    val authenticationUrl = "http://pakumori.net"
    val callbackUrl = documentRootUrl + routes.OAuthController.oauthCallback(None).url
    val verifier = "fake verifier"

    val mockedAccessToken = mock[AccessToken]
    when(mockedAccessToken.getScreenName).thenReturn(screenName)
    when(mockedAccessToken.getUserId).thenReturn(twitterId)

    val mockedTwitterAuthenticator = mock[TwitterAuthenticator]
    val mockedCacheApi = mock[CacheApi]

    val mockedConfiguration = mock[Configuration]
    when(mockedConfiguration.getString("mojipic.documentrooturl")).thenReturn(Some(documentRootUrl))

    val sut = new OAuthController(mockedTwitterAuthenticator, mockedConfiguration, mockedCacheApi)
  }

  "OAuthController#login" should {
    "redirect to the Twitter authentication URL" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(None)
      when(mockedTwitterAuthenticator.startAuthentication(sessionId, callbackUrl)).thenReturn(authenticationUrl)
      val actual = sut.login()(FakeRequest().withCookies(cookie))
      assert(status(actual) == 303)
      assert(redirectLocation(actual) === Some(authenticationUrl))
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
      verify(mockedTwitterAuthenticator, times(1)).startAuthentication(sessionId, callbackUrl)
    }

    "return BadRequest if Twitter authentication failed" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(None)
      when(mockedTwitterAuthenticator.startAuthentication(sessionId, callbackUrl)).thenThrow(new TwitterException(""))
      val actual = sut.login()(FakeRequest().withCookies(cookie))
      assert(status(actual) == 400)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
      verify(mockedTwitterAuthenticator, times(1)).startAuthentication(sessionId, callbackUrl)
    }
  }

  "OAuthController#oauthCallback" should {
    "redirect to the root page" in new Setup {
      when(mockedTwitterAuthenticator.getAccessToken(sessionId, verifier)).thenReturn(mockedAccessToken)
      val actual = sut.oauthCallback(Some(verifier))(FakeRequest().withCookies(cookie))
      assert(status(actual) == 303)
      assert(redirectLocation(actual) === Some(documentRootUrl + "/"))
      verify(mockedTwitterAuthenticator, times(1)).getAccessToken(sessionId, verifier)
      verify(mockedCacheApi, times(1)).set(sessionId, mockedAccessToken, 30.minutes)
    }

    "return BadRequest if OAuth verifier is not passed" in new Setup {
      val actual = sut.oauthCallback(None)(FakeRequest().withCookies(cookie))
      assert(status(actual) == 400)
    }

    "return BadRequest if Twitter authentication failed" in new Setup {
      when(mockedTwitterAuthenticator.getAccessToken(sessionId, verifier)).thenThrow(new TwitterException(""))
      val actual = sut.oauthCallback(Some(verifier))(FakeRequest().withCookies(cookie))
      assert(status(actual) == 400)
      verify(mockedTwitterAuthenticator, times(1)).getAccessToken(sessionId, verifier)
    }
  }

  "OAuthController#logout" should {
    "show an unauthenticated page and session is deleted" in new Setup {
      val actual = sut.logout()(FakeRequest().withCookies(cookie))
      assert(status(actual) == 303)
      assert(redirectLocation(actual) === Some(documentRootUrl + "/"))
      verify(mockedCacheApi, times(1)).remove(sessionId)
    }
  }
}
