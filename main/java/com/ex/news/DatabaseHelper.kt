package com.ex.news

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.ex.news.DatabaseFrame.Companion.ARTICLE
import com.ex.news.DatabaseFrame.Companion.ARTICLE_ID
import com.ex.news.DatabaseFrame.Companion.ARTICLE_TABLE
import com.ex.news.DatabaseFrame.Companion.CACHED_ARTICLES
import com.ex.news.DatabaseFrame.Companion.CAN_READ
import com.ex.news.DatabaseFrame.Companion.CMD_TABLE
import com.ex.news.DatabaseFrame.Companion.COLUMN_INDEX
import com.ex.news.DatabaseFrame.Companion.IS_OUTDATED
import com.ex.news.DatabaseFrame.Companion.IS_READ
import com.ex.news.DatabaseFrame.Companion.NOTIFICATION
import com.ex.news.DatabaseFrame.Companion.NOTIFICATION_TABLE
import com.ex.news.DatabaseFrame.Companion.PASSWORD_COLUMN
import com.ex.news.DatabaseFrame.Companion.PHOTO_COLUMN
import com.ex.news.DatabaseFrame.Companion.USERNAME_COLUMN
import com.ex.news.DatabaseFrame.Companion.USER_TABLE
import com.google.gson.Gson

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "VlogDB"
        private const val DATABASE_VERSION = 2
        const val query = "CREATE TABLE $USER_TABLE (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, $USERNAME_COLUMN TEXT, $PASSWORD_COLUMN TEXT, $PHOTO_COLUMN INTEGER)"
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val query0 = "CREATE TABLE $ARTICLE_TABLE (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, $ARTICLE_ID TEXT, $ARTICLE TEXT, $IS_READ INTEGER)"
        val query1 = "CREATE TABLE $CACHED_ARTICLES (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, $ARTICLE_ID TEXT, $ARTICLE TEXT, $IS_READ INTEGER)"
        val query2 = "CREATE TABLE $NOTIFICATION_TABLE (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, $NOTIFICATION TEXT)"
        val query4 = "CREATE TABLE  $CMD_TABLE (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, $IS_OUTDATED INTEGER, $CAN_READ TEXT)"

        db!!.execSQL(query)
        db.execSQL(query0)
        db.execSQL(query1)
        db.execSQL(query2)
        db.execSQL(query4)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldDb: Int, newDb: Int){

        db!!.execSQL("DROP TABLE IF EXISTS $USER_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $ARTICLE_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $CACHED_ARTICLES")
        db.execSQL("DROP TABLE IF EXISTS $NOTIFICATION_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $CMD_TABLE")

        onCreate(db)

    }

    var user: User?
        get() {

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT * FROM $USER_TABLE WHERE ${BaseColumns._ID}=1", null)
            var rslt: User? = null

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        val username = cursor.getString(cursor.getColumnIndex(USERNAME_COLUMN))
                        val password = cursor.getString(cursor.getColumnIndex(PASSWORD_COLUMN))

                        rslt = User(username, password)

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value)  {

            val r = listOf(0,1,2,3)

            val db:SQLiteDatabase = this.writableDatabase

            db.execSQL("DROP TABLE IF EXISTS $USER_TABLE")
            db.execSQL(query)

            val contentValues = ContentValues()

            contentValues.put(USERNAME_COLUMN, value?.username)
            contentValues.put(PASSWORD_COLUMN, value?.password)
            contentValues.put(PHOTO_COLUMN, r.random())

            val queryResult = db.insert(USER_TABLE, null, contentValues)
        }

    var username: String?
        get() {

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT $USERNAME_COLUMN FROM $USER_TABLE WHERE ${BaseColumns._ID}=$COLUMN_INDEX", null)
            var rslt: String? = null

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        rslt = cursor.getString(cursor.getColumnIndex(USERNAME_COLUMN))

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value) {

            val db:SQLiteDatabase = this.writableDatabase

            db.execSQL("UPDATE $USER_TABLE SET $USERNAME_COLUMN=$value WHERE ${BaseColumns._ID}=$COLUMN_INDEX")

        }

    var password: String?
        get() {

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT $PASSWORD_COLUMN FROM $USER_TABLE WHERE ${BaseColumns._ID}=$COLUMN_INDEX", null)
            var rslt: String? = null

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        rslt = cursor.getString(cursor.getColumnIndex(PASSWORD_COLUMN))

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value) {

            val db:SQLiteDatabase = this.writableDatabase

            db.execSQL("UPDATE $USER_TABLE SET $PASSWORD_COLUMN=$value WHERE ${BaseColumns._ID}=$COLUMN_INDEX")

        }

    var canRead: String?
        get() {

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT * FROM $CMD_TABLE", null)
            var rslt: String? = null

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        rslt = cursor.getString(cursor.getColumnIndex(CAN_READ))

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value) {

            val db:SQLiteDatabase = this.writableDatabase

            val query = "DELETE FROM $CMD_TABLE"
            val query0 ="VACUUM"

            db.execSQL(query)
            db.execSQL(query0)


            val contentValues = ContentValues()

            contentValues.put(IS_OUTDATED, 0)
            contentValues.put(CAN_READ, value)

            val queryResult = db.insert(CMD_TABLE, null, contentValues)

        }

    var isAppOutdated: Boolean
        get() {

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT * FROM $CMD_TABLE", null)
            var rslt = false

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        val i = when(cursor.getInt(cursor.getColumnIndex(IS_OUTDATED))){
                            1 -> true
                            0 -> false
                            else -> false}

                        rslt = i

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value) {

            val db:SQLiteDatabase = this.writableDatabase

            val query = "DELETE FROM $CMD_TABLE"
            val query0 ="VACUUM"

            db.execSQL(query)
            db.execSQL(query0)

            val i = when(value){
                true -> 1
                false -> 0
                else -> 0
            }

            val contentValues = ContentValues()

            contentValues.put(IS_OUTDATED, i)
            contentValues.put(CAN_READ, "#")

            val queryResult = db.insert(CMD_TABLE, null, contentValues)

        }

    var photo: Int?
        get() {

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT $PHOTO_COLUMN FROM $USER_TABLE WHERE ${BaseColumns._ID}=$COLUMN_INDEX", null)
            var rslt: Int? = null

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        rslt = cursor.getInt(cursor.getColumnIndex(PHOTO_COLUMN))

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value) {

            val db:SQLiteDatabase = this.writableDatabase

            db.execSQL("UPDATE $USER_TABLE SET $PHOTO_COLUMN=$value WHERE ${BaseColumns._ID}=$COLUMN_INDEX")

        }

    var cachedArticles: MutableList<VlogArticle>
        get(){

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT $ARTICLE FROM $CACHED_ARTICLES", null)
            val rslt: MutableList<VlogArticle> = mutableListOf()
            val  gson = Gson()

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        val a = gson.fromJson(cursor.getString(cursor.getColumnIndex(ARTICLE)), VlogArticle::class.java)
                        rslt.add(a)

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value){

            val  gson = Gson()

            val db = this.writableDatabase
            val contentValues = ContentValues()

            contentValues.put(ARTICLE_ID, value[0].id)
            contentValues.put(ARTICLE, gson.toJson(value[0]))
            contentValues.put(IS_READ, 0)

            val queryResult = db.insert(CACHED_ARTICLES, null, contentValues)
        }

    fun removeCachedArticle(id: String){

        val db:SQLiteDatabase = this.writableDatabase
        db.delete(CACHED_ARTICLES, "$ARTICLE_ID=?", arrayOf(id))

    }

    fun isCached(id: String): Boolean {

        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT $ARTICLE FROM $CACHED_ARTICLES WHERE $ARTICLE_ID=?", arrayOf(id))

        var res = false

        if(cursor != null){
            if (cursor.count >= 1) {

                res = true

                cursor.close()

            }
        }

        return res
    }

    var articles: MutableList<VlogArticle>
        get(){

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT $ARTICLE FROM $ARTICLE_TABLE", null)
            val rslt: MutableList<VlogArticle> = mutableListOf()
            val  gson = Gson()

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        val a = gson.fromJson(cursor.getString(cursor.getColumnIndex(ARTICLE)), VlogArticle::class.java)
                        rslt.add(a)

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value){

            val  gson = Gson()

            val db = this.writableDatabase
            val contentValues = ContentValues()

            contentValues.put(ARTICLE_ID, value[0].id)
            contentValues.put(ARTICLE, gson.toJson(value[0]))
            contentValues.put(IS_READ, 0)

            val queryResult = db.insert(ARTICLE_TABLE, null, contentValues)
        }

    var notification: MutableList<Notification>
        get(){

            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT $NOTIFICATION FROM $NOTIFICATION_TABLE", null)
            val rslt: MutableList<Notification> = mutableListOf()
            val  gson = Gson()

            if(cursor != null){
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    do {

                        val a = gson.fromJson(cursor.getString(cursor.getColumnIndex(NOTIFICATION)), Notification::class.java)
                        rslt.add(a)

                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }

            return rslt
        }
        set(value){

            val  gson = Gson()

            val db = this.writableDatabase
            val contentValues = ContentValues()

            contentValues.put(NOTIFICATION, gson.toJson(value[0]))

            val queryResult = db.insert(NOTIFICATION_TABLE, null, contentValues)
        }

    fun removeArticle(id: String){

        val db:SQLiteDatabase = this.writableDatabase
        db.delete(ARTICLE_TABLE, "$ARTICLE_ID=?", arrayOf(id))

    }

    fun isSaved(id: String): Boolean {

        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT $ARTICLE FROM $ARTICLE_TABLE WHERE $ARTICLE_ID=?", arrayOf(id))

       var res = false

        if(cursor != null){
            if (cursor.count >= 1) {

                    res = true

                cursor.close()

            }
        }

        return res
    }

    fun clearAll(tableCode: Int){

        val table = when(tableCode){
            0 -> USER_TABLE
            1 -> ARTICLE_TABLE
            2 -> CACHED_ARTICLES
            3 -> NOTIFICATION_TABLE
            else -> NOTIFICATION_TABLE
        }

        val db = this.writableDatabase
        val query = "DELETE FROM $table"
        val query0 ="VACUUM"

        db.execSQL(query)
        db.execSQL(query0)
    }
}