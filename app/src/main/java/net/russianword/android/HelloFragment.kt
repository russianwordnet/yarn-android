package net.russianword.android

import android.app.Fragment
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*

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
