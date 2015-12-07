package net.russianword.android.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import retrofit.JacksonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory
import retrofit.http.GET
import retrofit.http.Headers
import rx.Observable
import java.util.*

/**
 * API definition for MTsarEngine.
 *
 * Created by igushs on 12/4/15.
 */


@JsonIgnoreProperties(ignoreUnknown = true)
public data class Process(var id: String = "",
                          var description: String = "")

interface MTsarService {
    @GET("/processes")
    @Headers("Accept: application/json")
    fun listProcesses(): Observable<ArrayList<Process>>

    companion object : AnkoLogger {
        public const val DEFAULT_URL = "http://crowd.yarn.nlpub.ru/"
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
    }
}