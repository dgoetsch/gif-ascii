package request

object ImageSize {
  final val EXTRA_SMALL = 10
  final val SMALL = 25
  final val MEDIUM = 50
  final val LARGE = 100
  final val EXTRA_LARGE = 200

  final val EXTRA_SMALL_KEY = "extraSmall"
  final val SMALL_KEY = "small"
  final val MEDIUM_KEY = "medium"
  final val LARGE_KEY = "large"
  final val EXTRA_LARGE_KEY = "extraLarge"

  sealed trait ImageSizeDefinition {
    val key: String
    val value: Int
  }
  final object ExtraSmallImage extends ImageSizeDefinition {
    override val key = EXTRA_SMALL_KEY
    override val value = EXTRA_SMALL
  }
  final object SmallImage extends ImageSizeDefinition {
    override val key = SMALL_KEY
    override val value = SMALL
  }
  final object MediumImage extends ImageSizeDefinition {
    override val key = MEDIUM_KEY
    override val value = MEDIUM
  }
  final object LargeImage extends ImageSizeDefinition {
    override val key = LARGE_KEY
    override val value = LARGE
  }
  final object ExtraLargeImage extends ImageSizeDefinition {
    override val key = EXTRA_LARGE_KEY
    override val value = EXTRA_LARGE
  }

  final val sizeMap = Map(
    ExtraSmallImage.key -> ExtraSmallImage.value,
    SmallImage.key -> SmallImage.value,
    MediumImage.key -> MediumImage.value,
    LargeImage.key -> LargeImage.value,
    ExtraLargeImage.key -> ExtraLargeImage.value
  )

  final val largeModifiers = Seq("larg", "big")
  final val xtraModifiers = Seq("xtra", "supe", "more", "real")
  final val smallModifiers = Seq("smal", "peq", "lit")

  implicit class StringUtil(s: String) {
    def isLike(other: String) =
      s.toLowerCase.contains(other)

    def isLikeAnyOf(others: Seq[String]) =
      others.exists(other => s.isLike(other))

    def isLikeAllOf(others: Seq[String]) =
      others.forall(other => s.isLike(other))

    def isExtraSmall = s.isLikeAnyOf(smallModifiers) && s.isLikeAnyOf(xtraModifiers)
    def isSmall = s.isLikeAnyOf(smallModifiers)
    def isLarge = s.isLikeAnyOf(largeModifiers) && ! s.isLikeAnyOf(xtraModifiers)
    def isExtraLarge = s.isLikeAnyOf(largeModifiers) && s.isLikeAnyOf(xtraModifiers)

    def targetSize = s match {
      case extraSmall if extraSmall.isExtraSmall => EXTRA_SMALL
      case small if small.isSmall => SMALL
      case extraLarge if extraLarge.isExtraLarge => EXTRA_LARGE
      case large if large.isLarge => LARGE
      case _ => MEDIUM
    }

    def targetSizeKey = s match {
      case extraSmall if extraSmall.isExtraSmall => EXTRA_SMALL_KEY
      case small if small.isSmall => SMALL_KEY
      case extraLarge if extraLarge.isExtraLarge => EXTRA_LARGE_KEY
      case large if large.isLarge => LARGE_KEY
      case _ => MEDIUM_KEY
    }

    def targetDefinition: ImageSizeDefinition = s match {
      case extraSmall if extraSmall.isExtraSmall => ExtraSmallImage
      case small if small.isSmall => SmallImage
      case extraLarge if extraLarge.isExtraLarge => ExtraLargeImage
      case large if large.isLarge => LargeImage
      case _ => MediumImage
    }
  }
}
