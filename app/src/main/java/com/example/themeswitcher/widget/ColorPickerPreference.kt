package com.example.themeswitcher.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.example.themeswitcher.R
import com.takisoft.colorpicker.ColorStateDrawable

/**
 * @author Perry Lance
 * @since 2019-04-08 Created
 */
class ColorPickerPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    Preference(context, attrs) {

    private var color: Int = 0
    private var colorWidget: ImageView? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference, 0, 0)

        color = a.getColor(R.styleable.ColorPickerPreference_pref_currentColor, 0)

        a.recycle()

        widgetLayoutResource = R.layout.preference_widget_color_swatch
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        colorWidget = holder.findViewById(R.id.color_picker_widget) as ImageView
        setColorOnWidget(color)
    }

    private fun setColorOnWidget(color: Int) {
        if (colorWidget == null) {
            return
        }

        val colorDrawable =
            arrayOf(ContextCompat.getDrawable(context, R.drawable.colorpickerpreference_pref_swatch)!!)
        colorWidget!!.setImageDrawable(ColorStateDrawable(colorDrawable, color))
    }

    private fun setInternalColor(color: Int, force: Boolean) {
        val oldColor = getPersistedInt(0)

        val changed = oldColor != color

        if (changed || force) {
            this.color = color

            persistInt(color)

            setColorOnWidget(color)

            notifyChanged()
        }
    }

    fun setColor(color: Int) {
        setInternalColor(color, false)
    }

    fun getColor(): Int {
        return color
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any? {
        return a!!.getString(index)
    }

    override fun onSetInitialValue(defaultValueObj: Any?) {
        val defaultValue = defaultValueObj as String?
        setInternalColor(
            getPersistedInt(if (!TextUtils.isEmpty(defaultValue)) Color.parseColor(defaultValue) else 0),
            true
        )
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        val myState = SavedState(superState)
        myState.color = color
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        val myState = state as SavedState?
        super.onRestoreInstanceState(myState!!.superState)
        color = myState.color
    }

    private class SavedState : BaseSavedState {
        var color: Int = 0

        constructor(source: Parcel) : super(source) {
            color = source.readInt()
        }

        constructor(superState: Parcelable) : super(superState) {}

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(color)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel) = SavedState(parcel)

                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }

    }
}
