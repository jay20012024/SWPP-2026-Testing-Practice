# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show the D-day label of a task row in the task list, counted from today: `D-N`, `D-Day` or `D+N` for an active task with a due date, `CLEAR` for a completed task, and no label for an active task without a due date. The label must follow the task's current state, so it is updated when the task is checked or unchecked and never keeps a label from an earlier binding. "No label" means the view shows no text (`textView.text` is ""); whether the empty view is `VISIBLE` or `GONE` is not specified.

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
  - An active task with no due date (optional value left empty): no label. `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = false, dueDate = null) / When setDueDateLabel is called / Then textView.text is "" (test: `setDueDateLabel_activeWithoutDueDate_showsNoLabel`)
  - A null task (optional value left empty): no label. `[Integration]`
    - Tip: Given a `TextView` / When setDueDateLabel(textView, null) is called / Then textView.text is "" (test: `setDueDateLabel_nullTask_showsNoLabel`)
  - Shown in the real task list: the item's `due_date_text` displays the label for that task. `[Integration]`
    - Tip: Given the repository holds Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().plusDays(3)) and TasksFragment is launched / When the list is displayed / Then the view with id `due_date_text` in the "Title" row displays "D-3" (test: `setDueDateLabel_taskInList_showsDMinus3`)
- Error cases
  - None. Every `Task?` value is valid input.
- Edge cases
  - Boundary between overdue / due today / due in future, one day before today. Shows "D+1". `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().minusDays(1)) / When setDueDateLabel is called / Then textView.text is "D+1" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_activeDueYesterday_showsDPlus1`)
  - Boundary between overdue / due today / due in future, exactly today. Shows "D-Day". `[Integration]`
    - Tested by `TasksListBindingsTest.setDueDateLabel_activeDueToday_showsDDay`
  - Boundary between overdue / due today / due in future, one day after today. Shows "D-1". `[Integration]`
    - Tip: Given a `TextView` and Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().plusDays(1)) / When setDueDateLabel is called / Then textView.text is "D-1" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_activeDueTomorrow_showsDMinus1`)
  - Unchecking a completed task with no due date: the "CLEAR" label is removed. Shows no label. `[Integration]`
    - Tip: Given a `TextView` already bound to Task(title = "Title", isCompleted = true, dueDate = null) so it shows "CLEAR" / When setDueDateLabel is called again with Task(title = "Title", isCompleted = false, dueDate = null) / Then textView.text is "" (test: `setDueDateLabel_uncheckedWithoutDueDate_showsNoLabel`)
  - Unchecking a completed task with a due date: "CLEAR" changes back to the D-day label. Shows "D-1". `[Integration]`
    - Tip: Given a `TextView` already bound to Task(title = "Title", isCompleted = true, dueDate = LocalDate.now().plusDays(1)) so it shows "CLEAR" / When setDueDateLabel is called again with Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().plusDays(1)) / Then textView.text is "D-1" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_uncheckedWithDueDate_showsDMinus1`)
  - Checking an active task with a due date: the D-day label changes to "CLEAR". Shows "CLEAR". `[Integration]`
    - Tip: Given a `TextView` already bound to Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().plusDays(1)) so it shows "D-1" / When setDueDateLabel is called again with Task(title = "Title", isCompleted = true, dueDate = LocalDate.now().plusDays(1)) / Then textView.text is "CLEAR" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_checkedWithDueDate_showsClear`)
  - Rebinding a recycled view that showed a label to an active task without a due date. Shows no label. `[Integration]`
    - Tip: Given a `TextView` already bound to Task(title = "Title", isCompleted = false, dueDate = LocalDate.now().plusDays(3)) so it shows "D-3" / When setDueDateLabel is called again with Task(title = "Other", isCompleted = false, dueDate = null) / Then textView.text is "" (test: `setDueDateLabel_reboundToTaskWithoutDueDate_showsNoLabel`)
  - Rebinding a recycled view that showed "CLEAR" to a null task. Shows no label. `[Integration]`
    - Tip: Given a `TextView` already bound to Task(title = "Title", isCompleted = true, dueDate = null) so it shows "CLEAR" / When setDueDateLabel(textView, null) is called / Then textView.text is "" (test: `setDueDateLabel_reboundToNullTask_showsNoLabel`)
  - Rebinding a view with no label to a task due today. Shows "D-Day". `[Integration]`
    - Tip: Given a `TextView` already bound to Task(title = "Title", isCompleted = false, dueDate = null) so it shows no label / When setDueDateLabel is called again with Task(title = "Other", isCompleted = false, dueDate = LocalDate.now()) / Then textView.text is "D-Day" and textView.visibility is View.VISIBLE (test: `setDueDateLabel_reboundFromNoLabelToDueToday_showsDDay`)
  - Unchecking a task without a due date in the real task list, after checking it there. Shows no label. `[Integration]`
    - Tip: Given the repository holds Task(title = "Title", isCompleted = false, dueDate = null), TasksFragment is launched, and the `complete_checkbox` in the "Title" row is clicked so the row's `due_date_text` displays "CLEAR" / When the checkbox is clicked again / Then the row's `due_date_text` has text "" (test: `setDueDateLabel_uncheckInList_showsNoLabel`)

**Undefined behavior** (optional)
- A test whose `LocalDate.now()` in the test and the call to `setDueDateLabel` fall on different days (run across midnight): the expected label is computed from a different "today", so the result is not specified. Month-end, year-end and leap-day day counting is specified and tested in `DueDateUtils.spec.md` with a fixed `today`, not here.
