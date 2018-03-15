package controllers

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import com.google.common.net.MediaType
import domain.entity.PictureId
import domain.entity.PictureProperty
import domain.entity.TwitterId
import domain.exception.DatabaseException
import domain.service.GetPicturePropertiesService
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class UsersControllerSpec extends PlaySpec with MockitoSugar {

  trait Setup {
    val pictureId = PictureId(123L)
    val twitterId = TwitterId(456L)
    val fileName = "randy.jpg"
    val mediaType = MediaType.parse("image/jpeg")
    val overlayText = "Randy"
    val overlayTextSize = 25
    val clock = Clock.fixed(Instant.parse("1991-03-31T00:00:00Z"), ZoneId.of("Asia/Tokyo"))
    val propertyValue =
      PictureProperty.Value(
        PictureProperty.Status.Converting,
        twitterId,
        fileName,
        mediaType,
        overlayText,
        overlayTextSize,
        LocalDateTime.now(clock))
    val property = PictureProperty(pictureId, propertyValue)

    val mockedGetPicturePropertiesService = mock[GetPicturePropertiesService]

    val sut = new UsersController(mockedGetPicturePropertiesService, ExecutionContext.global)
  }

  "PropertiesControllerSpec#getAll" should {
    "return picture properties" in new Setup {
      val localDateTime = LocalDateTime.now(clock)
      when(mockedGetPicturePropertiesService.getAllByTwitterId(twitterId, localDateTime)).thenReturn(Future.successful(Seq(property)))
      val expected = Json.parse("""[{"id":"123","value":{"status":"Converting","twitterId":"456","fileName":"randy.jpg","contentType":"image/jpeg","overlayText":"Randy","overlayTextSize":25,"createdTime":"1991-03-31T09:00:00"}}]""")
      val actual = sut.getProperties(twitterId.value, Some(localDateTime.toString))(FakeRequest())
      assert(contentAsJson(actual) === expected)
      assert(contentType(actual) === Some("application/json"))
      verify(mockedGetPicturePropertiesService, times(1)).getAllByTwitterId(twitterId, localDateTime)
    }

    "return picture properties even if a last created time is not specified " in new Setup {
      when(mockedGetPicturePropertiesService.getAllByTwitterId(twitterId, LocalDateTime.parse("0000-01-01T00:00:00"))).thenReturn(Future.successful(Seq(property)))
      val expected = Json.parse("""[{"id":"123","value":{"status":"Converting","twitterId":"456","fileName":"randy.jpg","contentType":"image/jpeg","overlayText":"Randy","overlayTextSize":25,"createdTime":"1991-03-31T09:00:00"}}]""")
      val actual = sut.getProperties(twitterId.value, None)(FakeRequest())
      assert(contentAsJson(actual) === expected)
      assert(contentType(actual) === Some("application/json"))
      verify(mockedGetPicturePropertiesService, times(1)).getAllByTwitterId(twitterId, LocalDateTime.parse("0000-01-01T00:00:00"))
    }

    "return InternalServerError if GetPicturePropertiesService returns DatabaseException" in new Setup {
      val localDateTime = LocalDateTime.now(clock)
      when(mockedGetPicturePropertiesService.getAllByTwitterId(twitterId, localDateTime)).thenReturn(Future.failed(DatabaseException("")))
      val actual = sut.getProperties(twitterId.value, Some(localDateTime.toString))(FakeRequest())
      assert(status(actual) === 500)
      verify(mockedGetPicturePropertiesService, times(1)).getAllByTwitterId(twitterId, localDateTime)
    }
  }
}

