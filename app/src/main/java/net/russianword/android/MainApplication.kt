package net.russianword.android

import android.app.Application
import com.yandex.metrica.YandexMetrica

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        YandexMetrica.activate(applicationContext, YandexMetricaAccess.API_KEY)
        YandexMetrica.enableActivityAutoTracking(this)
    }
}