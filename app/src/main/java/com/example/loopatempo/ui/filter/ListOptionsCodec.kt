package com.example.loopatempo.ui.filter

fun encodeListOptions(options: ListOptions): String =
    listOf(options.mainEnabled.toString(), options.main.name, options.sort.name).joinToString("|")

fun decodeListOptions(raw: String?, default: ListOptions): ListOptions {
    if (raw == null) return default
    val parts = raw.split("|")
    if (parts.size != 3) return default
    return ListOptions(
        mainEnabled = parts[0].toBooleanStrictOrNull() ?: default.mainEnabled,
        main = runCatching { MainChoice.valueOf(parts[1]) }.getOrDefault(default.main),
        sort = runCatching { SortChoice.valueOf(parts[2]) }.getOrDefault(default.sort)
    )
}