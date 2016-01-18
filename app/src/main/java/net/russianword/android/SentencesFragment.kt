package net.russianword.android


import android.content.BroadcastReceiver
import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.*
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.trello.rxlifecycle.components.support.RxFragment
import net.russianword.android.api.MTsarService
import net.russianword.android.api.Process
import net.russianword.android.api.Task
import net.russianword.android.utils.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import retrofit.HttpException
import rx.Observable
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates

class SentencesFragment : RxFragment(), AnkoLogger {

    val processId = fragmentToProcessId(this)!!

    private var userState = ProcessState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val restoredState = savedInstanceState?.getSerializable(USER_STATE_BUNDLE_ID) as? ProcessState
        restoredState?.let { userState = it }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val state = userState.currentState
        userState.currentState = when (state) {
            State.AUTHENTICATING -> State.NOT_AUTHENTICATED
            State.LOADING -> State.NOT_LOADED
            State.DISPLAYED -> State.LOADED
            State.SENDING_ANSWER -> State.ANSWER_READY
            else -> state
        }
        userState.preparedAnswers = checkBoxes.filter { it.isChecked }.map { it.text.toString() }.toSet()
        outState?.putSerializable(USER_STATE_BUNDLE_ID, userState)
    }

    private fun fallBack(stringResource: Int, nextState: State) {
        toast(stringResource)
        userState.currentState = nextState
        showRetryButton()
    }

    private fun proceed() {
        if (activity == null || isDetached)
            return

        when (userState.currentState) {
            State.AUTHENTICATING -> Unit
            State.LOADING -> Unit
            State.DISPLAYED -> Unit
            State.SENDING_ANSWER -> Unit

            State.NOT_AUTHENTICATED -> {
                userState.currentState = State.AUTHENTICATING
                MTsarService.authenticateForProcess(Process(processId), "android-" + ctx.getAndroidId())
                        .asAsync(this)
                        .handleError { e: IOException ->
                            fallBack(R.string.tst_auth_failed, State.NOT_AUTHENTICATED)
                        }
                        .subscribe { w ->
                            userState.userId = w.id
                            userState.currentState = State.NOT_LOADED
                            toast(getString(R.string.tst_auth_success).format(w.id))
                            proceed()
                        }
            }

            State.NOT_LOADED -> {
                userState.currentState = State.LOADING
                MTsarService.assignTask(processId, userState.userId!!.toInt())
                        .asAsync(this)
                        .handleError { e: IOException ->
                            fallBack(R.string.tst_load_failed, State.NOT_LOADED)
                        }
                        .subscribe {
                            userState.currentState = State.LOADED
                            userState.task = it
                            proceed()
                        }
            }

            State.LOADED -> {
                userState.currentState = State.DISPLAYED
                showTask(userState.task)
            }

            State.ANSWER_READY -> {
                userState.currentState = State.SENDING_ANSWER
                MTsarService.sendAnswer(processId, userState.userId!!.toInt(),
                                        userState.task!!.id, userState.preparedAnswers.toList())
                        .asAsync(this)
                        .handleErrorThen { e: HttpException ->
                            if (e.response().errorBody().string().contains("#answer-duplicate"))
                                Observable.just(intArrayOf())
                            else
                                Observable.error(e)
                        }
                        .handleError { e: IOException ->
                            e.printStackTrace()
                            fallBack(R.string.tst_send_answer_failed, State.ANSWER_READY)
                        }
                        .subscribe {
                            userState.currentState = State.NOT_LOADED
                            userState.preparedAnswers = emptySet()
                            proceed()
                        }

            }
        }

        if (userState.currentState.let { it == State.AUTHENTICATING || it == State.LOADING })
            showProgressBar()
    }

    var contentLayout: FrameLayout by Delegates.notNull()

    private fun showProgressBar() {
        if (contentLayout.childrenSequence().singleOrNull()?.let { it !is ProgressBar } ?: true) {
            contentLayout.apply {
                progressBar { isIndeterminate = true }.apply {
                    layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            Gravity.CENTER_HORIZONTAL)
                }
            }
        }
    }

    private fun showRetryButton() {
        contentLayout.removeAllViewsInLayout()
        contentLayout.apply {
            button(R.string.btn_retry) {
                onClick { proceed(); setOnClickListener(null); visibility = View.GONE }
            }
        }
    }

    private fun showTask(t: Task?) {
        checkBoxes.clear()
        contentLayout.removeAllViewsInLayout()
        contentLayout.apply {
            taskView(t) {
                appearFromBottom()
            }
        }
        for (ans in userState.preparedAnswers) {
            checkBoxes.firstOrNull() { it.text == ans }?.let { it.isChecked = true }
        }
    }

    val receiversToUnregister = ArrayList<BroadcastReceiver>()

    override fun onResume() {
        super.onResume()
        receiversToUnregister.add(ctx.onNetworkStateChange {
            if (it.isConnected) {
                proceed() //assuming that this is called every time in onResume
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

    private fun ViewManager.taskView(task: Task?, init: CardView.() -> Unit) = UI {
        if (task == null) {
            this@taskView.textView(R.string.no_more_tasks) {
                this@textView.gravity = Gravity.CENTER
                textSize = 17f
            }
            return@UI
        }

        this@taskView.cardView {

            verticalLayout {
                val desc = textView(ctx.spanAsterisksWithAccentColor(task.description)) {
                    textSize = 19f
                }.lparams {
                    width = matchParent
                    verticalMargin = dip(8)
                }

                configuration(orientation = Orientation.LANDSCAPE) {
                    desc.gravity = Gravity.CENTER
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
                    onClick {
                        userState.preparedAnswers = checkBoxes.filter { it.isChecked }.map { it.text.toString() }.toMutableSet()
                        userState.currentState = State.ANSWER_READY
                        proceed()
                        this@cardView.disappearToTop()
                        onClick { }
                    }
                }.lparams {
                    width = matchParent
                    topMargin = dip(8)
                }
            }.apply {
                padding = dip(16)
            }

            init()

            post { disableClip(this) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View =
            ctx.verticalLayout {
                textView(R.string.hint_sentences).lparams { bottomMargin = dip(16) }
                scrollView {
                    contentLayout = frameLayout()
                }.lparams(width = matchParent, height = dip(0), weight = 1f)
            }.style {
                when {
                    it is ViewGroup && it !is FrameLayout -> {
                        it.padding = ctx.dip(16)
                    }
                }
            }
}
