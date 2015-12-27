package net.russianword.android

import net.russianword.android.api.Task
import java.io.Serializable

public const val USER_STATE_BUNDLE_ID = "userState"

public enum class State { NOT_AUTHENTICATED, AUTHENTICATING, NOT_LOADED, LOADING, LOADED, DISPLAYED }

public data class ProcessState(var currentState: State = State.NOT_AUTHENTICATED,
                               var userId: String? = null,
                               var task: Task? = null) : Serializable