package domain.exception

case class DatabaseException(message: String = null, cause: Throwable = null)
  extends DomainException(message, cause)
