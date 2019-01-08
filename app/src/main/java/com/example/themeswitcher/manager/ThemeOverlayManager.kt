package com.example.themeswitcher.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.annotation.StyleRes
import com.example.themeswitcher.R
import com.example.themeswitcher.activity.BaseActivity
import com.example.themeswitcher.enums.ColorfulThemeColor
import com.example.themeswitcher.enums.ScheduleNightMode
import com.example.themeswitcher.model.*
import com.example.themeswitcher.model.pref.ThemeOverlayPref
import com.example.themeswitcher.model.pref.ThemeSettingsPref
import com.example.themeswitcher.receiver.NightModeReceiver
import com.takisoft.preferencex.TimePickerPreference
import com.trello.rxlifecycle3.android.ActivityEvent
import eu.davidea.flexibleadapter.utils.FlexibleUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

const val DEFAULT_THEME = R.style.ThemeOverlay_White_DeepPurple
const val DEFAULT_PRIMARY_COLOR_HAX = "#FFFFFF"
const val DEFAULT_SECONDARY_COLOR_HAX = "#673AB7"

/** Manager for theme overlays.  */
object ThemeOverlayManager {

    private val scheduleDisposables = CompositeDisposable()
    private val nightModeDisposables = CompositeDisposable()
    private val primaryColorDisposables = CompositeDisposable()

    private fun getCurrentDay(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + calendar.get(Calendar.MINUTE)
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return getCurrentDay().toString() + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE)
    }

    private fun getStartTimeTime(): String {
        return getStartHour().toString() + ":" + getStartMinute()
    }

    private fun getStartTimeDate(): String {
        return getCurrentDay().toString() + " " + getStartHour() + ":" + getStartMinute()
    }

    private fun getStartHour(): Int {
        return ThemeOverlayPref.startHourNightMode
    }

    private fun getStartMinute(): Int {
        return ThemeOverlayPref.startMinuteNightMode
    }

    private fun setStartTime(hour: Int, minute: Int) {
        ThemeOverlayPref.startHourNightMode = hour
        ThemeOverlayPref.startMinuteNightMode = minute
    }

    private fun getEndTime(): String {
        return getEndHour().toString() + ":" + getEndMinute()
    }

    private fun getEndDate(): String {
        return getCurrentDay().toString() + " " + getEndHour() + ":" + getEndMinute()
    }

    private fun getEndHour(): Int {
        return ThemeOverlayPref.endHourNightMode
    }

    private fun getEndMinute(): Int {
        return ThemeOverlayPref.endMinuteNightMode
    }

    private fun setEndTime(hour: Int, minute: Int) {
        ThemeOverlayPref.endHourNightMode = hour
        ThemeOverlayPref.endMinuteNightMode = minute
    }

    fun isDarkTheme(): Boolean {
        return ThemeOverlayPref.darkTheme
    }

    private fun setDarkTheme(darkTheme: Boolean) {
        ThemeOverlayPref.darkTheme = darkTheme
    }

    @StyleRes
    fun getTheme(): Int {
        return ThemeOverlayPref.theme
    }

    private fun setTheme(@StyleRes theme: Int) {
        ThemeOverlayPref.theme = theme
    }

    fun isColorfulTheme(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("switch_preference_colorful_theme", false)
    }

    /**
     * Initialize night mode to prevent night mode alarm from stopping after device reboot.
     */
    fun initNightModeAlarm(context: Context) {
        // If the NightModeReceiver is running, then return. (Reduce overhead)
        if (PreferenceManager.getDefaultSharedPreferences(context).getString("drop_down_preference_night_mode_schedule", "1") != "1") {
            setNightModeAlarm(context, isDarkTheme())
        }
    }

    fun updateNightMode(activity: BaseActivity?, context: Context) {
        val colorPickerModel = ColorPickerModel
        colorPickerModel.primaryColorHax = ThemeSettingsPref.primaryColorHax
        colorPickerModel.secondaryColorHax = ThemeSettingsPref.secondaryColorHax

        val dateFormat = SimpleDateFormat("dd HH:mm", Locale.getDefault())
        val currentDate = dateFormat.parse(getCurrentDate())
        val startDate = dateFormat.parse(getStartTimeDate())
        val endDate = dateFormat.parse(getEndDate())

        applyNightMode(activity, context, NightModeModel(when {
            currentDate.before(startDate) -> false
            currentDate.before(endDate) -> true
            endDate.before(startDate) -> true
            else -> false
        }, isColorfulTheme(context), ScheduleNightMode.CUSTOM_TIME))
    }

    fun dispose() {
        scheduleDisposables.clear()
        nightModeDisposables.clear()
        primaryColorDisposables.clear()
    }

    fun applySchedule(activity: BaseActivity?, context: Context, schedule: ScheduleNightMode) {
        val observable = Observable.create(ObservableOnSubscribe<ScheduleNightMode> {
            if (it.isDisposed) return@ObservableOnSubscribe
            it.onNext(schedule)
            it.onComplete()
        })

        val observer = object : DisposableObserver<ScheduleNightMode>() {
            override fun onComplete() {
            }

            override fun onNext(t: ScheduleNightMode) {
                Timber.d("applyNightMode > colorfulTheme: ${isColorfulTheme(context)}")
                applyNightMode(activity, context, NightModeModel(isScheduleDarkTheme(), isColorfulTheme(context), ScheduleNightMode.CUSTOM_TIME))
            }

            override fun onError(e: Throwable) {
                Timber.e(e.localizedMessage)
            }
        }

        observable
            .filter { it == ScheduleNightMode.CUSTOM_TIME }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)

        scheduleDisposables.add(observer)
    }

    fun applyStartTime(activity: BaseActivity, context: Context, timeWrapper: TimePickerPreference.TimeWrapper) {
        setStartTime(timeWrapper.hour, timeWrapper.minute)
        applyNightMode(activity, context, NightModeModel(isScheduleDarkTheme(), isColorfulTheme(context), ScheduleNightMode.CUSTOM_TIME))
    }

    fun applyEndTime(activity: BaseActivity, context: Context, timeWrapper: TimePickerPreference.TimeWrapper) {
        setEndTime(timeWrapper.hour, timeWrapper.minute)
        applyNightMode(activity, context, NightModeModel(isScheduleDarkTheme(), isColorfulTheme(context), ScheduleNightMode.CUSTOM_TIME))
    }

    fun applyNightMode(activity: BaseActivity?, context: Context, model: NightModeModel) {
        val observable = Observable.create(ObservableOnSubscribe<NightModeModel> {
            if (it.isDisposed) return@ObservableOnSubscribe
            it.onNext(model)
            it.onComplete()
        })

        val observer = object : DisposableObserver<NightModeModel>() {
            override fun onComplete() {
            }

            override fun onNext(t: NightModeModel) {
                when (t.scheduleNightMode) {
                    ScheduleNightMode.NONE -> {
                        cancelNightModeAlarm(context)
                        val colorPickerModel = ColorPickerModel
                        val primaryColorHax = colorPickerModel.primaryColorHax
                        setDarkTheme(t.darkTheme)
                        if (t.darkTheme) {
                            if (primaryColorHax != "#FFFFFF") ThemeSettingsPref.primaryColorHax = primaryColorHax
                            colorPickerModel.currentColorHax = "#000000"
                            applyPrimaryColor(activity, colorPickerModel, true)
                        } else {
                            colorPickerModel.currentColorHax = ThemeSettingsPref.primaryColorHax
                            applyPrimaryColor(activity, colorPickerModel, model.colorfulTheme)
                        }
                    }
                    ScheduleNightMode.CUSTOM_TIME -> {
                        setNightModeAlarm(context, t.darkTheme)
                        val colorPickerModel = ColorPickerModel
                        val primaryColorHax = colorPickerModel.primaryColorHax
                        val secondaryColorHax = colorPickerModel.secondaryColorHax
                        Timber.i("primaryColorHax: $primaryColorHax")
                        Timber.i("secondaryColorHax: $secondaryColorHax")
                        if (primaryColorHax != "#000000" && primaryColorHax != "#FFFFFF") {
                            ThemeSettingsPref.primaryColorHax = primaryColorHax
                            ThemeSettingsPref.secondaryColorHax = secondaryColorHax
                        }
                        Timber.i("darkTheme: ${t.darkTheme}")

                        if (t.darkTheme != isDarkTheme()) {
                            setDarkTheme(t.darkTheme)
                            if (t.darkTheme) {
                                if (primaryColorHax != "#FFFFFF") ThemeSettingsPref.primaryColorHax = primaryColorHax
                                colorPickerModel.currentColorHax = "#000000"
                                applyPrimaryColor(activity, colorPickerModel, true)
                            } else {
                                colorPickerModel.primaryColorHax = "#000000"
                                colorPickerModel.secondaryColorHax = ThemeSettingsPref.secondaryColorHax
                                colorPickerModel.currentColorHax = ThemeSettingsPref.primaryColorHax
                                applyPrimaryColor(activity, colorPickerModel, model.colorfulTheme)
                            }
                        }
                    }
                }
            }

            override fun onError(e: Throwable) {
                Timber.e(e.localizedMessage)
            }
        }

        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)

        nightModeDisposables.add(observer)
    }

    fun applyPrimaryColor(activity: BaseActivity?,
                          model: ColorPickerModel,
                          colorfulMode: Boolean) {

        val observable = Observable.create(ObservableOnSubscribe<ColorPickerModel> {
            if (it.isDisposed) return@ObservableOnSubscribe
            if (model.currentColorHax != model.secondaryColorHax) {
                it.onNext(model)
                it.onComplete()
            }

/*            if ((model.primaryColorHax != "#000000" &&
                            model.primaryColorHax != "#FFFFFF" &&
                            model.currentColorHax == model.primaryColorHax).not()) {
                it.onNext(model)
                it.onComplete()
            }*/
        })

        val observer = object : DisposableObserver<Int>() {
            override fun onComplete() {
                activity?.recreate()
            }

            override fun onNext(t: Int) {
                Timber.i("secondaryColorHax: ${model.secondaryColorHax}")
                if (model.currentColorHax != "#000000"
                    && model.currentColorHax != "#FFFFFF") ThemeSettingsPref.primaryColorHax = model.currentColorHax
                setTheme(t)
                SettingsModel.theme = t
            }

            override fun onError(e: Throwable) {
                Timber.e(e.localizedMessage)
            }
        }

        observable
            .map {
                if (colorfulMode) {
                    Timber.d("generateColorfulTheme > primaryColor: ${it.currentColorHax} - secondaryColor: ${it.secondaryColorHax}")
                    generateColorfulTheme(getColorfulThemeColor(it.currentColorHax), getColorfulThemeColor(it.secondaryColorHax))
                } else {
                    Timber.d("generateTheme > primaryColor: #FFFFFF - secondaryColor: ${it.secondaryColorHax}")
                    generateTheme(getColorfulThemeColor(it.secondaryColorHax))
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)

        primaryColorDisposables.add(observer)
    }

    fun applySecondaryColor(activity: BaseActivity,
                            model: ColorPickerModel,
                            colorfulMode: Boolean,
                            nightMode: Boolean) {
        Observable
            .just(model)
            .map {
                when {
                    nightMode -> generateColorfulTheme(getColorfulThemeColor("#000000"), getColorfulThemeColor(it.currentColorHax))
                    colorfulMode -> generateColorfulTheme(getColorfulThemeColor(it.primaryColorHax), getColorfulThemeColor(it.currentColorHax))
                    else -> generateTheme(getColorfulThemeColor(it.currentColorHax))
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(activity.bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(object : Observer<Int?> {
                override fun onComplete() {
                    activity.recreate()
                }

                override fun onSubscribe(d: Disposable) {
                    Timber.i("currentColorHax: ${model.currentColorHax}")
                    Timber.i("secondaryColorHax: ${model.secondaryColorHax}")
                    if (model.currentColorHax == model.secondaryColorHax) d.dispose()
                }

                override fun onNext(t: Int) {
                    FlexibleUtils.resetAccentColor()
                    Timber.i("primaryColorHax: ${model.primaryColorHax}")
                    if (model.currentColorHax != "#000000"
                        && model.currentColorHax != "#FFFFFF") ThemeSettingsPref.secondaryColorHax = model.currentColorHax
                    setTheme(t)
                    SettingsModel.theme = t
                }

                override fun onError(e: Throwable) {
                    Timber.e(e.localizedMessage)
                }
            })
    }

    fun applyColorfulTheme(activity: BaseActivity, model: ColorfulThemeModel) {
        Observable
            .just(model)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(activity.bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(object : Observer<ColorfulThemeModel?> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: ColorfulThemeModel) {
                    val colorPickerModel = ColorPickerModel
                    val primaryColorHax = colorPickerModel.primaryColorHax
                    if (t.colorfulMode) {
                        if (isDarkTheme()) {
                            colorPickerModel.currentColorHax = "#000000"
                            applyPrimaryColor(null, colorPickerModel, true)
                        } else {
                            colorPickerModel.primaryColorHax = "#FFFFFF"
                            colorPickerModel.currentColorHax = ThemeSettingsPref.primaryColorHax
                            applyPrimaryColor(activity, colorPickerModel, true)
                        }
                    } else {
                        if (isDarkTheme()) {
                            colorPickerModel.currentColorHax = "#000000"
                            applyPrimaryColor(null, colorPickerModel, true)
                        } else {
                            ThemeSettingsPref.primaryColorHax = primaryColorHax
                            colorPickerModel.currentColorHax = "#FFFFFF"
                            applyPrimaryColor(activity, colorPickerModel, false)
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    Timber.e(e.localizedMessage)
                }
            })
    }

    private fun setNightModeAlarm(context: Context, darkTheme: Boolean) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val i = Intent(context, NightModeReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, 0)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.parse(getCurrentTime())
        val startTime = timeFormat.parse(getStartTimeTime())
        val endTime = timeFormat.parse(getEndTime())
        val calendar = Calendar.getInstance()
        if (startTime == endTime) {
            Timber.d("Night mode alert has been canceled")
            am?.cancel(pi)
        } else {
            if (darkTheme) {
                if (endTime.after(currentTime).not()) {
                    // Note: The calendar month starts from 0 instead of 1
                    Timber.d(("${calendar.get(Calendar.YEAR)}" + "-"
                            + "${calendar.get(Calendar.MONTH) + 1}" + "-"
                            + "${calendar.get(Calendar.DAY_OF_MONTH) + 1}" + "-"
                            + "${getEndHour()}" + ":"
                            + "${getEndMinute()}"
                            + " will TURN OFF night mode"))
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) + 1, getEndHour(), getEndMinute(), 0)
                    am?.setRepeating(AlarmManager.RTC, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
                } else {
                    Timber.d(("${calendar.get(Calendar.YEAR)}" + "-"
                            + "${calendar.get(Calendar.MONTH) + 1}" + "-"
                            + "${calendar.get(Calendar.DAY_OF_MONTH)}" + "-"
                            + "${getEndHour()}" + ":"
                            + "${getEndMinute()}"
                            + " will TURN OFF night mode"))
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), getEndHour(), getEndMinute(), 0)
                    am?.setRepeating(AlarmManager.RTC, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
                }
            } else {
                if (startTime.before(currentTime)) {
                    Timber.d(("${calendar.get(Calendar.YEAR)}" + "-"
                            + "${calendar.get(Calendar.MONTH) + 1}" + "-"
                            + "${calendar.get(Calendar.DAY_OF_MONTH) + 1}" + "-"
                            + "${getStartHour()}" + ":"
                            + "${getStartMinute()}"
                            + " will TURN ON night mode"))
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) + 1, getStartHour(), getStartMinute(), 0)
                    am?.setRepeating(AlarmManager.RTC, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
                } else {
                    Timber.d(("${calendar.get(Calendar.YEAR)}" + "-"
                            + "${calendar.get(Calendar.MONTH) + 1}" + "-"
                            + "${calendar.get(Calendar.DAY_OF_MONTH)}" + "-"
                            + "${getStartHour()}" + ":"
                            + "${getStartMinute()}"
                            + " will TURN ON night mode"))
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), getStartHour(), getStartMinute(), 0)
                    am?.setRepeating(AlarmManager.RTC, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
                }
            }
        }
    }

    private fun cancelNightModeAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val i = Intent(context, NightModeReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, 0)
        am?.cancel(pi)
    }

    private fun isScheduleDarkTheme(): Boolean {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.parse(getCurrentTime())
        val startTime = timeFormat.parse(getStartTimeTime())
        val endTime = timeFormat.parse(getEndTime())
        return if (startTime == endTime) {
            false
        } else {
            if (startTime.after(currentTime).not()) {
                if (currentTime.before(endTime)) {
                    Timber.v("Schedule: A")
                    true
                } else {
                    Timber.v("Schedule: B")
                    endTime.before(startTime)
                }
            } else {
                if (endTime.before(currentTime)) {
                    Timber.v("Schedule: C")
                    false
                } else {
                    Timber.v("Schedule: D")
                    startTime.before(endTime).not()
                }
            }
        }
    }

    private fun getColorfulThemeColor(colorHax: String): ColorfulThemeColor {
        return when (colorHax) {
            "#F44336" -> ColorfulThemeColor.RED
            "#E91E63" -> ColorfulThemeColor.PINK
            "#9C27B0" -> ColorfulThemeColor.PURPLE
            "#673AB7" -> ColorfulThemeColor.DEEP_PURPLE
            "#3F51B5" -> ColorfulThemeColor.INDIGO

            "#2196F3" -> ColorfulThemeColor.BLUE
            "#03A9F4" -> ColorfulThemeColor.LIGHT_BLUE
            "#00BCD4" -> ColorfulThemeColor.CYAN
            "#009688" -> ColorfulThemeColor.TEAL
            "#4CAF50" -> ColorfulThemeColor.GREEN

            "#8BC34A" -> ColorfulThemeColor.LIGHT_GREEN
            "#CDDC39" -> ColorfulThemeColor.LIME
            "#FFEB3B" -> ColorfulThemeColor.YELLOW
            "#FFC107" -> ColorfulThemeColor.AMBER
            "#FF9800" -> ColorfulThemeColor.ORANGE

            "#FF5722" -> ColorfulThemeColor.DEEP_ORANGE
            "#795548" -> ColorfulThemeColor.BROWN
            "#9E9E9E" -> ColorfulThemeColor.GREY
            "#607D8B" -> ColorfulThemeColor.BLUE_GREY
            "#000000" -> ColorfulThemeColor.BLACK
            else -> ColorfulThemeColor.INDIGO
        }
    }

    @StyleRes
    private fun generateTheme(secondaryColor: ColorfulThemeColor): Int {
        return when (secondaryColor) {
            ColorfulThemeColor.RED -> R.style.ThemeOverlay_White_Red
            ColorfulThemeColor.PINK -> R.style.ThemeOverlay_White_Pink
            ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_White_Purple
            ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_White_DeepPurple
            ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_White_Indigo
            ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_White_Blue
            ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_White_LightBlue
            ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_White_Cyan
            ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_White_Teal
            ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_White_Green
            ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_White_LightGreen
            ColorfulThemeColor.LIME -> R.style.ThemeOverlay_White_Lime
            ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_White_Yellow
            ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_White_Amber
            ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_White_Orange
            ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_White_DeepOrange
            ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_White_Brown
            ColorfulThemeColor.GREY -> R.style.ThemeOverlay_White_Grey
            ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_White_BlueGrey
            ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_White_Black
        }
    }

    @StyleRes
    private fun generateColorfulTheme(primaryColor: ColorfulThemeColor, secondaryColor: ColorfulThemeColor): Int {
        return when (primaryColor) {
            ColorfulThemeColor.RED -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Red_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Red_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Red_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Red_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Red_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Red_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Red_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Red_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Red_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Red_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Red_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Red_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Red_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Red_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Red_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Red_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Red_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Red_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Red_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Red_Black
                }
            }
            ColorfulThemeColor.PINK -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Pink_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Pink_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Pink_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Pink_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Pink_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Pink_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Pink_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Pink_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Pink_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Pink_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Pink_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Pink_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Pink_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Pink_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Pink_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Pink_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Pink_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Pink_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Pink_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Pink_Black
                }
            }
            ColorfulThemeColor.PURPLE -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Purple_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Purple_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Purple_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Purple_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Purple_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Purple_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Purple_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Purple_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Purple_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Purple_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Purple_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Purple_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Purple_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Purple_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Purple_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Purple_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Purple_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Purple_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Purple_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Purple_Black
                }
            }
            ColorfulThemeColor.DEEP_PURPLE -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_DeepPurple_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_DeepPurple_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_DeepPurple_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_DeepPurple_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_DeepPurple_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_DeepPurple_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_DeepPurple_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_DeepPurple_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_DeepPurple_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_DeepPurple_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_DeepPurple_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_DeepPurple_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_DeepPurple_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_DeepPurple_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_DeepPurple_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_DeepPurple_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_DeepPurple_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_DeepPurple_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_DeepPurple_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_DeepPurple_Black
                }
            }
            ColorfulThemeColor.INDIGO -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Indigo_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Indigo_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Indigo_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Indigo_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Indigo_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Indigo_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Indigo_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Indigo_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Indigo_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Indigo_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Indigo_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Indigo_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Indigo_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Indigo_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Indigo_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Indigo_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Indigo_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Indigo_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Indigo_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Indigo_Black
                }
            }
            ColorfulThemeColor.BLUE -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Blue_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Blue_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Blue_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Blue_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Blue_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Blue_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Blue_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Blue_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Blue_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Blue_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Blue_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Blue_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Blue_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Blue_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Blue_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Blue_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Blue_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Blue_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Blue_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Blue_Black
                }
            }
            ColorfulThemeColor.LIGHT_BLUE -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_LightBlue_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_LightBlue_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_LightBlue_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_LightBlue_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_LightBlue_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_LightBlue_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_LightBlue_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_LightBlue_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_LightBlue_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_LightBlue_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_LightBlue_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_LightBlue_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_LightBlue_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_LightBlue_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_LightBlue_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_LightBlue_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_LightBlue_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_LightBlue_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_LightBlue_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_LightBlue_Black
                }
            }
            ColorfulThemeColor.CYAN -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Cyan_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Cyan_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Cyan_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Cyan_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Cyan_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Cyan_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Cyan_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Cyan_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Cyan_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Cyan_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Cyan_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Cyan_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Cyan_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Cyan_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Cyan_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Cyan_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Cyan_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Cyan_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Cyan_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Cyan_Black
                }
            }
            ColorfulThemeColor.TEAL -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Teal_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Teal_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Teal_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Teal_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Teal_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Teal_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Teal_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Teal_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Teal_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Teal_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Teal_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Teal_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Teal_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Teal_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Teal_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Teal_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Teal_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Teal_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Teal_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Teal_Black
                }
            }
            ColorfulThemeColor.GREEN -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Green_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Green_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Green_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Green_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Green_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Green_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Green_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Green_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Green_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Green_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Green_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Green_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Green_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Green_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Green_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Green_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Green_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Green_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Green_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Green_Black
                }
            }
            ColorfulThemeColor.LIGHT_GREEN -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_LightGreen_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_LightGreen_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_LightGreen_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_LightGreen_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_LightGreen_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_LightGreen_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_LightGreen_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_LightGreen_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_LightGreen_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_LightGreen_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_LightGreen_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_LightGreen_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_LightGreen_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_LightGreen_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_LightGreen_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_LightGreen_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_LightGreen_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_LightGreen_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_LightGreen_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_LightGreen_Black
                }
            }
            ColorfulThemeColor.LIME -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Lime_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Lime_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Lime_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Lime_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Lime_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Lime_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Lime_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Lime_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Lime_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Lime_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Lime_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Lime_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Lime_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Lime_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Lime_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Lime_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Lime_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Lime_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Lime_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Lime_Black
                }
            }
            ColorfulThemeColor.YELLOW -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Yellow_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Yellow_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Yellow_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Yellow_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Yellow_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Yellow_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Yellow_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Yellow_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Yellow_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Yellow_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Yellow_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Yellow_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Yellow_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Yellow_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Yellow_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Yellow_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Yellow_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Yellow_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Yellow_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Yellow_Black
                }
            }
            ColorfulThemeColor.AMBER -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Amber_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Amber_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Amber_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Amber_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Amber_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Amber_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Amber_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Amber_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Amber_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Amber_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Amber_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Amber_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Amber_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Amber_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Amber_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Amber_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Amber_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Amber_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Amber_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Amber_Black
                }
            }
            ColorfulThemeColor.ORANGE -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Orange_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Orange_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Orange_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Orange_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Orange_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Orange_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Orange_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Orange_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Orange_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Orange_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Orange_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Orange_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Orange_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Orange_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Orange_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Orange_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Orange_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Orange_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Orange_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Orange_Black
                }
            }
            ColorfulThemeColor.DEEP_ORANGE -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_DeepOrange_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_DeepOrange_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_DeepOrange_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_DeepOrange_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_DeepOrange_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_DeepOrange_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_DeepOrange_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_DeepOrange_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_DeepOrange_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_DeepOrange_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_DeepOrange_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_DeepOrange_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_DeepOrange_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_DeepOrange_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_DeepOrange_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_DeepOrange_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_DeepOrange_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_DeepOrange_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_DeepOrange_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_DeepOrange_Black
                }
            }
            ColorfulThemeColor.BROWN -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Brown_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Brown_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Brown_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Brown_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Brown_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Brown_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Brown_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Brown_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Brown_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Brown_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Brown_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Brown_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Brown_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Brown_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Brown_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Brown_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Brown_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Brown_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Brown_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Brown_Black
                }
            }
            ColorfulThemeColor.GREY -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Grey_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Grey_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Grey_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Grey_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Grey_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Grey_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Grey_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Grey_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Grey_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Grey_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Grey_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Grey_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Grey_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Grey_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Grey_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Grey_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Grey_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Grey_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Grey_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Grey_Black
                }
            }
            ColorfulThemeColor.BLUE_GREY -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_BlueGrey_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_BlueGrey_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_BlueGrey_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_BlueGrey_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_BlueGrey_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_BlueGrey_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_BlueGrey_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_BlueGrey_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_BlueGrey_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_BlueGrey_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_BlueGrey_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_BlueGrey_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_BlueGrey_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_BlueGrey_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_BlueGrey_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_BlueGrey_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_BlueGrey_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_BlueGrey_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_BlueGrey_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_BlueGrey_Black
                }
            }
            ColorfulThemeColor.BLACK -> {
                when (secondaryColor) {
                    ColorfulThemeColor.RED -> R.style.ThemeOverlay_Black_Red
                    ColorfulThemeColor.PINK -> R.style.ThemeOverlay_Black_Pink
                    ColorfulThemeColor.PURPLE -> R.style.ThemeOverlay_Black_Purple
                    ColorfulThemeColor.DEEP_PURPLE -> R.style.ThemeOverlay_Black_DeepPurple
                    ColorfulThemeColor.INDIGO -> R.style.ThemeOverlay_Black_Indigo
                    ColorfulThemeColor.BLUE -> R.style.ThemeOverlay_Black_Blue
                    ColorfulThemeColor.LIGHT_BLUE -> R.style.ThemeOverlay_Black_LightBlue
                    ColorfulThemeColor.CYAN -> R.style.ThemeOverlay_Black_Cyan
                    ColorfulThemeColor.TEAL -> R.style.ThemeOverlay_Black_Teal
                    ColorfulThemeColor.GREEN -> R.style.ThemeOverlay_Black_Green
                    ColorfulThemeColor.LIGHT_GREEN -> R.style.ThemeOverlay_Black_LightGreen
                    ColorfulThemeColor.LIME -> R.style.ThemeOverlay_Black_Lime
                    ColorfulThemeColor.YELLOW -> R.style.ThemeOverlay_Black_Yellow
                    ColorfulThemeColor.AMBER -> R.style.ThemeOverlay_Black_Amber
                    ColorfulThemeColor.ORANGE -> R.style.ThemeOverlay_Black_Orange
                    ColorfulThemeColor.DEEP_ORANGE -> R.style.ThemeOverlay_Black_DeepOrange
                    ColorfulThemeColor.BROWN -> R.style.ThemeOverlay_Black_Brown
                    ColorfulThemeColor.GREY -> R.style.ThemeOverlay_Black_Grey
                    ColorfulThemeColor.BLUE_GREY -> R.style.ThemeOverlay_Black_BlueGrey
                    ColorfulThemeColor.BLACK -> R.style.ThemeOverlay_Black_Black
                }
            }
        }
    }
}
