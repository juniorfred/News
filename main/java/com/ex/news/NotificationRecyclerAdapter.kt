package com.ex.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotificationRecyclerAdapter(private var items: MutableList<Notification>) : RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationRecyclerAdapter.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item,parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindNotification(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val titleLabel = view.findViewById<AppCompatTextView>(R.id.notificationTitle)
        private val dateLabel = view.findViewById<AppCompatTextView>(R.id.notificationTime)
        private val textLabel = view.findViewById<AppCompatTextView>(R.id.notificationText)

        fun bindNotification(notification: Notification){

            with(notification){

                val v = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(this.date)!!)

                titleLabel.text = this.title
                dateLabel.text = v
                textLabel.text = this.data
            }
        }
    }
}