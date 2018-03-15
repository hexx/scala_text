package controllers

import javax.inject.Inject
import play.api.cache.CacheApi

class EntryPointController @Inject() (
  val cache: CacheApi
) extends TwitterLoginController {
  def index = TwitterLoginAction { request =>
    Ok(views.html.index(request.accessToken))
  }
}
