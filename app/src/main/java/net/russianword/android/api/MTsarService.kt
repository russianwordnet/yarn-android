package net.russianword.android.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.russianword.android.utils.ListMultiMap
import org.jetbrains.anko.AnkoLogger
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*
import rx.Observable
import java.io.Serializable
import java.util.*

/**
 * API definition for MTsarEngine.
 *
 * Created by igushs on 12/4/15.
 */

data class Process(val id: String,
                   val description: String = "")

data class Worker(val id: String)

data class TasksResponse(val tasks: List<Task>)

data class Task(val id: Int,
                val description: String,
                val answers: List<String>) : Serializable

data class AnswerReport(val id: Int,
                        val stage: String,
                        val answers: List<String>)

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

    @FormUrlEncoded
    @PATCH("/processes/{process}/workers/{worker}/answers")
    fun sendAnswer(@Path("process") processId: String, @Path("worker") workerId: Int,
                   @FieldMap(encoded = false) fields: Map<String, String>): Observable<List<AnswerReport>>

    @FormUrlEncoded
    @PATCH("/processes/{process}/workers/{worker}/answers/skip")
    fun skipAnswer(@Path("process") processId: String, @Path("worker") workerId: Int,
                   @Field("tasks") taskId: Int): Observable<List<AnswerReport>>

    companion object : AnkoLogger {
        const val DEFAULT_URL = "https://api.russianword.net/"

        val DEFAULT_RETROFIT =
                Retrofit.Builder()
                        .baseUrl(DEFAULT_URL)
                        .addConverterFactory(JacksonConverterFactory.create(
                                jacksonObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)))
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build()

        val service by lazy { DEFAULT_RETROFIT.create(MTsarService::class.java) }

        private var processesCache: ArrayList<Process>? = null
        fun cachedListProcesses(): Observable<ArrayList<Process>> =
                processesCache?.let { Observable.just(it) } ?:
                service.listProcesses().doOnNext { processesCache = it }

        fun authenticateForProcess(process: Process, userTag: String): Observable<Worker> =
                service.workerByTag(process.id, userTag)
                        .flatMap {
                            if (it != null)
                                Observable.just(it)
                            else
                                service.addWorker(process.id, userTag)
                        }

        fun assignTask(processId: String, workerId: Int): Observable<Task> =
                service.assignTask(processId, workerId)
                        .flatMap { Observable.from(it?.tasks ?: listOf()) }
                        .singleOrDefault(null)

        fun sendAnswer(processId:
                       String, workerId: Int,
                       taskId: Int, answers: List<String>): Observable<List<AnswerReport>> =
                service.sendAnswer(
                        processId, workerId,
                        ListMultiMap(mapOf("answers[$taskId]" to answers.let { if (it.isEmpty()) listOf("") else it }))
                )

        fun skipAnswer(processId:
                       String, workerId: Int,
                       taskId: Int) =
                service.skipAnswer(processId, workerId, taskId)
    }
}