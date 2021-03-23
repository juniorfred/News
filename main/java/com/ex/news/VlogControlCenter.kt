package com.ex.news

import android.content.Context

class VlogControlCenter {

    companion object {

        fun verifyInstaller(context: Context): Boolean{

            val playStoreAppId = "com.android.vending"

            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            return installer != null && installer.startsWith(playStoreAppId)
        }
    }


}