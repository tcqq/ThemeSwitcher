package com.example.themeswitcher.activity

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.themeswitcher.model.pref.NightModePref
import com.example.themeswitcher.themeswitcher.ThemeOverlayUtils
import com.example.themeswitcher.R
import com.example.themeswitcher.enums.NightMode
import com.example.themeswitcher.utils.MenuColorize
import com.example.themeswitcher.viewmodel.ToolbarViewModel
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity


/**
 * Base Activity class, used to initialize global configuration.
 *
 * @author Alan Dreamer
 * @since 2016/03/12 Created
 */
abstract class BaseActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeOverlayUtils.applyThemeOverlays(this)
        super.onCreate(savedInstanceState)
        when (NightModePref.nightMode) {
            NightMode.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            NightMode.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val model = ViewModelProviders.of(this).get(ToolbarViewModel::class.java)
        model.menuResId.observe(this, Observer<Int> { menuResId ->
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            toolbar.menu.clear()
            menuInflater.inflate(menuResId, menu)
            val menuIconColor = if (nightMode()) Color.WHITE else Color.BLACK
            MenuColorize.colorMenu(this, menu, menuIconColor)
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun nightMode(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    fun setMenuFromResource(@MenuRes menuResId: Int) {
        ViewModelProviders.of(this).get(ToolbarViewModel::class.java).menuResId.value = menuResId
    }

    fun setActionBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
    }

    fun setActionBarTitle(@StringRes resId: Int) {
        supportActionBar?.setTitle(resId)
    }

    fun setActionBarTitle(title: CharSequence) {
        supportActionBar?.title = title
    }
}
