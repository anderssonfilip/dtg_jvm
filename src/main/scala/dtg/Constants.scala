package dtg

object Constants {
  
  val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
  calendar.set(java.util.Calendar.YEAR, 2013)
  calendar.set(java.util.Calendar.MONTH, java.util.Calendar.NOVEMBER)
  calendar.set(java.util.Calendar.DAY_OF_MONTH, 24)
  calendar.set(java.util.Calendar.HOUR_OF_DAY, 12)
  calendar.set(java.util.Calendar.MINUTE, 0)
  calendar.set(java.util.Calendar.SECOND, 0)
  calendar.set(java.util.Calendar.MILLISECOND, 0)
  
  val epoch = calendar.getTimeInMillis()
}