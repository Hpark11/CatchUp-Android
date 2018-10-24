package blackburn.io.catchup.app

object Define {
  const val VERSION_MAJOR = 1
  const val VERSION_MINOR = 0
  const val VERSION_REVISION = 6

  const val DYNAMODB_BATCHGET_LIMIT = 50
  const val PLATFORM_ANDROID = "android"

  const val APP_MARKET_URL = "https://play.google.com/store/apps/details?id="

  const val DIFF_DAYS = (24 * 60 * 60 * 1000)
  const val DIFF_HOURS = (60 * 60 * 1000)
  const val DIFF_MINUTES = (60 * 1000)

  const val ACTIVATE_PERIOD = 7200000

  const val DISTANCE_UPPERBOUND = 700000
  const val DISTANCE_LOWERBOUND = 250

  const val DEFAULT_LATITUDE = 0.0
  const val DEFAULT_LONGITUDE = 0.0

  const val FIELD_TITLE = "title"
  const val FIELD_PUSH_TOKENS= "pushTokens"
  const val FIELD_MESSAGE = "message"

  const val COLLECTION_MESSAGES = "messages"
}