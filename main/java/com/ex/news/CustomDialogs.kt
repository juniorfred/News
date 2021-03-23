package com.ex.news

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView

class CustomDialogs {

    companion object {

        fun confirm(ctx: Context, msg: String, ok: String, cancel: String, callback: (Boolean) -> Unit) {

            val dialog = AlertDialog.Builder(ctx, R.style.AppTheme_DialogTransparent)
            val customDialog = LayoutInflater.from(ctx).inflate(R.layout.dx02, null)

            customDialog.findViewById<AppCompatTextView>(R.id.confirmMessage).text = msg

            dialog.setView(customDialog)
            dialog.setCancelable(false)

            val confirmDialogBox = dialog.create()
            confirmDialogBox.show()
            confirmDialogBox.window?.setLayout(560, confirmDialogBox.window?.attributes?.height!!)

            val okBtn = customDialog.findViewById<AppCompatButton>(R.id.okBtn)
            okBtn.text = ok
            okBtn.setOnClickListener {

                callback(true)
                confirmDialogBox.cancel()
            }

            val cancelBtn = customDialog.findViewById<AppCompatButton>(R.id.cancelBtn)
            cancelBtn.text = cancel
            cancelBtn.setOnClickListener {

                callback(false)
                confirmDialogBox.cancel()

            }


        }

        fun alert(ctx: Context, msg: String, btn: String, callback: () -> Unit) {

            val dialog = AlertDialog.Builder(ctx, R.style.AppTheme_DialogTransparent)
            val customDialog = LayoutInflater.from(ctx).inflate(R.layout.dx01, null)

            val alertText = customDialog.findViewById<AppCompatTextView>(R.id.message)
            val okBtn = customDialog.findViewById<AppCompatButton>(R.id.ok)

            alertText.text = msg
            okBtn.text = btn

            dialog.setView(customDialog)
            dialog.setCancelable(false)

            val alertDialogBox = dialog.create()
            alertDialogBox.show()
            alertDialogBox.window?.setLayout(560, alertDialogBox.window?.attributes?.height!!)

            okBtn.setOnClickListener {
                callback()
                alertDialogBox.cancel()
            }

        }

        fun rate(ctx: Context, callback: (Int) -> Unit) {

            val dialog = AlertDialog.Builder(ctx, R.style.AppTheme_DialogTransparent)
            val customDialog = LayoutInflater.from(ctx).inflate(R.layout.rating_dialog, null)

            dialog.setView(customDialog)
            dialog.setCancelable(false)

            val alertDialogBox = dialog.create()
            alertDialogBox.show()
            alertDialogBox.window?.setLayout(560, alertDialogBox.window?.attributes?.height!!)

            customDialog.findViewById<AppCompatButton>(R.id.reviewBtn).setOnClickListener {
                callback(0)
                alertDialogBox.cancel()
            }

            customDialog.findViewById<AppCompatButton>(R.id.laterBtn).setOnClickListener {
                callback(1)
                alertDialogBox.cancel()
            }

            customDialog.findViewById<AppCompatButton>(R.id.neverBtn).setOnClickListener {
                callback(2)
                alertDialogBox.cancel()
            }

        }

        fun hint (ctx: Context, msg: String) {

            val dialog = AlertDialog.Builder(ctx, R.style.AppTheme_DialogTransparent)
            val customDialog = LayoutInflater.from(ctx).inflate(R.layout.hint_dialog, null)

            customDialog.findViewById<AppCompatTextView>(R.id.hintMessage).text = msg

            dialog.setView(customDialog)
            dialog.setCancelable(false)

            val alertDialogBox = dialog.create()
            alertDialogBox.show()
            alertDialogBox.window?.setLayout(560, alertDialogBox.window?.attributes?.height!!)

            Handler().postDelayed({
                alertDialogBox.cancel()
            }, 2000)

        }

    }
}