package net.russianword.android

import android.app.FragmentManager
import android.content.BroadcastReceiver
import android.content.Context
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
import net.russianword.android.utils.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.drawerLayout
import java.io.IOException
import java.security.cert.CertPathValidatorException
import java.util.*
import kotlin.properties.Delegates
import android.support.design.R as RR
import org.jetbrains.anko.appcompat.v7.toolbar as tbar

const val FRAGMENT_HOLDER_ID = 1

private const val FRAGMENT_BUNDLE_ID = "fragment"

private const val MENU_ABOUT_ID = -1

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
                nvNavigation.menu.itemsSequence().filter { it != item }.forEach { it.setChecked(false) }
                this@drawerLayout.closeDrawers()
                selectProcess(menuItemToProcess[item])
                return@f true
            }
        }
        prepareMenu()
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

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(FRAGMENT_HOLDER_ID)
            var menuItem = processToMenuItem(fragmentToProcessId(fragment))
            nvNavigation.setCheckedItem(menuItem?.itemId ?: MENU_ABOUT_ID)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        loadProcesses()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState,
                                           FRAGMENT_BUNDLE_ID,
                                           supportFragmentManager.findFragmentById(FRAGMENT_HOLDER_ID))
    }

    private fun loadProcesses() {
        MTsarService.cachedListProcesses()
                .asAsync()
                .handleError { e: IOException ->
                    toast(R.string.tst_processes_load_failed)
                    if (nvNavigation.getHeaderView(1) == null)
                        nvNavigation.addHeaderView(retryView())
                    error(e.getStackTraceString())
                }
                .handleError { e: CertPathValidatorException ->
                    toast(R.string.letsencrypt_not_supported)
                    if (nvNavigation.getHeaderView(1) == null)
                        nvNavigation.addHeaderView(retryView(R.string.letsencrypt_not_supported))
                    error(e.getStackTraceString())
                }
                .subscribe ({ displayProcessesInMenu(it) })
    }

    private val receiversToUnregister = ArrayList<BroadcastReceiver>()

    override fun onResume() {
        super.onResume()
        receiversToUnregister.add(onNetworkStateChange {
            if (it.isConnected && menuItemToProcess.isEmpty())
                loadProcesses()
        })
    }

    override fun onPause() {
        super.onPause()
        for (r in receiversToUnregister)
            unregisterReceiver(r)
        receiversToUnregister.clear()
    }

    private val menuItemToProcess = HashMap<MenuItem, Process>()

    private fun processToMenuItem(id: String?) =
            id?.let { menuItemToProcess.entries.firstOrNull { it.value.id == id }?.key }

    private fun Context.retryView(textResource: Int = R.string.tst_processes_load_failed) = verticalLayout {
        textView(textResource) {
            this@textView.gravity = Gravity.CENTER
        }
        button(R.string.btn_retry) {
            onClick { loadProcesses(); onClick { } }
        }
    }.style { it.padding = dip(16) }

    private fun prepareMenu() {
        with (nvNavigation) {
            getHeaderView(1)?.let { removeHeaderView(it) }
            menu.clear()
            menu.add(0, MENU_ABOUT_ID, 0, R.string.mnu_about).apply {
                setCheckable(true)
                setIcon(android.R.drawable.ic_menu_info_details)
            }
            setCheckedItem(MENU_ABOUT_ID)
        }
    }

    private fun displayProcessesInMenu(processes: List<Process>) {
        with (nvNavigation) {
            prepareMenu()
            var id = 100
            menu.addSubMenu(R.string.subMnu_processes).apply {
                processes.forEach { p ->
                    val item = add(0, ++id, 0, p.description.dropLastWhile { it == '.' }).apply {
                        setIcon(android.R.drawable.ic_menu_edit)
                        setCheckable(true)
                    }
                    if (p.id == fragmentToProcessId(supportFragmentManager.findFragmentById(FRAGMENT_HOLDER_ID))) {
                        setCheckedItem(item.itemId)
                        selectProcess(p)
                    }
                    menuItemToProcess[item] = p
                }
            }
        }
    }

    public fun selectProcess(process: Process?) {
        if (process == null) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE) //clear back stack
            supportFragmentManager.beginTransaction().replace(FRAGMENT_HOLDER_ID, HelloFragment()).commit()
        } else {
            val currentProcess = fragmentToProcessId(supportFragmentManager.findFragmentById(FRAGMENT_HOLDER_ID))
            if (process.id == currentProcess)
                return
            supportFragmentManager.beginTransaction()
                    .replace(FRAGMENT_HOLDER_ID, processToFragment(process.id))
                    .addToBackStack(null)
                    .commit()
        }
    }
}