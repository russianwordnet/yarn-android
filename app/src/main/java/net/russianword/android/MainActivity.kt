package net.russianword.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.frameLayout

const val FRAGMENT_HOLDER_ID = 1

class MainActivity : AppCompatActivity() {

    fun ui() = frameLayout { id = FRAGMENT_HOLDER_ID }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui()
        fragmentManager.beginTransaction().add(FRAGMENT_HOLDER_ID, HelloFragment()).commit()
    }
}
