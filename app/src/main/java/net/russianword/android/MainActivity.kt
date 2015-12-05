package net.russianword.android

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import net.russianword.android.api.MTsarService
import net.russianword.android.api.Process
import net.russianword.android.utils.asAsync
import net.russianword.android.utils.getActionBarColor
import net.russianword.android.utils.getActionBarSize
import net.russianword.android.utils.navigationView
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.drawerLayout
import rx.lang.kotlin.onError
import kotlin.properties.Delegates
import android.support.design.R as RR
import org.jetbrains.anko.appcompat.v7.toolbar as tbar

const val FRAGMENT_HOLDER_ID = 1

class MainActivity : AppCompatActivity(), AnkoLogger {

    private var nvNavigation: NavigationView by Delegates.notNull()

    private fun ui() = drawerLayout {
        fitsSystemWindows = true
        verticalLayout {
            tbar().apply {
                lparams(height = getActionBarSize(), width = matchParent)
                backgroundColor = getActionBarColor()
                setSupportActionBar(this)
                supportActionBar.setDisplayHomeAsUpEnabled(true);

                ActionBarDrawerToggle(
                        act, this@drawerLayout, this@apply,
                        R.string.acs_drawer_open, R.string.acs_drawer_close)
                        .let {
                            setDrawerListener(it)
                            it.syncState()
                        }
            }
            frameLayout {
                id = FRAGMENT_HOLDER_ID
            }
        }
        nvNavigation = navigationView {
            addHeaderView(ctx.verticalLayout {
                imageView(R.mipmap.ic_launcher) { padding = dip(16); lparams() }
            })
            lparams(width = wrapContent, height = matchParent, gravity = Gravity.START)
            setNavigationItemSelectedListener() f@{ item ->
                item.setChecked(true)
                this@drawerLayout.closeDrawers()
                return@f true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ui())
        fragmentManager.beginTransaction().replace(FRAGMENT_HOLDER_ID, HelloFragment()).commit()
        listProcesses()
    }

    private fun listProcesses() {
        MTsarService.service.listProcesses()
                .asAsync()
                .onError {
                    toast(R.string.tst_load_failed)
                    error(it.getStackTraceString())
                }
                .subscribe {
                    toast(R.string.tst_processes_loaded)
                    setMenuItems(it)
                }
    }

    private fun setMenuItems(processes: List<Process>) {
        with (nvNavigation) {
            menu.clear()
            processes.forEach { menu.add(it.description) }
            setCheckedItem(menu.getItem(0).itemId)
        }
    }
}
