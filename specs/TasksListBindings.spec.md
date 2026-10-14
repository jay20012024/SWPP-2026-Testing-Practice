# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show the task's D-day label (from `dueDateLabel(task.dueDate, LocalDate.now(), task.isCompleted)`) in the list item's `due_date_text` view: when the label is non-empty the view shows that text and is `VISIBLE`; when there is nothing to show (no task, or an active task with no due date) the view is hidden (`GONE`) and holds no stale label, even if the same recycled view previously showed one.

**Pre-conditions**
- Called on the main thread with a `TextView` that is attached to a context (e.g. the `due_date_text` view in `task_item.xml`, or a `TextView` created in an instrumented test).
- `task` is either `null` or a valid `Task`.
- "Today" is the device's current date, `LocalDate.now()`. Tips below write due dates as `LocalDate.now()` plus/minus an exact number of days; the label values are exact.

**Post-conditions**
- Success cases
  - An active task due in the future: view text is "D-N" and visibility is `VISIBLE`. `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().plusDays(3)) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-3" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_activeDueInThreeDays_showsDMinus3`)
  - An active task already overdue: view text is "D+N" and visibility is `VISIBLE`. `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().minusDays(2)) / When setDueDateLabel is called / Then textView.text is "D+2" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_activeOverdueByTwoDays_showsDPlus2`)
  - An active task due today: view text is "D-Day" and visibility is `VISIBLE`. `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = false, dueDate = LocalDate.now()) / When setDueDateLabel is called / Then textView.text is "D-Day" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_activeDueToday_showsDDay`)
  - A completed task with a due date: view text is "CLEAR" and visibility is `VISIBLE`. `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = true, dueDate = LocalDate.now().plusDays(3)) / When setDueDateLabel is called / Then textView.text is "CLEAR" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_completedWithDueDate_showsClear`)
  - A completed task with no due date (optional value left empty): view text is "CLEAR" and visibility is `VISIBLE`. `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = true, dueDate = null) / When setDueDateLabel is called / Then textView.text is "CLEAR" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_completedWithoutDueDate_showsClear`)
  - An active task with no due date (optional value left empty): view text is "" and visibility is `GONE`. `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = false, dueDate = null) / When setDueDateLabel is called / Then textView.text is "" and textView.visibility is View.GONE (test: `setDueDateLabel_activeWithoutDueDate_hidesLabel`)
  - A null task (optional value left empty): view text is "" and visibility is `GONE`. `[Integration]`
    - Tip: Given a `TextView` / When setDueDateLabel(textView, null) is called / Then textView.text is "" and textView.visibility is View.GONE (test: `setDueDateLabel_nullTask_hidesLabel`)
  - Shown in the real task list: the item's `due_date_text` displays the label for that task. `[Integration]`
    - Tip: Given the repository holds Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().plusDays(3)) and TasksFragment is launched / When the list is displayed / Then the view with id `due_date_text` in the "Title" row displays "D-3" (test: `setDueDateLabel_taskInList_showsDMinus3`)
- Error cases
  - None. Every `Task?` value is valid input.
- Edge cases
  - Boundary between overdue / due today / due in future, one day before today. `[Integration]`
    - Tip: Given a `TextView` and Task(isCompleted = false, dueDate = LocalDate.now().minusDays(1)) / When setDueDateLabel is called / Then textView.text is "D+1" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_activeDueYesterday_showsDPlus1`)
  - Boundary between overdue / due today / due in future, exactly today. `[Integration]`
    - Tip: Given a `TextView` and Task(isCompleted = false, dueDate = LocalDate.now()) / When setDueDateLabel is called / Then textView.text is "D-Day" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_activeDueToday_showsDDay`, shared with Success case)
  - Boundary between overdue / due today / due in future, one day after today. `[Integration]`
    - Tip: Given a `TextView` and Task(isCompleted = false, dueDate = LocalDate.now().plusDays(1)) / When setDueDateLabel is called / Then textView.text is "D-1" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_activeDueTomorrow_showsDMinus1`)
  - Boundary between shown and hidden, rebinding a recycled view: a view that showed a label is rebound to an active task without a due date. `[Integration]`
    - Tip: Given a `TextView` already bound via setDueDateLabel to Task(isCompleted = false, dueDate = LocalDate.now().plusDays(3)) so it shows "D-3" and is View.VISIBLE / When setDueDateLabel is called again with Task(isCompleted = false, dueDate = null) / Then textView.text is "" and textView.visibility is View.GONE (test: `setDueDateLabel_reboundToTaskWithoutDueDate_hidesLabel`)
  - Boundary between shown and hidden, rebinding a recycled view to a null task. `[Integration]`
    - Tip: Given a `TextView` already bound to Task(isCompleted = true, dueDate = null) so it shows "CLEAR" and is View.VISIBLE / When setDueDateLabel(textView, null) is called / Then textView.text is "" and textView.visibility is View.GONE (test: `setDueDateLabel_reboundToNullTask_hidesLabel`)
  - Boundary between hidden and shown, rebinding a hidden view: a view hidden for a task without a due date is rebound to a task due today. `[Integration]`
    - Tip: Given a `TextView` already bound to Task(isCompleted = false, dueDate = null) so it is View.GONE / When setDueDateLabel is called with Task(isCompleted = false, dueDate = LocalDate.now()) / Then textView.text is "D-Day" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_reboundFromHiddenToDueToday_showsDDay`)
  - Boundary between completed and active on the same due date: toggling completion on a rebound view. `[Integration]`
    - Tip: Given a `TextView` already bound to Task(isCompleted = false, dueDate = LocalDate.now().plusDays(1)) showing "D-1" / When setDueDateLabel is called with Task(isCompleted = true, dueDate = LocalDate.now().plusDays(1)) / Then textView.text is "CLEAR" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_reboundToCompletedTask_showsClear`)

**Undefined behavior** (optional)
- A test whose `LocalDate.now()` in the test and the call to `setDueDateLabel` fall on different days (run across midnight): the expected label is computed from a different "today", so the result is not specified. Month-end, year-end and leap-day day counting is specified and tested in `DueDateUtils.spec.md` with a fixed `today`, not here.
