package controllers

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Named, Inject, Singleton}

import akka.util.Timeout
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.data._
import play.api.data.Forms._
import service.ImageInjestService
import play.api.libs.concurrent.Execution.Implicits._

case class DisplayRequest(name: UUID, size: Option[String])
case class DownloadRequest(url: String, size: Option[String])

@Singleton
class GifController @Inject()(injestService: ImageInjestService) extends Controller {
  val GIFHTML = "<script type='text/javascript' src=\"/assets/javascript/render.js\"></script><div id=\"gifBody\"></div><div id=\"gifData\" hidden>%PLACEHOLDER%</div>"

  implicit val timeout = Timeout(10000, TimeUnit.MILLISECONDS)

  val displayRequestForm: Form[DisplayRequest] = Form(
    mapping(
      "id" -> uuid,
      "size" -> optional(text)
    )(DisplayRequest.apply)(DisplayRequest.unapply)
  )

  val downloadRequestForm: Form[DownloadRequest] = Form(
    mapping(
      "url" -> text,
      "size" -> optional(text)
    )(DownloadRequest.apply)(DownloadRequest.unapply))

  def index = Action {
    Ok(views.html.download(downloadRequestForm))
  }

  def downloadForm = Action {
    Ok(views.html.download(downloadRequestForm))
  }

  def handleDownloadRequest = Action.async(parse.form(downloadRequestForm)) { request =>
    val downloadRequest = request.body
    injestService.injest(request.body).map { frames =>
      Ok(views.html.gif(Json.toJson(frames.map(_.map(line => s"<span>$line</span><br/>").mkString)).toString))
    }
  }
}
