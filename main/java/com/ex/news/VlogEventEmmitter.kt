package com.ex.news

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent

class VlogEventEmmitter : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {

        jobFinished(params, true)

        sendBroadcast(Intent("com.ace.vlog")
            .putExtra("connected", true))

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {

        return false
    }
}