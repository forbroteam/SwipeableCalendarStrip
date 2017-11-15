package com.forbroteam.calendarstrip

import android.graphics.Typeface
import android.view.View
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.util.*

/**
 * Created by bogatenkov on 06/11/17.
 */

class SwipeableCalendarStrip(builder: Builder) {

    enum class DisplayMode {
        DAYS, MONTHS, MONTHS_YEARS
    }

    private var rootView: View
    private var calendarStripViewId: Int = 0
    private lateinit var calendarStripView: SwipeableCalendarStripView
    private var itemCount: Int = 0
    var typeface: Typeface?
    var itemTextColor: Int = 0
    var itemTextSize: Int = 0
    private var displayMode: DisplayMode?
    lateinit var itemSelectionListener: SwipeableCalendarStripListener

    init {
        rootView = builder.rootView
        calendarStripViewId = builder.viewId
        itemCount = builder.itemCount
        typeface = builder.typeface
        itemTextColor = builder.itemTextColor
        itemTextSize = builder.itemTextSize
        displayMode = builder.displayMode
    }

    fun loadCalendarStrip() {
        calendarStripView = rootView.findViewById(calendarStripViewId)
        calendarStripView.calendarStrip = this
        async(UI) {
            val items: Deferred<LinkedList<CalendarStripItem>> = bg {
                generateItems()
            }

            calendarStripView.prepareCalendarStrip(items.await())
        }
    }

    class Builder {
        var viewId: Int = 0
        var rootView: View
        var itemCount: Int = 0
        var typeface: Typeface? = null
        var itemTextColor: Int = 0
        var itemTextSize: Int = 0
        var displayMode: DisplayMode? = null

        constructor(rootView: View, viewId: Int) {
            this.rootView = rootView
            this.viewId = viewId
        }

        fun build(): SwipeableCalendarStrip {
            initDefaultValues()
            val calendarStrip = SwipeableCalendarStrip(this)
            calendarStrip.loadCalendarStrip()

            return calendarStrip
        }

        private fun initDefaultValues() {
            if (itemCount <= 0) {
                itemCount = 7
            }
            displayMode ?: apply { displayMode = DisplayMode.MONTHS }
        }

        fun itemCount(itemCount: Int): Builder {
            this.itemCount = itemCount
            return this
        }

        fun typeface(typeface: Typeface): Builder {
            this.typeface = typeface
            return this
        }

        fun itemTextColor(itemTextColor: Int): Builder {
            this.itemTextColor = itemTextColor
            return this
        }

        fun itemTextSize(itemTextSize: Int): Builder {
            this.itemTextSize = itemTextSize
            return this
        }

        fun displayMode(displayMode: DisplayMode): Builder {
            this.displayMode = displayMode
            return this
        }
    }

    private fun generateItems(): LinkedList<CalendarStripItem> {
        var itemList: LinkedList<CalendarStripItem> = LinkedList()
        var calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"))
        calendar.time = Date()

        when (displayMode) {
            SwipeableCalendarStrip.DisplayMode.DAYS -> calendar.add(Calendar.DATE, -(itemCount))
            SwipeableCalendarStrip.DisplayMode.MONTHS,
            SwipeableCalendarStrip.DisplayMode.MONTHS_YEARS -> calendar.add(Calendar.MONTH, -(itemCount))
        }

        for (x in 1..itemCount) {
            when (displayMode) {
                SwipeableCalendarStrip.DisplayMode.DAYS -> {
                    calendar.add(Calendar.DATE, 1)
                    itemList.add(CalendarStripItem(SwipeableCalendarStrip.DisplayMode.DAYS, calendar.time))
                }
                SwipeableCalendarStrip.DisplayMode.MONTHS -> {
                    calendar.add(Calendar.MONTH, 1)
                    itemList.add(CalendarStripItem(SwipeableCalendarStrip.DisplayMode.MONTHS, calendar.time))
                }
                SwipeableCalendarStrip.DisplayMode.MONTHS_YEARS -> {
                    calendar.add(Calendar.MONTH, 1)
                    itemList.add(CalendarStripItem(SwipeableCalendarStrip.DisplayMode.MONTHS_YEARS, calendar.time))
                }
            }
        }

        return itemList
    }

    interface ItemClickListener {
        fun onItemClicked(position: Int)
    }
}