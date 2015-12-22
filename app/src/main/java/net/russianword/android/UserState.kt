package net.russianword.android

import net.russianword.android.api.Task
import java.io.Serializable

/**
 * Created by igushs on 12/15/15.
 */

public const val USER_STATE_BUNDLE_ID = "userState"

public data class UserState(var userId: String? = null, var task: Task? = null) : Serializable