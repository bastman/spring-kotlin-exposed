package com.example.util.time

import java.time.Duration
import java.time.Instant

fun Instant.durationToNow(now: Instant = Instant.now()): Duration = Duration.between(this, now)
fun Instant.durationToNowInMillis(now: Instant = Instant.now()):Long = durationToNow(now).toMillis()