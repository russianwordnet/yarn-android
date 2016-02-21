package net.russianword.android

import net.russianword.android.api.Task
import java.io.Serializable
import java.util.*

const val USER_STATE_BUNDLE_ID = "userState"

enum class State {
    NOT_AUTHENTICATED, AUTHENTICATING,
    NOT_LOADED, LOADING,
    LOADED, DISPLAYED,
    ANSWER_READY, SENDING_ANSWER
}

data class ProcessState(@Volatile var currentState: State = State.NOT_AUTHENTICATED,
                        @Volatile var userId: String? = null,
                        @Volatile var task: Task? = null,
                        @Volatile var preparedAnswers: Set<String> = HashSet()) : Serializable
