import java.time.Clock

import com.google.inject.AbstractModule
import domain.repository.ConvertedPictureRepository
import domain.repository.PicturePropertyRepository
import domain.service.ConvertPictureService
import infrastructure.repository.ConvertedPictureRepositoryImpl
import infrastructure.repository.PicturePropertyRepositoryImpl
import infrastructure.service.ConvertPictureServiceImpl

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[ConvertedPictureRepository]).to(classOf[ConvertedPictureRepositoryImpl])
    bind(classOf[PicturePropertyRepository]).to(classOf[PicturePropertyRepositoryImpl])
    bind(classOf[ConvertPictureService]).to(classOf[ConvertPictureServiceImpl])
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
  }
}
