package com.bistrocart.utils

import android.content.Context
import android.content.DialogInterface
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.bistrocart.R


open class AppDialog {

    companion object {

        var dialog: AlertDialog? = null

        fun showNoNetworkDialog(context: Context): AlertDialog? {
            if (dialog != null) {
                dialog!!.dismiss()
            }
            val builder = AlertDialog.Builder(context)
            builder.setMessage(context.getString(R.string.msg_no_network))
            builder.setCancelable(true)
            builder.setPositiveButton(
                "OK"
            ) { dialog, which -> dialog.dismiss() }
            dialog = builder.create()
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.show()
            return dialog
        }

        fun showConfirmDialog(
            context: Context?,
            msg: String?,
            listener: AppDialogListener?
        ): AlertDialog? {
            if (dialog != null) {
                dialog!!.dismiss()
            }
            val builder = AlertDialog.Builder(
                context!!
            )
            builder.setMessage(msg)
            builder.setCancelable(true)
            builder.setPositiveButton("OK") { dialog, which ->
                if (listener != null) {
                    listener.onOkClick(dialog)
                } else {
                    dialog.dismiss()
                }
            }
            builder.setNegativeButton(
                "CANCEL"
            ) { dialog, which -> dialog.dismiss() }
            dialog = builder.create()
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.show()
            return dialog
        }

        fun showConfirmDialogyesNo(
            context: Context?,
            msg: String?,
            listener: AppDialogListener?
        ): AlertDialog? {
            if (dialog != null) {
                dialog!!.dismiss()
            }
            val builder = AlertDialog.Builder(
                context!!
            )
            builder.setMessage(msg)
            builder.setCancelable(true)
            builder.setPositiveButton("Yes") { dialog, which ->
                if (listener != null) {
                    listener.onOkClick(dialog)
                } else {
                    dialog.dismiss()
                }
            }
            builder.setNegativeButton(
                "No"
            ) { dialog, which -> dialog.dismiss() }
            dialog = builder.create()
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.show()
            return dialog
        }

        fun showOKDialogAlert(
            context: Context?,
            msg: String?,
            listener: AppDialogListener?
        ): AlertDialog? {
            if (dialog != null) {
                dialog!!.dismiss()
            }
            val builder = AlertDialog.Builder(
                context!!
            )
            builder.setCancelable(false)
            builder.setMessage(msg)
            builder.setPositiveButton(
                "OK"
            )
            { dialog, which ->
                if (listener != null) {
                    listener.onOkClick(dialog)
                } else {
                    dialog.dismiss()
                }
            }
            dialog = builder.create()
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.show()
            return dialog
        }

        interface AppDialogListener {
            fun onOkClick(dialog: DialogInterface?)
        }
    }
}