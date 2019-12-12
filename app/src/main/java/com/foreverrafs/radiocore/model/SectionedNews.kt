package com.foreverrafs.radiocore.model

import org.joda.time.DateTime

class SectionedNews(private var newsList: List<News>) {
    private var _headerPos: MutableList<Int> = arrayListOf()

    init {
        setHeaderPositions()
        filterList()
    }

    private fun filterList() {
        newsList = newsList.filter { news ->
            news.date.toLocalDate().year > 2018 && news.date.toLocalDate().monthOfYear > 5
        }
    }

    private fun setHeaderPositions() {
        var previousDate: DateTime = DateTime.now()

        this.list.forEachIndexed { index, news ->
            if (index == 0) {
                _headerPos.add(index)
                previousDate = news.date
            } else if (!areSameDates(previousDate, news.date)) {
                _headerPos.add(index)
                previousDate = news.date
            }
        }
    }

    var headerPositions: List<Int>
        get() {
            return _headerPos
        }
        set(value) {
            _headerPos = value.toMutableList()
        }

    val list: List<News>
        get() {
            return this.newsList
        }

    private fun areSameDates(date1: DateTime, date2: DateTime): Boolean {
        return date1.toLocalDate().isEqual(date2.toLocalDate())
    }

}