package controllers

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._
import twitter4j.auth.AccessToken

class EntryPointControllerSpec extends PlaySpec with MockitoSugar {

  trait Setup {
    val sessionId = "abc"
    val sessionIdName = "mojipic.sessionId"
    val screenName = "Randy"
    val twitterId = 123L
    val cookie = Cookie(sessionIdName, sessionId)

    val mockedAccessToken = mock[AccessToken]
    when(mockedAccessToken.getScreenName).thenReturn(screenName)
    when(mockedAccessToken.getUserId).thenReturn(twitterId)

    val mockedCacheApi = mock[CacheApi]

    val sut = new EntryPointController(mockedCacheApi)
  }

  "EntryPointController#index" should {
    "show an unauthenticated page if SessionId is not received" in new Setup {
      val actual = sut.index()(FakeRequest())
      assert(contentAsString(actual) === views.html.index(None).toString())
      assert(cookies(actual).get(sessionIdName).isDefined === true)
    }

    "show an unauthenticated page if it is not logged in by Twitter" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(None)
      val actual = sut.index()(FakeRequest().withCookies(cookie))
      assert(contentAsString(actual) === views.html.index(None).toString())
      assert(cookies(actual).get(sessionIdName).get.value === sessionId)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
    }

    "show an authenticated page if SessionId is not received" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(Some(mockedAccessToken))
      val actual = sut.index()(FakeRequest().withCookies(cookie))
      assert(contentAsString(actual) === views.html.index(Some(mockedAccessToken)).toString())
      assert(cookies(actual).get(sessionIdName).get.value === sessionId)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
    }
  }
}
