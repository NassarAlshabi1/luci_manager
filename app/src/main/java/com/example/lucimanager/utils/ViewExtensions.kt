package com.example.lucimanager.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.lucimanager.R
import com.google.android.material.snackbar.Snackbar

/**
 * Hide software keyboard
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Show snackbar with action
 */
fun Fragment.showSnackbar(
    message: String,
    actionText: String? = null,
    action: (() -> Unit)? = null,
    duration: Int = Snackbar.LENGTH_SHORT
) {
    view?.let { view ->
        val snackbar = Snackbar.make(view, message, duration)
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action() }
        }
        snackbar.show()
    }
}

/**
 * Show error snackbar
 */
fun Fragment.showErrorSnackbar(message: String) {
    view?.let { view ->
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackbar.setAction(getString(R.string.action_dismiss)) { snackbar.dismiss() }
        snackbar.show()
    }
}

/**
 * Show success snackbar
 */
fun Fragment.showSuccessSnackbar(message: String) {
    showSnackbar(message, duration = Snackbar.LENGTH_SHORT)
}
