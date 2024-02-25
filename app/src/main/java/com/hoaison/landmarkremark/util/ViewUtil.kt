package com.hoaison.landmarkremark.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.hoaison.landmarkremark.constant.Constant

fun TextView.setTextIfEmpty(value: String?) {
    text = if (value.isNullOrEmpty()) Constant.NO_VALUE else value
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val isHidden = imm?.hideSoftInputFromWindow(view.windowToken, 0)
    //    Timber.d("hideKeyboard $isHidden")
}