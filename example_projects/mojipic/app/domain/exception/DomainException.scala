package domain.exception

abstract class DomainException(message: String, cause: Throwable) extends RuntimeException(message, cause)
