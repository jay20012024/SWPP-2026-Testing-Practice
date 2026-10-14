# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Build the D-day label shown next to a task in the list: "CLEAR" for a completed task, nothing for an active task without a due date, and otherwise "D-Day", "D-N" (due in N days) or "D+N" (overdue by N days), where N is the total number of calendar days between `today` and `dueDate`, correct across month ends, year ends and leap days.

**Pre-conditions**
- `today` is a valid `LocalDate` (non-null).
- `dueDate` is either `null` (task has no due date) or a valid `LocalDate`.

**Post-conditions**
- Success cases
  - A completed task with a due date returns "CLEAR", regardless of how the due date compares to today. `[Unit]`
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-14, isCompleted = true / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "CLEAR" (test: `dueDateLabel_completedWithDueDate_returnsClear`)
  - A completed task that is overdue returns "CLEAR", not "D+N". `[Unit]`
    - Tip: Given dueDate = 2026-10-10, today = 2026-10-14, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR" (test: `dueDateLabel_completedOverdue_returnsClear`)
  - A completed task with no due date (optional value left empty) returns "CLEAR". `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-14, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR" (test: `dueDateLabel_completedWithoutDueDate_returnsClear`)
  - An active task with no due date (optional value left empty) returns "" (empty string). `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "" (test: `dueDateLabel_activeWithoutDueDate_returnsEmpty`)
  - An active task due today returns "D-Day". `[Unit]`
    - Tip: Given dueDate = 2026-10-14, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-Day" (test: `dueDateLabel_activeDueToday_returnsDDay`)
  - An active task due in the future (same month) returns "D-N" with N = days until the due date. `[Unit]`
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-6" (test: `dueDateLabel_activeDueInSixDays_returnsDMinus6`)
  - An active task already overdue (same month) returns "D+N" with N = days since the due date. `[Unit]`
    - Tip: Given dueDate = 2026-10-10, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D+4" (test: `dueDateLabel_activeOverdueByFourDays_returnsDPlus4`)
- Error cases
  - None. Every combination of a nullable `dueDate`, a `today` and `isCompleted` is valid input.
- Edge cases
  - Boundary between overdue / due today / due in future: one day before today returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2026-10-13, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeDueYesterday_returnsDPlus1`)
  - Boundary between overdue / due today / due in future: exactly today returns "D-Day" (not "D-0" or "D+0"). `[Unit]`
    - Tested by `DueDateUtilsTest.dueDateLabel_activeDueToday_returnsDDay`
  - Boundary between overdue / due today / due in future: one day after today returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2026-10-15, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueTomorrow_returnsDMinus1`)
  - Boundary between completed and active: the same due-today task flips from "CLEAR" to "D-Day" only on `isCompleted`. Returns "CLEAR". `[Unit]`
    - Tip: Given dueDate = 2026-10-14, today = 2026-10-14, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR" (test: `dueDateLabel_completedDueToday_returnsClear`)
  - Month end, due date in the next month: last day of month to first day of next month is 1 day. Returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2026-11-01, today = 2026-10-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueAcrossMonthEnd_returnsDMinus1`)
  - Month end, right next to it on the earlier side: same month, last day. Returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2026-10-31, today = 2026-10-30, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueOnMonthEnd_returnsDMinus1`)
  - Month end, right next to it on the later side: two days spanning the month end. Returns "D-2". `[Unit]`
    - Tip: Given dueDate = 2026-11-02, today = 2026-10-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-2" (test: `dueDateLabel_activeDueTwoDaysAcrossMonthEnd_returnsDMinus2`)
  - Month end, overdue across it: first day of month with due date on last day of previous month. Returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2026-10-31, today = 2026-11-01, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeOverdueAcrossMonthEnd_returnsDPlus1`)
  - Month end overdue, right next to it on the earlier side: due the day before, still in the same month. Returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2026-10-30, today = 2026-10-31, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeOverdueOnMonthEnd_returnsDPlus1`)
  - Month end overdue, right next to it on the later side: overdue by two days spanning the month end. Returns "D+2". `[Unit]`
    - Tip: Given dueDate = 2026-10-31, today = 2026-11-02, isCompleted = false / When dueDateLabel is called / Then it returns "D+2" (test: `dueDateLabel_activeOverdueTwoDaysAcrossMonthEnd_returnsDPlus2`)
  - One-month boundary, right before it: one day short of a full month is still counted in days. Returns "D-30". `[Unit]`
    - Tip: Given dueDate = 2026-11-13, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-30" (test: `dueDateLabel_activeDueOneDayBeforeOneMonth_returnsDMinus30`)
  - One-month boundary, exactly at it: due in exactly one month counts every calendar day, not just the day-of-month difference (which is 0). Returns "D-31". `[Unit]`
    - Tip: Given dueDate = 2026-11-14, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-31", not "D-Day" (test: `dueDateLabel_activeDueInOneMonth_returnsDMinus31`)
  - One-month boundary, right after it: one day past a full month counts every calendar day, not just the leftover day. Returns "D-32". `[Unit]`
    - Tip: Given dueDate = 2026-11-15, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-32", not "D-1" (test: `dueDateLabel_activeDueOneDayAfterOneMonth_returnsDMinus32`)
  - One-month boundary on the overdue side, right before it: one day short of a full month overdue. Returns "D+29". `[Unit]`
    - Tip: Given dueDate = 2026-09-15, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D+29" (test: `dueDateLabel_activeOverdueOneDayBeforeOneMonth_returnsDPlus29`)
  - One-month boundary on the overdue side, exactly at it: overdue by exactly one month counts every calendar day. Returns "D+30". `[Unit]`
    - Tip: Given dueDate = 2026-09-14, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D+30" (test: `dueDateLabel_activeOverdueByOneMonth_returnsDPlus30`)
  - One-month boundary on the overdue side, right after it: one day past a full month overdue. Returns "D+31". `[Unit]`
    - Tip: Given dueDate = 2026-09-13, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D+31", not "D+1" (test: `dueDateLabel_activeOverdueOneDayAfterOneMonth_returnsDPlus31`)
  - One-year boundary, right before it: one day short of a full year. Returns "D-364". `[Unit]`
    - Tip: Given dueDate = 2027-10-13, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-364" (test: `dueDateLabel_activeDueOneDayBeforeOneYear_returnsDMinus364`)
  - One-year boundary, exactly at it: due in exactly one year counts every calendar day. Returns "D-365". `[Unit]`
    - Tip: Given dueDate = 2027-10-14, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-365" (test: `dueDateLabel_activeDueInOneYear_returnsDMinus365`)
  - One-year boundary, right after it: one day past a full year. Returns "D-366". `[Unit]`
    - Tip: Given dueDate = 2027-10-15, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D-366", not "D-1" (test: `dueDateLabel_activeDueOneDayAfterOneYear_returnsDMinus366`)
  - One-year boundary on the overdue side, right before it: one day short of a full year overdue. Returns "D+364". `[Unit]`
    - Tip: Given dueDate = 2025-10-15, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D+364" (test: `dueDateLabel_activeOverdueOneDayBeforeOneYear_returnsDPlus364`)
  - One-year boundary on the overdue side, exactly at it: overdue by exactly one year counts every calendar day. Returns "D+365". `[Unit]`
    - Tip: Given dueDate = 2025-10-14, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D+365", not "D-Day" (test: `dueDateLabel_activeOverdueByOneYear_returnsDPlus365`)
  - One-year boundary on the overdue side, right after it: one day past a full year overdue. Returns "D+366". `[Unit]`
    - Tip: Given dueDate = 2025-10-13, today = 2026-10-14, isCompleted = false / When dueDateLabel is called / Then it returns "D+366", not "D+1" (test: `dueDateLabel_activeOverdueOneDayAfterOneYear_returnsDPlus366`)
  - Year end, due date in the next year. Returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2027-01-01, today = 2026-12-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueAcrossYearEnd_returnsDMinus1`)
  - Year end, overdue across it. Returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2026-12-31, today = 2027-01-01, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeOverdueAcrossYearEnd_returnsDPlus1`)
  - Year end, right next to it on the earlier side: due on the last day of the year. Returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2026-12-31, today = 2026-12-30, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueOnYearEnd_returnsDMinus1`)
  - Year end, right next to it on the later side: two days spanning the year end. Returns "D-2". `[Unit]`
    - Tip: Given dueDate = 2027-01-02, today = 2026-12-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-2" (test: `dueDateLabel_activeDueTwoDaysAcrossYearEnd_returnsDMinus2`)
  - Year end overdue, right next to it on the earlier side: due the day before, still in the same year. Returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2026-12-30, today = 2026-12-31, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeOverdueOnYearEnd_returnsDPlus1`)
  - Year end overdue, right next to it on the later side: overdue by two days spanning the year end. Returns "D+2". `[Unit]`
    - Tip: Given dueDate = 2026-12-31, today = 2027-01-02, isCompleted = false / When dueDateLabel is called / Then it returns "D+2" (test: `dueDateLabel_activeOverdueTwoDaysAcrossYearEnd_returnsDPlus2`)
  - Leap day: in a leap year, Feb 28 to Mar 1 is 2 days (Feb 29 exists). Returns "D-2". `[Unit]`
    - Tip: Given dueDate = 2028-03-01, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-2" (test: `dueDateLabel_activeDueAcrossLeapDay_returnsDMinus2`)
  - Leap day, right next to it: Feb 28 to Feb 29 in a leap year is 1 day. Returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2028-02-29, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueOnLeapDay_returnsDMinus1`)
  - Leap day, right next to it: Feb 29 to Mar 1 in a leap year is 1 day. Returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2028-03-01, today = 2028-02-29, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueDayAfterLeapDay_returnsDMinus1`)
  - Leap day overdue: in a leap year, Feb 28 to Mar 1 is 2 days overdue. Returns "D+2". `[Unit]`
    - Tip: Given dueDate = 2028-02-28, today = 2028-03-01, isCompleted = false / When dueDateLabel is called / Then it returns "D+2" (test: `dueDateLabel_activeOverdueAcrossLeapDay_returnsDPlus2`)
  - Leap day overdue, right next to it: Feb 28 to Feb 29 in a leap year is 1 day overdue. Returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2028-02-28, today = 2028-02-29, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeOverdueOnLeapDay_returnsDPlus1`)
  - Leap day overdue, right next to it: Feb 29 to Mar 1 in a leap year is 1 day overdue. Returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2028-02-29, today = 2028-03-01, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeOverdueDayAfterLeapDay_returnsDPlus1`)
  - Non-leap year: Feb 28 to Mar 1 is 1 day. Returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2027-03-01, today = 2027-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueAcrossFebEndNonLeapYear_returnsDMinus1`)
  - Non-leap year, right next to it on the earlier side: Feb 27 to Feb 28 is 1 day. Returns "D-1". `[Unit]`
    - Tip: Given dueDate = 2027-02-28, today = 2027-02-27, isCompleted = false / When dueDateLabel is called / Then it returns "D-1" (test: `dueDateLabel_activeDueOnFebEndNonLeapYear_returnsDMinus1`)
  - Non-leap year, right next to it on the later side: Feb 28 to Mar 2 is 2 days. Returns "D-2". `[Unit]`
    - Tip: Given dueDate = 2027-03-02, today = 2027-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-2" (test: `dueDateLabel_activeDueTwoDaysAcrossFebEndNonLeapYear_returnsDMinus2`)
  - Non-leap year overdue: Feb 28 to Mar 1 is 1 day overdue. Returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2027-02-28, today = 2027-03-01, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeOverdueAcrossFebEndNonLeapYear_returnsDPlus1`)
  - Non-leap year overdue, right next to it on the earlier side: Feb 27 to Feb 28 is 1 day overdue. Returns "D+1". `[Unit]`
    - Tip: Given dueDate = 2027-02-27, today = 2027-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D+1" (test: `dueDateLabel_activeOverdueOnFebEndNonLeapYear_returnsDPlus1`)
  - Non-leap year overdue, right next to it on the later side: Feb 28 to Mar 2 is 2 days overdue. Returns "D+2". `[Unit]`
    - Tip: Given dueDate = 2027-02-28, today = 2027-03-02, isCompleted = false / When dueDateLabel is called / Then it returns "D+2" (test: `dueDateLabel_activeOverdueTwoDaysAcrossFebEndNonLeapYear_returnsDPlus2`)

**Undefined behavior** (optional)
- None. The function is pure: its output depends only on its three arguments, so every input has a defined result.
