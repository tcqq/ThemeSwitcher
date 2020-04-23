package com.example.themeswitcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author Perry Lance
 * @since 2018/08/19 Created
 */
class ToolbarViewModel : ViewModel() {
    val title: MutableLiveData<String> = MutableLiveData()
    val menuResId: MutableLiveData<Int> = MutableLiveData()
}