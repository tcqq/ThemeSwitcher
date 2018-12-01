package com.example.themeswitcher.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.themeswitcher.R
import com.example.themeswitcher.event.SettingsEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        setActionBar(toolbar)
        setActionBarTitle(R.string.app_name)
        button_start.setOnClickListener {
            startActivity(Intent(this, ThemeSettingsActivity::class.java))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    /**
     * This event is used to change the theme.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingsEvent(event: SettingsEvent) {
        if (event.resetActivity) {
            Timber.i("Reset activity")
            recreate()
        }
    }

    override fun shouldShowBackArrow(): Boolean {
        return false
    }
}
