package net.ntworld.mergeRequestIntegration.util

import net.ntworld.mergeRequest.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import org.ocpsoft.prettytime.PrettyTime

object DateTimeUtil {
    private val timezone = TimeZone.getTimeZone("UTC")
    private val toStringDateFormat by lazy {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm")
        df.timeZone = timezone
        df
    }
    private val convertDateFormat by lazy {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        df.timeZone = timezone
        df
    }
    private val parser = ISODateTimeFormat.dateTimeParser()
    private val localTimeZone = DateTimeZone.forID(ZoneId.systemDefault().id)
    private val prettyTime = PrettyTime()

    @Synchronized
    fun fromDate(date: Date): DateTime = convertDateFormat.format(date)

    @Synchronized
    fun toDate(datetime: DateTime): Date {
        return parser.parseDateTime(datetime).withZone(localTimeZone).toDate()
    }

    @Synchronized
    fun formatDate(date: Date): String = toStringDateFormat.format(date) ?: ""

    @Synchronized
    fun toPretty(date: Date): String {
        return prettyTime.format(date)
    }

    @Synchronized
    fun toPretty(datetime: DateTime): String {
        return prettyTime.format(toDate(datetime))
    }
}