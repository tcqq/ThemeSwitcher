package com.example.themeswitcher.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.themeswitcher.manager.ThemeOverlayManager
import com.example.themeswitcher.utils.MenuColorize
import com.example.themeswitcher.utils.ThemeUtils
import com.example.themeswitcher.viewmodel.ToolbarViewModel
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import timber.log.Timber
import com.example.themeswitcher.R


/**
 * Base Activity class, used to initialize global configuration.
 *
 * @author Alan Dreamer
 * @since 2016/03/12 Created
 */
abstract class BaseActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeOverlays()
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val darkTheme = ThemeOverlayManager.isDarkTheme()
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> if (darkTheme) {
                Timber.d("Recreate Activity - UI_MODE_NIGHT_NO")
                recreate()
            }
            Configuration.UI_MODE_NIGHT_YES -> if (darkTheme.not()) {
                Timber.d("Recreate Activity - UI_MODE_NIGHT_YES")
                recreate()
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                Timber.w("Recreate Activity - UI_MODE_NIGHT_UNDEFINED")
                recreate()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val model = ViewModelProviders.of(this).get(ToolbarViewModel::class.java)
        model.menuResId.observe(this, Observer<Int> { menuResId ->
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            toolbar.menu.clear()
            menuInflater.inflate(menuResId, menu)
            ThemeOverlayManager.isDarkTheme()
            MenuColorize.colorMenu(
                this, menu,
                if (ThemeOverlayManager.isColorfulTheme(this) || ThemeOverlayManager.isDarkTheme()) Color.WHITE
                else ThemeUtils.getThemeValue(R.attr.colorSecondary, this)
            )
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

    fun setMenuFromResource(@MenuRes menuResId: Int) {
        ViewModelProviders.of(this).get(ToolbarViewModel::class.java).menuResId.value = menuResId
    }

    fun setActionBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        setBackArrow(shouldShowBackArrow())
    }

    fun setActionBarTitle(@StringRes resId: Int) {
        supportActionBar?.setTitle(resId)
    }

    open fun shouldShowBackArrow(): Boolean {
        return true
    }

    @SuppressLint("PrivateResource")
    private fun setBackArrow(showBackArrow: Boolean) {
        if (showBackArrow) {
            val actionBar = supportActionBar
            actionBar?.let {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeButtonEnabled(true)
                val backArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
                if (ThemeOverlayManager.isColorfulTheme(this) || ThemeOverlayManager.isDarkTheme()) backArrow?.setColorFilter(
                    Color.WHITE,
                    PorterDuff.Mode.SRC_ATOP
                )
                else backArrow?.setColorFilter(
                    ThemeUtils.getThemeValue(R.attr.colorSecondary, this),
                    PorterDuff.Mode.SRC_ATOP
                )
                actionBar.setHomeAsUpIndicator(backArrow)
            }
        }
    }

    private fun applyThemeOverlays() {
        delegate.setLocalNightMode(if (ThemeOverlayManager.isDarkTheme()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        setTheme(ThemeOverlayManager.getTheme())
    }
}
