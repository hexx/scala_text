package infrastructure.twitter

case class TwitterException(message: String = null, cause: Throwable = null)
  extends RuntimeException(message, cause)
