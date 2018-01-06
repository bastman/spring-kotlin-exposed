package com.example.util.exposed

import org.joda.time.DateTime as JodaDateTime
import java.time.Instant as JavaInstant

fun JodaDateTime.toInstantJava() = JavaInstant.ofEpochMilli(this.millis)
fun JavaInstant.toJodaDateTime() = JodaDateTime(this.toEpochMilli())