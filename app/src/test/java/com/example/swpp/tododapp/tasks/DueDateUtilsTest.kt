package com.example.swpp.tododapp.tasks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DueDateUtilsTest {

    @Test
    fun dueDateLabel_completedWithDueDate_returnsClear() {
        // GIVEN - dueDate = 2026-10-20, today = 2026-10-14, completed task
        val dueDate = LocalDate.of(2026, 10, 20)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = true)

        // THEN - A completed task with a due date returns "CLEAR", regardless of how the due date compares to today
        assertEquals("CLEAR", result)
    }

    @Test
    fun dueDateLabel_completedOverdue_returnsClear() {
        // GIVEN - dueDate = 2026-10-10, today = 2026-10-14, completed task
        val dueDate = LocalDate.of(2026, 10, 10)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = true)

        // THEN - A completed task that is overdue returns "CLEAR", not "D+N"
        assertEquals("CLEAR", result)
    }

    @Test
    fun dueDateLabel_completedWithoutDueDate_returnsClear() {
        // GIVEN - dueDate = null, today = 2026-10-14, completed task
        val dueDate: LocalDate? = null
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = true)

        // THEN - A completed task with no due date (optional value left empty) returns "CLEAR"
        assertEquals("CLEAR", result)
    }

    @Test
    fun dueDateLabel_activeWithoutDueDate_returnsEmpty() {
        // GIVEN - dueDate = null, today = 2026-10-14, active task
        val dueDate: LocalDate? = null
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - An active task with no due date (optional value left empty) returns "" (empty string)
        assertEquals("", result)
    }

    @Test
    fun dueDateLabel_activeDueToday_returnsDDay() {
        // GIVEN - dueDate = 2026-10-14, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 10, 14)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - An active task due today returns "D-Day"
        assertEquals("D-Day", result)
    }

    @Test
    fun dueDateLabel_activeDueInSixDays_returnsDMinus6() {
        // GIVEN - dueDate = 2026-10-20, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 10, 20)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - An active task due in the future (same month) returns "D-N" with N = days until the due date
        assertEquals("D-6", result)
    }

    @Test
    fun dueDateLabel_activeOverdueByFourDays_returnsDPlus4() {
        // GIVEN - dueDate = 2026-10-10, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 10, 10)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - An active task already overdue (same month) returns "D+N" with N = days since the due date
        assertEquals("D+4", result)
    }

    @Test
    fun dueDateLabel_activeDueYesterday_returnsDPlus1() {
        // GIVEN - dueDate = 2026-10-13, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 10, 13)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Boundary between overdue / due today / due in future: one day before today returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeDueTomorrow_returnsDMinus1() {
        // GIVEN - dueDate = 2026-10-15, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 10, 15)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Boundary between overdue / due today / due in future: one day after today returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_completedDueToday_returnsClear() {
        // GIVEN - dueDate = 2026-10-14, today = 2026-10-14, completed task
        val dueDate = LocalDate.of(2026, 10, 14)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = true)

        // THEN - Boundary between completed and active: the same due-today task flips from "CLEAR" to "D-Day" only on `isCompleted`. Returns "CLEAR"
        assertEquals("CLEAR", result)
    }

    @Test
    fun dueDateLabel_activeDueAcrossMonthEnd_returnsDMinus1() {
        // GIVEN - dueDate = 2026-11-01, today = 2026-10-31, active task
        val dueDate = LocalDate.of(2026, 11, 1)
        val today = LocalDate.of(2026, 10, 31)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Month end, due date in the next month: last day of month to first day of next month is 1 day. Returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_activeDueOnMonthEnd_returnsDMinus1() {
        // GIVEN - dueDate = 2026-10-31, today = 2026-10-30, active task
        val dueDate = LocalDate.of(2026, 10, 31)
        val today = LocalDate.of(2026, 10, 30)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Month end, right next to it on the earlier side: same month, last day. Returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_activeDueTwoDaysAcrossMonthEnd_returnsDMinus2() {
        // GIVEN - dueDate = 2026-11-02, today = 2026-10-31, active task
        val dueDate = LocalDate.of(2026, 11, 2)
        val today = LocalDate.of(2026, 10, 31)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Month end, right next to it on the later side: two days spanning the month end. Returns "D-2"
        assertEquals("D-2", result)
    }

    @Test
    fun dueDateLabel_activeOverdueAcrossMonthEnd_returnsDPlus1() {
        // GIVEN - dueDate = 2026-10-31, today = 2026-11-01, active task
        val dueDate = LocalDate.of(2026, 10, 31)
        val today = LocalDate.of(2026, 11, 1)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Month end, overdue across it: first day of month with due date on last day of previous month. Returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeOverdueOnMonthEnd_returnsDPlus1() {
        // GIVEN - dueDate = 2026-10-30, today = 2026-10-31, active task
        val dueDate = LocalDate.of(2026, 10, 30)
        val today = LocalDate.of(2026, 10, 31)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Month end overdue, right next to it on the earlier side: due the day before, still in the same month. Returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeOverdueTwoDaysAcrossMonthEnd_returnsDPlus2() {
        // GIVEN - dueDate = 2026-10-31, today = 2026-11-02, active task
        val dueDate = LocalDate.of(2026, 10, 31)
        val today = LocalDate.of(2026, 11, 2)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Month end overdue, right next to it on the later side: overdue by two days spanning the month end. Returns "D+2"
        assertEquals("D+2", result)
    }

    @Test
    fun dueDateLabel_activeDueOneDayBeforeOneMonth_returnsDMinus30() {
        // GIVEN - dueDate = 2026-11-13, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 11, 13)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-month boundary, right before it: one day short of a full month is still counted in days. Returns "D-30"
        assertEquals("D-30", result)
    }

    @Test
    fun dueDateLabel_activeDueInOneMonth_returnsDMinus31() {
        // GIVEN - dueDate = 2026-11-14, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 11, 14)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-month boundary, exactly at it: due in exactly one month counts every calendar day, not just the day-of-month difference (which is 0). Returns "D-31"
        assertEquals("D-31", result)
    }

    @Test
    fun dueDateLabel_activeDueOneDayAfterOneMonth_returnsDMinus32() {
        // GIVEN - dueDate = 2026-11-15, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 11, 15)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-month boundary, right after it: one day past a full month counts every calendar day, not just the leftover day. Returns "D-32"
        assertEquals("D-32", result)
    }

    @Test
    fun dueDateLabel_activeOverdueOneDayBeforeOneMonth_returnsDPlus29() {
        // GIVEN - dueDate = 2026-09-15, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 9, 15)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-month boundary on the overdue side, right before it: one day short of a full month overdue. Returns "D+29"
        assertEquals("D+29", result)
    }

    @Test
    fun dueDateLabel_activeOverdueByOneMonth_returnsDPlus30() {
        // GIVEN - dueDate = 2026-09-14, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 9, 14)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-month boundary on the overdue side, exactly at it: overdue by exactly one month counts every calendar day. Returns "D+30"
        assertEquals("D+30", result)
    }

    @Test
    fun dueDateLabel_activeOverdueOneDayAfterOneMonth_returnsDPlus31() {
        // GIVEN - dueDate = 2026-09-13, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2026, 9, 13)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-month boundary on the overdue side, right after it: one day past a full month overdue. Returns "D+31"
        assertEquals("D+31", result)
    }

    @Test
    fun dueDateLabel_activeDueOneDayBeforeOneYear_returnsDMinus364() {
        // GIVEN - dueDate = 2027-10-13, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2027, 10, 13)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-year boundary, right before it: one day short of a full year. Returns "D-364"
        assertEquals("D-364", result)
    }

    @Test
    fun dueDateLabel_activeDueInOneYear_returnsDMinus365() {
        // GIVEN - dueDate = 2027-10-14, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2027, 10, 14)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-year boundary, exactly at it: due in exactly one year counts every calendar day. Returns "D-365"
        assertEquals("D-365", result)
    }

    @Test
    fun dueDateLabel_activeDueOneDayAfterOneYear_returnsDMinus366() {
        // GIVEN - dueDate = 2027-10-15, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2027, 10, 15)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-year boundary, right after it: one day past a full year. Returns "D-366"
        assertEquals("D-366", result)
    }

    @Test
    fun dueDateLabel_activeOverdueOneDayBeforeOneYear_returnsDPlus364() {
        // GIVEN - dueDate = 2025-10-15, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2025, 10, 15)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-year boundary on the overdue side, right before it: one day short of a full year overdue. Returns "D+364"
        assertEquals("D+364", result)
    }

    @Test
    fun dueDateLabel_activeOverdueByOneYear_returnsDPlus365() {
        // GIVEN - dueDate = 2025-10-14, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2025, 10, 14)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-year boundary on the overdue side, exactly at it: overdue by exactly one year counts every calendar day. Returns "D+365"
        assertEquals("D+365", result)
    }

    @Test
    fun dueDateLabel_activeOverdueOneDayAfterOneYear_returnsDPlus366() {
        // GIVEN - dueDate = 2025-10-13, today = 2026-10-14, active task
        val dueDate = LocalDate.of(2025, 10, 13)
        val today = LocalDate.of(2026, 10, 14)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - One-year boundary on the overdue side, right after it: one day past a full year overdue. Returns "D+366"
        assertEquals("D+366", result)
    }

    @Test
    fun dueDateLabel_activeDueAcrossYearEnd_returnsDMinus1() {
        // GIVEN - dueDate = 2027-01-01, today = 2026-12-31, active task
        val dueDate = LocalDate.of(2027, 1, 1)
        val today = LocalDate.of(2026, 12, 31)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Year end, due date in the next year. Returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_activeOverdueAcrossYearEnd_returnsDPlus1() {
        // GIVEN - dueDate = 2026-12-31, today = 2027-01-01, active task
        val dueDate = LocalDate.of(2026, 12, 31)
        val today = LocalDate.of(2027, 1, 1)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Year end, overdue across it. Returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeDueOnYearEnd_returnsDMinus1() {
        // GIVEN - dueDate = 2026-12-31, today = 2026-12-30, active task
        val dueDate = LocalDate.of(2026, 12, 31)
        val today = LocalDate.of(2026, 12, 30)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Year end, right next to it on the earlier side: due on the last day of the year. Returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_activeDueTwoDaysAcrossYearEnd_returnsDMinus2() {
        // GIVEN - dueDate = 2027-01-02, today = 2026-12-31, active task
        val dueDate = LocalDate.of(2027, 1, 2)
        val today = LocalDate.of(2026, 12, 31)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Year end, right next to it on the later side: two days spanning the year end. Returns "D-2"
        assertEquals("D-2", result)
    }

    @Test
    fun dueDateLabel_activeOverdueOnYearEnd_returnsDPlus1() {
        // GIVEN - dueDate = 2026-12-30, today = 2026-12-31, active task
        val dueDate = LocalDate.of(2026, 12, 30)
        val today = LocalDate.of(2026, 12, 31)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Year end overdue, right next to it on the earlier side: due the day before, still in the same year. Returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeOverdueTwoDaysAcrossYearEnd_returnsDPlus2() {
        // GIVEN - dueDate = 2026-12-31, today = 2027-01-02, active task
        val dueDate = LocalDate.of(2026, 12, 31)
        val today = LocalDate.of(2027, 1, 2)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Year end overdue, right next to it on the later side: overdue by two days spanning the year end. Returns "D+2"
        assertEquals("D+2", result)
    }

    @Test
    fun dueDateLabel_activeDueAcrossLeapDay_returnsDMinus2() {
        // GIVEN - dueDate = 2028-03-01, today = 2028-02-28, active task
        val dueDate = LocalDate.of(2028, 3, 1)
        val today = LocalDate.of(2028, 2, 28)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Leap day: in a leap year, Feb 28 to Mar 1 is 2 days (Feb 29 exists). Returns "D-2"
        assertEquals("D-2", result)
    }

    @Test
    fun dueDateLabel_activeDueOnLeapDay_returnsDMinus1() {
        // GIVEN - dueDate = 2028-02-29, today = 2028-02-28, active task
        val dueDate = LocalDate.of(2028, 2, 29)
        val today = LocalDate.of(2028, 2, 28)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Leap day, right next to it: Feb 28 to Feb 29 in a leap year is 1 day. Returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_activeDueDayAfterLeapDay_returnsDMinus1() {
        // GIVEN - dueDate = 2028-03-01, today = 2028-02-29, active task
        val dueDate = LocalDate.of(2028, 3, 1)
        val today = LocalDate.of(2028, 2, 29)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Leap day, right next to it: Feb 29 to Mar 1 in a leap year is 1 day. Returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_activeOverdueAcrossLeapDay_returnsDPlus2() {
        // GIVEN - dueDate = 2028-02-28, today = 2028-03-01, active task
        val dueDate = LocalDate.of(2028, 2, 28)
        val today = LocalDate.of(2028, 3, 1)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Leap day overdue: in a leap year, Feb 28 to Mar 1 is 2 days overdue. Returns "D+2"
        assertEquals("D+2", result)
    }

    @Test
    fun dueDateLabel_activeOverdueOnLeapDay_returnsDPlus1() {
        // GIVEN - dueDate = 2028-02-28, today = 2028-02-29, active task
        val dueDate = LocalDate.of(2028, 2, 28)
        val today = LocalDate.of(2028, 2, 29)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Leap day overdue, right next to it: Feb 28 to Feb 29 in a leap year is 1 day overdue. Returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeOverdueDayAfterLeapDay_returnsDPlus1() {
        // GIVEN - dueDate = 2028-02-29, today = 2028-03-01, active task
        val dueDate = LocalDate.of(2028, 2, 29)
        val today = LocalDate.of(2028, 3, 1)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Leap day overdue, right next to it: Feb 29 to Mar 1 in a leap year is 1 day overdue. Returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeDueAcrossFebEndNonLeapYear_returnsDMinus1() {
        // GIVEN - dueDate = 2027-03-01, today = 2027-02-28, active task
        val dueDate = LocalDate.of(2027, 3, 1)
        val today = LocalDate.of(2027, 2, 28)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Non-leap year: Feb 28 to Mar 1 is 1 day. Returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_activeDueOnFebEndNonLeapYear_returnsDMinus1() {
        // GIVEN - dueDate = 2027-02-28, today = 2027-02-27, active task
        val dueDate = LocalDate.of(2027, 2, 28)
        val today = LocalDate.of(2027, 2, 27)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Non-leap year, right next to it on the earlier side: Feb 27 to Feb 28 is 1 day. Returns "D-1"
        assertEquals("D-1", result)
    }

    @Test
    fun dueDateLabel_activeDueTwoDaysAcrossFebEndNonLeapYear_returnsDMinus2() {
        // GIVEN - dueDate = 2027-03-02, today = 2027-02-28, active task
        val dueDate = LocalDate.of(2027, 3, 2)
        val today = LocalDate.of(2027, 2, 28)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Non-leap year, right next to it on the later side: Feb 28 to Mar 2 is 2 days. Returns "D-2"
        assertEquals("D-2", result)
    }

    @Test
    fun dueDateLabel_activeOverdueAcrossFebEndNonLeapYear_returnsDPlus1() {
        // GIVEN - dueDate = 2027-02-28, today = 2027-03-01, active task
        val dueDate = LocalDate.of(2027, 2, 28)
        val today = LocalDate.of(2027, 3, 1)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Non-leap year overdue: Feb 28 to Mar 1 is 1 day overdue. Returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeOverdueOnFebEndNonLeapYear_returnsDPlus1() {
        // GIVEN - dueDate = 2027-02-27, today = 2027-02-28, active task
        val dueDate = LocalDate.of(2027, 2, 27)
        val today = LocalDate.of(2027, 2, 28)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Non-leap year overdue, right next to it on the earlier side: Feb 27 to Feb 28 is 1 day overdue. Returns "D+1"
        assertEquals("D+1", result)
    }

    @Test
    fun dueDateLabel_activeOverdueTwoDaysAcrossFebEndNonLeapYear_returnsDPlus2() {
        // GIVEN - dueDate = 2027-02-28, today = 2027-03-02, active task
        val dueDate = LocalDate.of(2027, 2, 28)
        val today = LocalDate.of(2027, 3, 2)

        // WHEN - Build the label
        val result = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Non-leap year overdue, right next to it on the later side: Feb 28 to Mar 2 is 2 days overdue. Returns "D+2"
        assertEquals("D+2", result)
    }
}
