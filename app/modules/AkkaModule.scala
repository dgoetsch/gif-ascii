package modules

import actors.GifReader
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class AkkaModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[GifReader](GifReader.name)
  }
}