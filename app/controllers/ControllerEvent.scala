package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import service.ServiceConsumer

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ControllerEvent @Inject()()(implicit val controllerComponents: ControllerComponents, serviceConsumer: ServiceConsumer, ec: ExecutionContext) extends BaseController {

  def average(eventType: String, from: Long, to: Long): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    serviceConsumer.average(eventType, from, to)
      .map { result =>
        Ok(Json.toJson(result))
      }
  }
}
