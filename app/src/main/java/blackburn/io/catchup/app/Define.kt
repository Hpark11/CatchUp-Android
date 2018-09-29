package blackburn.io.catchup.app

object Define {
  const val VERSION_MAJOR = 1
  const val VERSION_MINOR = 0
  const val VERSION_REVISION = 0

  const val DYNAMODB_BATCHGET_LIMIT = 100
  const val PLATFORM_ANDROID = "android"

  const val APP_MARKET_URL = "https://play.google.com/store/apps/details?id=blackburn.io.audigo_android"

  const val DIFF_DAYS = (24 * 60 * 60 * 1000)
  const val DIFF_HOURS = (60 * 60 * 1000)
  const val DIFF_MINUTES = (60 * 1000)
}