package infrastructure.service

import javax.inject.Inject
import com.rabbitmq.client.ConnectionFactory
import domain.entity.OriginalPicture
import domain.exception.ConversionFailureException
import domain.service.ConvertPictureService
import infrastructure.rabbitmq.RabbitMQConfiguration
import scala.concurrent.Future
import scala.pickling.Defaults._
import scala.pickling.binary._
import scala.util.Failure
import scala.util.Try
import scala.util.control.NonFatal

class ConvertPictureServiceImpl @Inject() (
  rabbitMQConfiguration: RabbitMQConfiguration
) extends ConvertPictureService {

  val factory = new ConnectionFactory()
  factory.setHost(rabbitMQConfiguration.HostName)

  def convert(original: OriginalPicture): Future[Unit] =
    Future.fromTry(Try {
      val connection = factory.newConnection()
      val channel = connection.createChannel()
      channel.queueDeclare(rabbitMQConfiguration.OriginalPictureQueueName, false, false, false, null)
      channel.basicPublish("", rabbitMQConfiguration.OriginalPictureQueueName, null, original.pickle.value)
    }.recoverWith {
      case NonFatal(e) => Failure(ConversionFailureException(s"It failed to send a picture to RabbitMQ. PictureId: ${original.id.value}", e))
    })
}
