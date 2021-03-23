package com.ex.news

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActivityNotification : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        databaseHelper = DatabaseHelper(this)

        if(databaseHelper.notification.size <= 0){

           findViewById<LinearLayoutCompat>(R.id.notificationEmptyMessage).visibility = View.VISIBLE
        }else{

            findViewById<LinearLayoutCompat>(R.id.notificationEmptyMessage).visibility = View.GONE
        }

        val notificationRecycler = findViewById<RecyclerView>(R.id.notificationsRecycler)
        notificationRecycler.layoutManager = LinearLayoutManager(this)

        notificationRecycler.adapter = NotificationRecyclerAdapter(databaseHelper.notification)

        databaseHelper.clearAll(3)

    }

}