package com.ex.news

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ActivityInvalidInstaller : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.invalid_installer)

        findViewById<AppCompatButton>(R.id.downloadBtn).setOnClickListener {

            val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.ace.vlog")

            try{

                startActivity(Intent(Intent.ACTION_VIEW, uri))

            }catch(e: ActivityNotFoundException){

                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()

            }
        }
    }
}