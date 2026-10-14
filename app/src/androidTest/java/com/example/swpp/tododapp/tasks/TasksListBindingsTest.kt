package com.example.swpp.tododapp.tasks

import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.example.swpp.tododapp.R
import com.example.swpp.tododapp.ServiceLocator
import com.example.swpp.tododapp.data.Task
import com.example.swpp.tododapp.data.source.FakeAndroidTestRepository
import com.example.swpp.tododapp.data.source.TasksRepository
import kotlinx.coroutines.test.runTest
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@MediumTest
@RunWith(AndroidJUnit4::class)
class TasksListBindingsTest {
    private lateinit var repository: TasksRepository
    private lateinit var textView: TextView

    @Before
    fun init() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
        getInstrumentation().runOnMainSync {
            textView = TextView(ApplicationProvider.getApplicationContext())
        }
    }

    @After
    fun cleanupDb() = runTest {
        ServiceLocator.resetRepository()
    }

    private fun bind(task: Task?) = getInstrumentation().runOnMainSync { setDueDateLabel(textView, task) }

    private fun task(isCompleted: Boolean, dueDate: LocalDate?, title: String = "Title") =
        Task(title = title, isCompleted = isCompleted, dueDate = dueDate)

    private fun assertLabel(expected: String) {
        assertEquals(expected, textView.text.toString())
        assertEquals(View.VISIBLE, textView.visibility)
    }

    private fun assertNoLabel() = assertEquals("", textView.text.toString())

    private fun dueDateTextInRow(title: String) =
        onView(allOf(withId(R.id.due_date_text), hasSibling(withText(title))))

    @Test
    fun setDueDateLabel_activeDueInThreeDays_showsDMinus3() {
        // GIVEN - An active task due in 3 days
        val task = task(isCompleted = false, dueDate = LocalDate.now().plusDays(3))

        // WHEN - Bind the task to the view
        bind(task)

        // THEN - The view shows "D-3"
        assertLabel("D-3")
    }

    @Test
    fun setDueDateLabel_activeOverdueByTwoDays_showsDPlus2() {
        // GIVEN - An active task overdue by 2 days
        val task = task(isCompleted = false, dueDate = LocalDate.now().minusDays(2))

        // WHEN - Bind the task to the view
        bind(task)

        // THEN - The view shows "D+2"
        assertLabel("D+2")
    }

    @Test
    fun setDueDateLabel_activeDueToday_showsDDay() {
        // GIVEN - An active task due today
        val task = task(isCompleted = false, dueDate = LocalDate.now())

        // WHEN - Bind the task to the view
        bind(task)

        // THEN - The view shows "D-Day"
        assertLabel("D-Day")
    }

    @Test
    fun setDueDateLabel_completedWithDueDate_showsClear() {
        // GIVEN - A completed task due in 3 days
        val task = task(isCompleted = true, dueDate = LocalDate.now().plusDays(3))

        // WHEN - Bind the task to the view
        bind(task)

        // THEN - The view shows "CLEAR"
        assertLabel("CLEAR")
    }

    @Test
    fun setDueDateLabel_completedWithoutDueDate_showsClear() {
        // GIVEN - A completed task without a due date
        val task = task(isCompleted = true, dueDate = null)

        // WHEN - Bind the task to the view
        bind(task)

        // THEN - The view shows "CLEAR"
        assertLabel("CLEAR")
    }

    @Test
    fun setDueDateLabel_activeWithoutDueDate_showsNoLabel() {
        // GIVEN - An active task without a due date
        val task = task(isCompleted = false, dueDate = null)

        // WHEN - Bind the task to the view
        bind(task)

        // THEN - The view shows no label
        assertNoLabel()
    }

    @Test
    fun setDueDateLabel_nullTask_showsNoLabel() {
        // GIVEN - No task
        val task: Task? = null

        // WHEN - Bind null to the view
        bind(task)

        // THEN - The view shows no label
        assertNoLabel()
    }

    @Test
    fun setDueDateLabel_taskInList_showsDMinus3() = runTest {
        // GIVEN - The repository holds an active task due in 3 days
        repository.saveTask(task(isCompleted = false, dueDate = LocalDate.now().plusDays(3)))

        // WHEN - The task list is displayed
        launchFragmentInContainer<TasksFragment>(themeResId = R.style.AppTheme)

        // THEN - The row of the task shows "D-3"
        dueDateTextInRow("Title").check(matches(withText("D-3")))
    }

    @Test
    fun setDueDateLabel_activeDueYesterday_showsDPlus1() {
        // GIVEN - An active task due yesterday
        val task = task(isCompleted = false, dueDate = LocalDate.now().minusDays(1))

        // WHEN - Bind the task to the view
        bind(task)

        // THEN - The view shows "D+1"
        assertLabel("D+1")
    }

    @Test
    fun setDueDateLabel_activeDueTomorrow_showsDMinus1() {
        // GIVEN - An active task due tomorrow
        val task = task(isCompleted = false, dueDate = LocalDate.now().plusDays(1))

        // WHEN - Bind the task to the view
        bind(task)

        // THEN - The view shows "D-1"
        assertLabel("D-1")
    }

    @Test
    fun setDueDateLabel_uncheckedWithoutDueDate_showsNoLabel() {
        // GIVEN - The view shows "CLEAR" for a completed task without a due date
        bind(task(isCompleted = true, dueDate = null))

        // WHEN - The task is unchecked
        bind(task(isCompleted = false, dueDate = null))

        // THEN - The view shows no label
        assertNoLabel()
    }

    @Test
    fun setDueDateLabel_uncheckedWithDueDate_showsDMinus1() {
        // GIVEN - The view shows "CLEAR" for a completed task due tomorrow
        val dueDate = LocalDate.now().plusDays(1)
        bind(task(isCompleted = true, dueDate = dueDate))

        // WHEN - The task is unchecked
        bind(task(isCompleted = false, dueDate = dueDate))

        // THEN - The view shows "D-1"
        assertLabel("D-1")
    }

    @Test
    fun setDueDateLabel_checkedWithDueDate_showsClear() {
        // GIVEN - The view shows "D-1" for an active task due tomorrow
        val dueDate = LocalDate.now().plusDays(1)
        bind(task(isCompleted = false, dueDate = dueDate))

        // WHEN - The task is checked
        bind(task(isCompleted = true, dueDate = dueDate))

        // THEN - The view shows "CLEAR"
        assertLabel("CLEAR")
    }

    @Test
    fun setDueDateLabel_reboundToTaskWithoutDueDate_showsNoLabel() {
        // GIVEN - The view shows "D-3" for an active task due in 3 days
        bind(task(isCompleted = false, dueDate = LocalDate.now().plusDays(3)))

        // WHEN - The view is rebound to another active task without a due date
        bind(task(isCompleted = false, dueDate = null, title = "Other"))

        // THEN - The view shows no label
        assertNoLabel()
    }

    @Test
    fun setDueDateLabel_reboundToNullTask_showsNoLabel() {
        // GIVEN - The view shows "CLEAR" for a completed task without a due date
        bind(task(isCompleted = true, dueDate = null))

        // WHEN - The view is rebound to null
        bind(null)

        // THEN - The view shows no label
        assertNoLabel()
    }

    @Test
    fun setDueDateLabel_reboundFromNoLabelToDueToday_showsDDay() {
        // GIVEN - The view shows no label for an active task without a due date
        bind(task(isCompleted = false, dueDate = null))

        // WHEN - The view is rebound to another active task due today
        bind(task(isCompleted = false, dueDate = LocalDate.now(), title = "Other"))

        // THEN - The view shows "D-Day"
        assertLabel("D-Day")
    }

    @Test
    fun setDueDateLabel_uncheckInList_showsNoLabel() = runTest {
        // GIVEN - The task list shows "CLEAR" after checking an active task without a due date
        repository.saveTask(task(isCompleted = false, dueDate = null))
        launchFragmentInContainer<TasksFragment>(themeResId = R.style.AppTheme)
        val checkbox = onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("Title"))))
        checkbox.perform(click())
        dueDateTextInRow("Title").check(matches(withText("CLEAR")))

        // WHEN - The checkbox is clicked again
        checkbox.perform(click())

        // THEN - The row shows no label
        dueDateTextInRow("Title").check(matches(withText("")))
    }
}
