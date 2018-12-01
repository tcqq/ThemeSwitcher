package com.example.themeswitcher.viewmodel

import androidx.annotation.MenuRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author Alan Dreamer
 * @since 2018/08/19 Created
 */
class ToolbarViewModel : ViewModel() {
    val title: MutableLiveData<String> = MutableLiveData()
    @MenuRes
    val menuResId: MutableLiveData<Int> = MutableLiveData()
}