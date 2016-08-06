package cassandra

import java.util.UUID

import play.api.libs.json.JsValue

case class ImageModel(id: UUID, extraLarge: JsValue, large: JsValue, medium: JsValue, small: JsValue, extraSmall: JsValue)

