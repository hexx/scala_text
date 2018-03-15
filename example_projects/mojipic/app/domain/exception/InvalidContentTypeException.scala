package domain.exception

case class InvalidContentTypeException(message: String = null, cause: Throwable = null)
  extends DomainException(message, cause)
