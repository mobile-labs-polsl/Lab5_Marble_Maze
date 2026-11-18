package pl.polsl.mt.marblemaze.ui

import java.util.Locale

fun formatTenths(tenths: Long): String = String.format(Locale.US, "%d.%d s", tenths / 10, tenths % 10)

fun formatMs(ms: Long?): String = if (ms == null) "—" else formatTenths(ms / 100)
