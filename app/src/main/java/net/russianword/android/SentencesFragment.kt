package net.russianword.android


import android.content.BroadcastReceiver
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ProgressBar
import net.russianword.android.api.MTsarService
import net.russianword.android.api.Process
import net.russianword.android.api.Task
import net.russianword.android.utils.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 */

class SentencesFragment : Fragment(), AnkoLogger {

    val processId = fragmentToProcessId(this)!!

    private var userState = ProcessState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val restoredState = savedInstanceState?.getSerializable(USER_STATE_BUNDLE_ID) as? ProcessState
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
                    if (i is Pair<*, *> && i.first is String)
                        checkBoxes.firstOrNull() { it.text == i.first }?.let { it.isChecked = i.second as Boolean }
                }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        userState.currentState = when (userState.currentState) {
            State.AUTHENTICATING -> State.NOT_AUTHENTICATED
            State.LOADING -> State.NOT_LOADED
            else -> userState.currentState
        }
        outState?.putSerializable(USER_STATE_BUNDLE_ID, userState)
        outState?.putSerializable(CHECKED_ITEMS_BUNDLE_ID, checkBoxes.map { it.text to it.isChecked }.toArrayList())
    }

    private fun updateTask() {
        if (activity == null || isDetached)
            return

        when (userState.currentState) {
            State.LOADED -> {
                showTask(userState.task!!)
            }

            State.AUTHENTICATING -> Unit
            State.LOADING -> Unit

            State.NOT_AUTHENTICATED -> {
                userState.currentState = State.AUTHENTICATING
                MTsarService.authenticateForProcess(Process(processId), "android-" + ctx.getAndroidId())
                        .asAsync()
                        .handleError { e: IOException ->
                            userState.currentState = State.NOT_AUTHENTICATED
                            toast(R.string.tst_auth_failed)
                            error(e.getStackTraceString())
                            showRetryButton()
                        }
                        .subscribe { w ->
                            userState.userId = w.id
                            userState.currentState = State.NOT_LOADED
                            toast(getString(R.string.tst_auth_success).format(w.id))
                            updateTask()
                        }
            }

            State.NOT_LOADED -> {
                userState.currentState = State.LOADING
                MTsarService.assignTask(processId, userState.userId!!.toInt())
                        .asAsync()
                        .handleError { e: IOException ->
                            userState.currentState = State.NOT_LOADED
                            toast(R.string.tst_load_failed)
                            error(e.getStackTraceString())
                            showRetryButton()
                        }
                        .subscribe {
                            userState.currentState = State.LOADED
                            userState.task = it
                            updateTask()
                        }
            }
        }

        if (userState.currentState.let { it == State.AUTHENTICATING || it == State.LOADING })
            showProgressBar()
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

    private fun showRetryButton() {
        contentLayout.removeAllViewsInLayout()
        contentLayout.apply {
            button(R.string.btn_retry) {
                onClick { updateTask(); setOnClickListener(null) }
            }
        }
    }

    private fun showTask(t: Task) {
        contentLayout.removeAllViewsInLayout()
        contentLayout.apply {
            taskView(t)
        }
    }

    val receiversToUnregister = ArrayList<BroadcastReceiver>()

    override fun onResume() {
        super.onResume()
        receiversToUnregister.add(ctx.onNetworkStateChange {
            if (it.isConnected) {
                updateTask()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        for (r in receiversToUnregister)
            ctx.unregisterReceiver(r)
        receiversToUnregister.clear()
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

                val button = button(R.string.btn_done) {
                    makeBorderless()
                }.lparams {
                    width = matchParent
                    topMargin = dip(8)
                }
                button.onClick {
                    //todo Send the answer
                    userState.currentState = State.NOT_LOADED
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
