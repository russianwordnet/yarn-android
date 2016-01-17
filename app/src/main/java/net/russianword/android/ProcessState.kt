package net.russianword.android

import net.russianword.android.api.Task
import java.io.Serializable
import java.util.*

public const val USER_STATE_BUNDLE_ID = "userState"

public enum class State {
    NOT_AUTHENTICATED, AUTHENTICATING,
    NOT_LOADED, LOADING,
    LOADED, DISPLAYED,
    ANSWER_READY, SENDING_ANSWER
}

public data class ProcessState(var currentState: State = State.NOT_AUTHENTICATED,
                               var userId: String? = null,
                               var task: Task? = null,
                               var preparedAnswers: Set<String> = HashSet()) : Serializable