package controllers

import java.nio.file.Files
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import com.google.common.net.MediaType
import domain.entity.ConvertedPicture
import domain.entity.PictureId
import domain.entity.PictureProperty
import domain.entity.TwitterId
import domain.exception.ConversionFailureException
import domain.exception.ConvertingException
import domain.exception.DatabaseException
import domain.exception.InvalidContentTypeException
import domain.exception.PictureNotFoundException
import domain.service.GetPictureService
import domain.service.PostPictureService
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import play.api.libs.Files.TemporaryFile
import play.api.mvc.Cookie
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.FakeRequest
import play.api.test.Helpers._
import twitter4j.auth.AccessToken
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PicturesControllerSpec extends PlaySpec with MockitoSugar {

  trait Setup {
    val pictureId = PictureId(123L)
    val twitterId = TwitterId(456L)
    val sessionId = "abc"
    val sessionIdName = "mojipic.sessionId"
    val cookie = Cookie(sessionIdName, sessionId)
    val clock = Clock.fixed(Instant.parse("1991-03-31T00:00:00Z"), ZoneId.of("Asia/Tokyo"))
    val fileName = "randy.jpg"
    val temporaryFile = Files.createTempFile("mojipic", ".jpg")
    val mediaType = MediaType.parse("image/jpeg")
    val filePart = FilePart("file", fileName, Some(mediaType.toString), TemporaryFile(temporaryFile.toFile))
    val overlayText = "Randy"
    val overlayTextSize = 25
    val dataParts = Map("overlaytext" -> Seq(overlayText), "overlaytextsize" -> Seq(overlayTextSize.toString))
    val multipartFormData = MultipartFormData(dataParts, Seq(filePart), Nil)
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
    val convertedBinary = "hogeika".getBytes
    val convertedPicture = ConvertedPicture(pictureId, convertedBinary)

    val mockedAccessToken = mock[AccessToken]
    when(mockedAccessToken.getUserId).thenReturn(twitterId.value)

    val mockedPostPictureService = mock[PostPictureService]
    val mockedGetPictureService = mock[GetPictureService]
    val mockedCacheApi = mock[CacheApi]

    val sut = new PicturesController(mockedPostPictureService, mockedGetPictureService, clock, ExecutionContext.global, mockedCacheApi)
  }

  "PicturesControllerSpec#post" should {
    "return OK" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(Some(mockedAccessToken))
      when(mockedPostPictureService.post(Array(), propertyValue)).thenReturn(Future.successful(()))
      val request = FakeRequest().withCookies(cookie).withMultipartFormDataBody(multipartFormData)
      val actual = sut.post()(request)
      assert(status(actual) === 200)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
      verify(mockedPostPictureService, times(1)).post(Array(), propertyValue)
    }

    "return Unauthorized if the user does not logged in" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(None)
      val request = FakeRequest().withCookies(cookie).withMultipartFormDataBody(multipartFormData)
      val actual = sut.post()(request)
      assert(status(actual) === 401)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
    }

    "return BadRequest if a body is not found" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(Some(mockedAccessToken))
      val request = FakeRequest().withCookies(cookie)
      val actual = sut.post()(request)
      assert(status(actual) === 400)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
    }

    "return BadRequest if a file parameter is not found" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(Some(mockedAccessToken))
      val request = FakeRequest().withCookies(cookie).withMultipartFormDataBody(multipartFormData.copy(files = Nil))
      val actual = sut.post()(request)
      assert(status(actual) === 400)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
    }

    "return BadRequest if Content-Type is invalid" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(Some(mockedAccessToken))
      when(mockedPostPictureService.post(Array(), propertyValue)).thenReturn(Future.failed(InvalidContentTypeException("")))
      val request = FakeRequest().withCookies(cookie).withMultipartFormDataBody(multipartFormData)
      val actual = sut.post()(request)
      assert(status(actual) === 400)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
      verify(mockedPostPictureService, times(1)).post(Array(), propertyValue)
    }

    "return InternalServerError if PostPictureService returns DatabaseException" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(Some(mockedAccessToken))
      when(mockedPostPictureService.post(Array(), propertyValue)).thenReturn(Future.failed(DatabaseException("")))
      val request = FakeRequest().withCookies(cookie).withMultipartFormDataBody(multipartFormData)
      val actual = sut.post()(request)
      assert(status(actual) === 500)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
      verify(mockedPostPictureService, times(1)).post(Array(), propertyValue)
    }

    "return InternalServerError if PostPictureService returns ConversionFailureException" in new Setup {
      when(mockedCacheApi.get[AccessToken](sessionId)).thenReturn(Some(mockedAccessToken))
      when(mockedPostPictureService.post(Array(), propertyValue)).thenReturn(Future.failed(ConversionFailureException("")))
      val request = FakeRequest().withCookies(cookie).withMultipartFormDataBody(multipartFormData)
      val actual = sut.post()(request)
      assert(status(actual) === 500)
      verify(mockedCacheApi, times(1)).get[AccessToken](sessionId)
      verify(mockedPostPictureService, times(1)).post(Array(), propertyValue)
    }
  }

  "PicturesControllerSpec#get" should {
    "return a picture" in new Setup {
      when(mockedGetPictureService.get(pictureId)).thenReturn(Future.successful((convertedPicture, property)))
      val actual = sut.get(pictureId.value)(FakeRequest())
      assert(contentAsBytes(actual) === convertedBinary)
      assert(contentType(actual) === Some(mediaType.toString))
      verify(mockedGetPictureService, times(1)).get(pictureId)
    }

    "return NotFound if a picture is not found" in new Setup {
      when(mockedGetPictureService.get(pictureId)).thenReturn(Future.failed(PictureNotFoundException("")))
      val actual = sut.get(pictureId.value)(FakeRequest())
      assert(status(actual) === 404)
      verify(mockedGetPictureService, times(1)).get(pictureId)
    }

    "return BadRequest if it failed to convert a picture" in new Setup {
      when(mockedGetPictureService.get(pictureId)).thenReturn(Future.failed(ConversionFailureException("")))
      val actual = sut.get(pictureId.value)(FakeRequest())
      assert(status(actual) === 400)
      verify(mockedGetPictureService, times(1)).get(pictureId)
    }

    "return BadRequest if it is converting a picture" in new Setup {
      when(mockedGetPictureService.get(pictureId)).thenReturn(Future.failed(ConvertingException("")))
      val actual = sut.get(pictureId.value)(FakeRequest())
      assert(status(actual) === 400)
      verify(mockedGetPictureService, times(1)).get(pictureId)
    }
  }
}
