package domain.exception

case class ConvertingException(message: String = null, cause: Throwable = null)
  extends DomainException(message, cause)
