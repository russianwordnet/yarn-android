package net.russianword.android


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.joshdholtz.sentry.Sentry
import net.russianword.android.api.MTsarService
import net.russianword.android.api.Process
import net.russianword.android.api.Task
import net.russianword.android.utils.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import rx.lang.kotlin.onError
import java.util.*
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 */

class SentencesFragment : Fragment(), AnkoLogger {

    val processId = fragmentToProcessId(this)!!

    private var userState = UserState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val restoredState = savedInstanceState?.getSerializable(USER_STATE_BUNDLE_ID) as? UserState
        restoredState?.let { userState = it }
    }

    val CHECKED_ITEMS_BUNDLE_ID = "checked_items"

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTask()
        savedInstanceState?.let {
            val checkedItems = it.getSerializable(CHECKED_ITEMS_BUNDLE_ID) as? ArrayList<*>
            if (checkedItems != null)
                for (i in checkedItems) {
                    i as Pair<*, *>; i.first as String; i.second as Boolean
                    checkBoxes.first { it.text == i.first }.isChecked = i.second as Boolean
                }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putSerializable(USER_STATE_BUNDLE_ID, userState)
        outState?.putSerializable(CHECKED_ITEMS_BUNDLE_ID, checkBoxes.map { it.text to it.isChecked }.toArrayList())
    }


    private fun updateTask() {
        if (userState.userId != null && userState.task != null) {
            showTask(userState.task!!)
            return
        }

        showProgressBar()

        if (userState.userId == null)
            MTsarService.authenticateForProcess(Process(processId), "android-" + ctx.getAndroidId())
                    .asAsync()
                    .onError {
                        toast(R.string.tst_auth_failed)
                        error(it.getStackTraceString())
                        Sentry.captureException(it)
                    }
                    .subscribe { w ->
                        userState.userId = w.id
                        updateTask()
                        toast(getString(R.string.tst_auth_success).format(w.id))
                    }
        else if (userState.task == null)
            MTsarService.assignTask(processId, userState.userId!!.toInt())
                    .asAsync()
                    .onError {
                        toast(R.string.tst_load_failed)
                        error(it.getStackTraceString())
                        Sentry.captureException(it)
                    }
                    .subscribe {
                        userState.task = it
                        updateTask()
                    }
    }

    var contentLayout: ViewGroup by Delegates.notNull()

    private fun showProgressBar() {
        if (contentLayout.childrenSequence().singleOrNull()?.let { it !is ProgressBar } ?: true) {
            contentLayout.removeAllViews()
            contentLayout.apply {
                progressBar { isIndeterminate = true }
            }
        }
    }

    private fun showTask(t: Task) {
        contentLayout.removeAllViewsInLayout()
        contentLayout.apply {
            taskView(t)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private val checkBoxes = ArrayList<CheckBox>()

    private fun ViewManager.taskView(task: Task) = UI {
        checkBoxes.clear()

        this@taskView.cardView {
            verticalLayout {
                textView(ctx.spanAsterisksWithAccentColor(task.description)) {
                    textSize = sp(8).toFloat()
                }.lparams {
                    verticalMargin = dip(8)
                }

                configuration(orientation = Orientation.LANDSCAPE) {
                    linearLayout {
                        for (ans in task.answers) {
                            frameLayout {
                                checkBox(ans) {
                                    gravity = Gravity.CENTER
                                    expandTouchAreaToParent()
                                    checkBoxes.add(this)
                                }.lparams {
                                    gravity = Gravity.CENTER
                                    width = wrapContent
                                    height = matchParent
                                }
                            }.lparams {
                                width = dip(0)
                                weight = 1.0f
                            }
                        }
                    }
                }
                configuration(orientation = Orientation.PORTRAIT) {
                    for (ans in task.answers)
                        checkBoxes.add(checkBox(ans).lparams {
                            width = matchParent
                            verticalMargin = dip(4)
                        })
                }

                button(R.string.btn_done) {
                    makeBorderless()
                }.lparams {
                    width = matchParent
                    topMargin = dip(8)
                }.onClick {
                    //todo
                    userState.task = null
                    updateTask()
                }
            }.apply {
                padding = dip(16)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View =
            ctx.verticalLayout {
                textView(R.string.hint_sentences).lparams { bottomMargin = dip(16) }
                scrollView { contentLayout = frameLayout() }
            }.style {
                when {
                    it is ViewGroup && it !is FrameLayout -> it.padding = ctx.dip(16)
                }
            }
}
