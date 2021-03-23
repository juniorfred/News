package com.ex.news

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ActivityOutdated : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outdated)

        findViewById<AppCompatButton>(R.id.exitBtn).setOnClickListener {

            val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.ace.vlog")

            try{

                startActivity(Intent(Intent.ACTION_VIEW, uri))

            }catch(e: ActivityNotFoundException){

                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()

            }

            finishAffinity()
        }
    }
}