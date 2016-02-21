package net.russianword.android

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.trello.rxlifecycle.components.support.RxFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.browse
import org.jetbrains.anko.support.v4.ctx

class HelloFragment : RxFragment() {

    fun TextView.centered() = apply {
        gravity = Gravity.CENTER
        textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return ctx.frameLayout() {
            horizontalPadding = resources.getDimension(R.dimen.activity_horizontal_margin).toInt()
            verticalPadding = resources.getDimension(R.dimen.activity_vertical_margin).toInt()
            verticalLayout {
                textView(R.string.txt_greeting).centered()
                imageView(R.mipmap.ic_launcher).apply {
                    padding = dip(16)
                    onClick { browse("https://russianword.net/") }
                }
                textView(R.string.txt_help).centered()
            }.lparams(gravity = Gravity.TOP or Gravity.CENTER)
            textView(R.string.txt_about).apply {
                centered()
                lparams(gravity = Gravity.BOTTOM, width = matchParent)
                textSize = 12f
            }
        }
    }
}