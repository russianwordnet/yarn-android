package net.russianword.android

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast

class HelloFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return ctx.verticalLayout {
            horizontalPadding = resources.getDimension(R.dimen.activity_horizontal_margin).toInt()
            verticalPadding = resources.getDimension(R.dimen.activity_vertical_margin).toInt()

            textView(R.string.txt_greeting).apply { this.gravity = Gravity.CENTER_HORIZONTAL }
            imageView(R.mipmap.ic_launcher).apply { padding = dip(16) }.onClick { toast(R.string.tst_on_icon_tap) }
        }
    }
}