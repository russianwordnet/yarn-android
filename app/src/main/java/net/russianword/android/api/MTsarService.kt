package net.russianword.android.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.russianword.android.utils.withSentry
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import retrofit.JacksonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory
import retrofit.http.*
import rx.Observable
import java.io.Serializable
import java.util.*

/**
 * API definition for MTsarEngine.
 *
 * Created by igushs on 12/4/15.
 */


@JsonIgnoreProperties(ignoreUnknown = true)
public data class Process(var id: String = "",
                          var description: String = "")

@JsonIgnoreProperties(ignoreUnknown = true)
public data class Worker(var id: String = "")

@JsonIgnoreProperties(ignoreUnknown = true)
public data class TasksResponse(var tasks: List<Task> = listOf())

@JsonIgnoreProperties(ignoreUnknown = true)
public data class Task(var id: Int = 0,
                       var description: String = "",
                       var answers: List<String> = listOf()) : Serializable

interface MTsarService {
    @GET("/processes")
    @Headers("Accept: application/json")
    fun listProcesses(): Observable<ArrayList<Process>>

    @GET("/processes/{process}/workers/tagged/{tag}")
    @Headers("Accept: application/json")
    fun workerByTag(@Path("process") processId: String, @Path("tag") tag: String): Observable<Worker>

    @FormUrlEncoded
    @POST("/processes/{process}/workers")
    @Headers("Accept: application/json")
    fun addWorker(@Path("process") processId: String, @Field("tags") tags: String): Observable<Worker>

    @GET("/processes/{process}/workers/{worker}/task")
    fun assignTask(@Path("process") processId: String, @Path("worker") workerId: Int): Observable<TasksResponse>

    companion object : AnkoLogger {
        public const val DEFAULT_URL = "https://api.russianword.net/"
        public val DEFAULT_RETROFIT =
                Retrofit.Builder()
                        .baseUrl(DEFAULT_URL)
                        .addConverterFactory(JacksonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build()

        public val service by lazy { DEFAULT_RETROFIT.create(MTsarService::class.java) }

        private var processesCache: Observable<ArrayList<Process>>? = null
        public fun cachedListProcesses() =
                processesCache ?:
                MTsarService.service.listProcesses()
                        .doOnCompleted { info("Loaded processes.") }
                        .cache()
                        .apply { processesCache = this }
                        .doOnError { processesCache = null }
                        .withSentry()

        public fun authenticateForProcess(process: Process, userTag: String): Observable<Worker> =
                service.workerByTag(process.id, userTag)
                        .flatMap {
                            if (it != null)
                                Observable.just(it)
                            else
                                service.addWorker(process.id, userTag)
                        }.withSentry()

        public fun assignTask(processId: String, workerId: Int): Observable<Task> =
                service.assignTask(processId, workerId)
                        .flatMap { Observable.from(it.tasks) }
                        .single()
                        .withSentry()

    }
}