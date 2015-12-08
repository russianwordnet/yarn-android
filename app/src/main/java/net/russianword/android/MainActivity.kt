package net.russianword.android

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import net.russianword.android.api.MTsarService
import net.russianword.android.api.Process
import net.russianword.android.utils.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.drawerLayout
import rx.lang.kotlin.onError
import java.util.*
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
                supportActionBar.setDisplayHomeAsUpEnabled(true)

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
            addHeaderView(ctx.verticalLayout { imageView(R.mipmap.ic_launcher) { padding = dip(16); lparams() } })
            lparams(width = wrapContent, height = matchParent, gravity = Gravity.START)
            setItemTextColor(ContextCompat.getColorStateList(ctx, android.R.color.primary_text_dark_nodisable))
            setNavigationItemSelectedListener() f@{ item ->
                item.setChecked(true)
                this@drawerLayout.closeDrawers()
                menuItemToProcess[item]?.let { selectProcess(it) }
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
        MTsarService.cachedListProcesses()
                .asAsync()
                .onError {
                    toast(R.string.tst_load_failed)
                    error(it.getStackTraceString())
                }.subscribe { displayProcessesInMenu(it) }
    }

    public fun selectProcess(process: Process) {
        toast("Selected process \"${process.id}\".")
        MTsarService.authenticateForProcess(process, "android-" + getAndroidId())
                .asAsync()
                .onError {
                    toast("Could not authenticate.")
                    error(it.getStackTraceString())
                }
                .subscribe {
                    toast("Authenticated as id ${it.id}.")
                }
    }

    private val menuItemToProcess = HashMap<MenuItem, Process>()

    private fun displayProcessesInMenu(processes: List<Process>) {
        with (nvNavigation) {
            menu.clear()
            menu.addSubMenu(R.string.subMnu_processes).apply {
                processes.forEach { p ->
                    val item = menu.add(p.description).apply { setCheckable(true) }
                    menuItemToProcess[item] = p
                }
            }
        }
    }
}
