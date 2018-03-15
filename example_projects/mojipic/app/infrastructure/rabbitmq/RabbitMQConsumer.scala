package infrastructure.rabbitmq

import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import akka.actor.ActorRef
import com.google.inject.AbstractModule
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.ShutdownSignalException
import domain.entity.OriginalPicture
import infrastructure.actor.ConvertPictureActor
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.pickling.Defaults._
import scala.pickling.binary._
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.control.NonFatal

/**
 * RabbitMQのキューを監視し続けるランナー
 */
@Singleton
class RabbitMQConsumer @Inject() (
  worker: RabbitMQConsumeWorker,
  applicationLifeCycle: ApplicationLifecycle
) {
  private[this] val executor = Executors.newFixedThreadPool(1)
  executor.submit(worker)
  applicationLifeCycle.addStopHook { () =>
    Future.successful(worker.close())
  }
}

class RabbitMQConsumeWorker @Inject() (
  rabbitMQConfiguration: RabbitMQConfiguration,
  @Named("convert-picture-actor") convertPictureActor: ActorRef
) extends Runnable {

  val factory = new ConnectionFactory()
  factory.setHost(rabbitMQConfiguration.HostName)
  val connection = factory.newConnection()
  val channel = connection.createChannel()

  override def run(): Unit = {
    channel.queueDeclare(rabbitMQConfiguration.OriginalPictureQueueName, false, false, false, null)
    val consumer = new QueueingConsumer(channel)
    channel.basicConsume(rabbitMQConfiguration.OriginalPictureQueueName, true, consumer)
    Logger.logger.info(s"RabbitMQの監視を開始しました スレッド名:${Thread.currentThread().getName}")
    messageLoop(consumer)
  }

  @tailrec private def messageLoop(consumer: QueueingConsumer): Unit = {
    Try(consumer.nextDelivery()) match {
      case Success(delivery) =>
        val original = BinaryPickle(delivery.getBody).unpickle[OriginalPicture]
        convertPictureActor ! ConvertPictureActor.Convert(original)
        messageLoop(consumer)
      case Failure(e: ShutdownSignalException) =>
        // channel が close されたときに発生するため、正常系の一部
        Logger.info("RabbitMQの監視を終了します")
      case Failure(NonFatal(e)) =>
        Logger.warn("RabbitMQの監視が中断されました", e)
    }
  }

  def close(): Unit = {
    println("closing...")
    channel.close()
    connection.close(5000)
  }
}

class RabbitMQConsumerModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[RabbitMQConsumer]).asEagerSingleton()
  }
}
