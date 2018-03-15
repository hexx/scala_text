package controllers

import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.Environment
import play.api.i18n.DefaultLangs
import play.api.i18n.DefaultMessagesApi
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

class HelloControllerSpec extends PlaySpec {

  val messagesApi = new DefaultMessagesApi(Environment.simple(), Configuration.reference, new DefaultLangs(Configuration.reference))

  def controller = new HelloController(messagesApi)

  "get" should {
    "クエリーパラメータがある場合は「○○さん、こんにちは！」というレスポンスを返す" in {
      val name = "namae"
      val action = controller.get(Some(name))
      val result = action(FakeRequest())
      assert(status(result) === 200)
      assert(contentAsString(result) === s"${name}さん、こんにちは！")
    }

    "クエリーパラメータがない場合は「名前をnameというクエリパラメータで与えてください」というレスポンスを返す" in {
      val result = controller.get(None)(FakeRequest())
      assert(status(result) === 200)
      assert(contentAsString(result) === s"名前をnameというクエリパラメータで与えてください")
    }
  }
}
