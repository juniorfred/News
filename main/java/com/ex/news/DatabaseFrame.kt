package com.ex.news

import android.provider.BaseColumns

class DatabaseFrame {

    companion object : BaseColumns {

        const val USER_TABLE = "UserTable"
        const val USERNAME_COLUMN = "UserName"
        const val PASSWORD_COLUMN = "Password"
        const val PHOTO_COLUMN = "ProfilePhoto"

        const val ARTICLE_TABLE = "ArticleTable"
        const val ARTICLE_ID ="ArticleId"
        const val ARTICLE = "Article"
        const val IS_READ = "IsRead"
        const val CACHED_ARTICLES = "CachedArticles"

        const val NOTIFICATION_TABLE ="NotificationTable"
        const val NOTIFICATION = "Notification"

        const val CMD_TABLE = "CommandTable"
        const val CAN_READ = "CanRead"
        const val IS_OUTDATED = "IsOutdated"

        const val COLUMN_INDEX = 1

    }

}