package domain.exception

case class ConversionFailureException(message: String = null, cause: Throwable = null)
  extends DomainException(message, cause)
