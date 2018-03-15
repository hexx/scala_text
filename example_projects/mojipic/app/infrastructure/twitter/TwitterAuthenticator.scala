package infrastructure.twitter

import javax.inject.Inject
import play.api.Configuration
import play.api.cache.CacheApi
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import scala.concurrent.duration._
import scala.util.control.NonFatal

class TwitterAuthenticator @Inject() (
  configuration: Configuration,
  cache: CacheApi
) {

  val CacheKeyPrefixTwitter = "twitterInstance"

  val ConsumerKey = configuration.getString("mojipic.consumerkey")
    .getOrElse(throw new IllegalStateException("mojipic.consumerkey is not set."))
  val ConsumerSecret = configuration.getString("mojipic.consumersecret")
    .getOrElse(throw new IllegalStateException("mojipic.consumersecret is not set."))

  private[this] def cacheKeyTwitter(sessionId: String): String = CacheKeyPrefixTwitter + sessionId

  /**
   * Twitterの認証を開始する
   * @param sessionId Twitterの認証をしたいセッションID
   * @param callbackUrl コールバックURL
   * @return 投稿者に認証してもらうためのURL
   * @throws TwitterException 何らかの理由でTwitterの認証を開始できなかった
   */
  def startAuthentication(sessionId: String, callbackUrl: String): String =
    try {
      val twitter = new TwitterFactory().getInstance()
      twitter.setOAuthConsumer(
        ConsumerKey,
        ConsumerSecret
      )
      val requestToken = twitter.getOAuthRequestToken(callbackUrl)
      cache.set(cacheKeyTwitter(sessionId), twitter, 30.seconds)
      requestToken.getAuthenticationURL
    } catch {
      case NonFatal(e) =>
        throw TwitterException(s"Could not get a request token. SessionId: $sessionId", e)
    }

  /**
   * Twitterのアクセストークンを取得する
   * @param sessionId Twitterの認証をしたいセッションID
   * @param verifier OAuth Verifier
   * @return アクセストークン
   * @throws TwitterException 何らかの理由でTwitterのアクセストークンを取得できなかった
   */
  def getAccessToken(sessionId: String, verifier: String): AccessToken =
    try {
      cache.get[Twitter](cacheKeyTwitter(sessionId)).get.getOAuthAccessToken(verifier)
    } catch {
      case NonFatal(e) =>
        throw TwitterException(s"Could not get an access token. SessionId: $sessionId", e)
    }
}
