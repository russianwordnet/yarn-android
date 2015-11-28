package net.russianword.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {

    fun ui() = verticalLayout {
        horizontalPadding = dip(16)
        verticalPadding = dip(16)

        textView(R.string.txt_greeting).apply { this.gravity = Gravity.CENTER_HORIZONTAL }
        imageView(R.mipmap.ic_launcher).apply { padding = dip(16) }.onClick { toast(R.string.tst_on_icon_tap) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui()
    }
}
