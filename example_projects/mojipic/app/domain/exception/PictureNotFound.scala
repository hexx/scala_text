package domain.exception

case class PictureNotFoundException(message: String = null, cause: Throwable = null)
  extends DomainException(message, cause)
