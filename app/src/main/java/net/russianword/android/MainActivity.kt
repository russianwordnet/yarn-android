package net.russianword.android

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import com.joshdholtz.sentry.Sentry
import net.russianword.android.api.MTsarService
import net.russianword.android.api.Process
import net.russianword.android.utils.asAsync
import net.russianword.android.utils.getActionBarColor
import net.russianword.android.utils.getActionBarSize
import net.russianword.android.utils.navigationView
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.drawerLayout
import rx.Observable
import java.util.*
import kotlin.properties.Delegates
import android.support.design.R as RR
import org.jetbrains.anko.appcompat.v7.toolbar as tbar

const val FRAGMENT_HOLDER_ID = 1

private const val FRAGMENT_BUNDLE_ID = "fragment"

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
            itemTextColor = ContextCompat.getColorStateList(ctx, android.R.color.primary_text_dark_nodisable)
            itemIconTintList = ContextCompat.getColorStateList(ctx, android.R.color.primary_text_dark_nodisable)
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
        supportFragmentManager
                .beginTransaction()
                .replace(FRAGMENT_HOLDER_ID,
                         savedInstanceState?.let {
                             supportFragmentManager.getFragment(it, FRAGMENT_BUNDLE_ID)
                         } ?: HelloFragment())
                .commit()

        Sentry.init(this.getApplicationContext(),
                    "http://sentry.eveel.ru",
                    "http://5363021a613a44c9a3a8107af0a5cf07:c252bb3313334773be197aff6b1a7bd7@sentry.eveel.ru/7");
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        listProcesses()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState,
                                           FRAGMENT_BUNDLE_ID,
                                           supportFragmentManager.findFragmentById(FRAGMENT_HOLDER_ID))
    }

    private fun listProcesses() {
        MTsarService.cachedListProcesses()
                .asAsync()
                .onErrorResumeNext {
                    toast(R.string.tst_load_failed)
                    error(it.getStackTraceString())
                    Sentry.captureException(it)
                    Observable.empty()
                }
                .subscribe ({ displayProcessesInMenu(it) })
    }

    private val menuItemToProcess = HashMap<MenuItem, Process>()

    private fun menuItemByProcess(id: String?) =
            id?.let { menuItemToProcess.entries.firstOrNull { it.value.id == id }?.key }

    private fun displayProcessesInMenu(processes: List<Process>) {
        with (nvNavigation) {
            menu.clear()
            menu.addSubMenu(R.string.subMnu_processes).apply {
                processes.forEach { p ->
                    val item = add(p.description).apply { setIcon(android.R.drawable.ic_menu_edit); setCheckable(true) }
                    if (p.id == fragmentToProcessId(supportFragmentManager.findFragmentById(FRAGMENT_HOLDER_ID))) {
                        item.setChecked(true)
                        selectProcess(p)
                    }
                    menuItemToProcess[item] = p
                }
            }
        }
    }


    public fun selectProcess(process: Process) {
        if (process.id == fragmentToProcessId(supportFragmentManager.findFragmentById(FRAGMENT_HOLDER_ID)))
            return

        when (process.id) {
            "sentences" -> supportFragmentManager.beginTransaction().replace(FRAGMENT_HOLDER_ID, SentencesFragment()).addToBackStack(null).commit()
        }
    }

}
