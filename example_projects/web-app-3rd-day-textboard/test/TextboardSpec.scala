import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.mvc._
import play.api.inject.guice._
import play.api.routing._
import play.api.routing.sird._

class TextboardSpec extends PlaySpec with GuiceOneServerPerTest with OneBrowserPerSuite with HtmlUnitFactory {

  override def fakeApplication() =
    new GuiceApplicationBuilder()
      .configure(
        "db.default.driver" -> "org.h2.Driver",
        "db.default.url" -> "jdbc:h2:mem:test;MODE=MYSQL")
      .build()

  "GET /" should {
    "何も投稿しない場合はメッセージを表示しない" in {
      go to s"http://localhost:$port/"
      assert(pageTitle === "Scala Text Textboard")
      assert(findAll(className("post-body")).length === 0)
    }
  }

  "POST /" should {
    "投稿したものが表示される" in {
      val body = "test post"

      go to s"http://localhost:$port/"
      textField(cssSelector("input#post")).value = body
      submit()

      eventually {
        val posts = findAll(className("post-body")).toSeq
        assert(posts.length === 1)
        assert(posts(0).text === body)
        assert(findAll(cssSelector("p#error")).length === 0)
      }
    }

    "空のメッセージは投稿できない" in {
      val body = ""

      go to s"http://localhost:$port/"
      textField(cssSelector("input#post")).value = body
      submit()

      eventually {
        val error = findAll(cssSelector("p#error")).toSeq
        assert(error.length === 1)
        assert(error(0).text === "Please enter a message.")
      }
    }

    "長すぎるメッセージは投稿できない" in {
      val body = "too long messages"

      go to s"http://localhost:$port/"
      textField(cssSelector("input#post")).value = body
      submit()

      eventually {
        val error = findAll(cssSelector("p#error")).toSeq
        assert(error.length === 1)
        assert(error(0).text === "The message is too long.")
      }
    }
  }
}
