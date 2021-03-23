package com.ex.news

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val  gson = Gson()

        findViewById<AppCompatButton>(R.id.getStarted).setOnClickListener {

            val user = findViewById<TextInputEditText>(R.id.username).text?.trim().toString()
            val pass = findViewById<TextInputEditText>(R.id.password).text?.toString()!!

            if(TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)){

                Toast.makeText(this, "one or more missing fields", Toast.LENGTH_SHORT).show()
            }else{

                val intent = Intent()
                intent.putExtra("login", gson.toJson(User(user, pass)))
                setResult(Activity.RESULT_OK, intent)

                finish()
            }
        }

        findViewById<AppCompatTextView>(R.id.dontSignIn).setOnClickListener {

            finish()

        }
    }
}