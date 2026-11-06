package com.example.loopatempo.ui.filter

import com.example.loopatempo.data.HomeItem

enum class MainChoice { APPS_FIRST, CUSTOM_FIRST, COMPLETED_FIRST, NOT_COMPLETED_FIRST }

enum class SortChoice { STREAK_HIGHEST, STREAK_LOWEST, NAME_AZ, NAME_ZA, OLDEST, NEWEST }

data class ListOptions(
    val mainEnabled: Boolean = false,
    val main: MainChoice = MainChoice.APPS_FIRST,
    val sort: SortChoice = SortChoice.STREAK_HIGHEST
)

sealed interface ListRow {
    data class Header(val title: String) : ListRow
    data class Entry(val item: HomeItem) : ListRow
}

private fun comparatorFor(sort: SortChoice): Comparator<HomeItem> {
    val primary = when (sort) {
        SortChoice.STREAK_HIGHEST -> compareByDescending<HomeItem> { it.streak }
        SortChoice.STREAK_LOWEST -> compareBy<HomeItem> { it.streak }
        SortChoice.NAME_AZ -> compareBy<HomeItem> { it.habit.name.lowercase() }
        SortChoice.NAME_ZA -> compareByDescending<HomeItem> { it.habit.name.lowercase() }
        SortChoice.OLDEST -> compareBy<HomeItem> { it.habit.createdAt }
        SortChoice.NEWEST -> compareByDescending<HomeItem> { it.habit.createdAt }
    }
    return primary.thenBy { it.habit.name.lowercase() }
}

private fun section(title: String, list: List<HomeItem>): List<ListRow> =
    if (list.isEmpty()) {
        emptyList()
    } else {
        listOf<ListRow>(ListRow.Header(title)) + list.map { ListRow.Entry(it) }
    }

fun arrangeRows(items: List<HomeItem>, options: ListOptions): List<ListRow> {
    val sorted = items.sortedWith(comparatorFor(options.sort))
    if (!options.mainEnabled) return sorted.map { ListRow.Entry(it) }
    val apps = sorted.filter { it.habit.packageName != null }
    val custom = sorted.filter { it.habit.packageName == null }
    val done = sorted.filter { it.checkedToday }
    val pending = sorted.filter { !it.checkedToday }
    return when (options.main) {
        MainChoice.APPS_FIRST -> section("Apps", apps) + section("Custom Tasks", custom)
        MainChoice.CUSTOM_FIRST -> section("Custom Tasks", custom) + section("Apps", apps)
        MainChoice.COMPLETED_FIRST -> section("Completed", done) + section("Not Completed", pending)
        MainChoice.NOT_COMPLETED_FIRST -> section("Not Completed", pending) + section("Completed", done)
    }
}