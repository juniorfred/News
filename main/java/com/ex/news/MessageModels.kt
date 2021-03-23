package com.ex.news

data class Notification(val title: String, val data: String, val date: String)
data class Authenticate(val username: String?, val password: String?, val apkVersion: String)