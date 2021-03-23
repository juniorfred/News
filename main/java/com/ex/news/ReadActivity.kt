package com.ex.news

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ReadActivity : AppCompatActivity() {

    private lateinit var gson: Gson
    private lateinit var article: VlogArticle
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(SavedInstanceState: Bundle?){
        super.onCreate(SavedInstanceState)

        setContentView(R.layout.activity_read)

        val bannerPhoto = findViewById<AppCompatImageView>(R.id.readBannerPhoto)
        val headline = findViewById<AppCompatTextView>(R.id.readHeadline)
        val time = findViewById<AppCompatTextView>(R.id.publicationTime)
        val body = findViewById<WebView>(R.id.readBody)

        body.setBackgroundColor(0)
        body.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        body.webViewClient = object: WebViewClient(){

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)

                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                runOnUiThread {
                    findViewById<ProgressBar>(R.id.contentProgress).visibility = View.GONE
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)

                view?.loadUrl("file:///android_asset/webview_load_error.html")
            }

        }

        val bodySettings = body.settings
        bodySettings.javaScriptEnabled = false
        bodySettings.allowContentAccess = false
        bodySettings.allowFileAccess = false
        bodySettings.allowFileAccessFromFileURLs = false


        databaseHelper = DatabaseHelper(this)
        gson = Gson()

        if(intent.extras != null){

            article = gson.fromJson(intent.getStringExtra("article"), VlogArticle::class.java)

            Picasso.get().load(article.multimedia[0]?.url).into(bannerPhoto)
            headline.text = article.headline

            val dateInLocal = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(article.publishDate)
            val hrs = ((((Date().time - dateInLocal!!.time)/1000)/60)/60)
            val mins = (((Date().time - dateInLocal.time)/1000)/60) - (hrs * 60)
            val days = hrs/24

            when{

                days > 1 -> {

                    val a = "$days Days ago"
                    time.text = a
                }
                days > 0 -> {

                    val a = "$days Day ago"
                    time.text = a
                }
                hrs <= 0 -> {

                    val a = "$mins Mins ago"
                    time.text = a
                }
                mins <= 0 -> {

                    val a = "$hrs Hours ago"
                    time.text = a
                }
                else -> {

                    val a = "$hrs Hours $mins mins ago"
                    time.text = a
                }
            }

            val bodyText = "<html>" +
                    "<head>" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=yes\"/>" +
                    "<style>" +
                    "@font-face{font-family:'cf';src:url('file:///android_asset/fonts/poppins.ttf')}" +
                    "*{color:rgba(255,255,255, 0.6);font-family:cf;}" +
                    "img{width:300px;height:200px}" +
                    "a{color:dodgerblue;text-decoration:none}" +
                    "a::hover{color:dodgerblue;text-decoration:none}" +
                    "a::active{color:dodgerblue;text-decoration:none}" +
                    "a::visited{color:dodgerblue;text-decoration:none}" +
                    "figcaption{display:none;}" +
                    "@keyframes bigFont {" +
                    "from {" +
                    "font-size:14px;" +
                    "}" +
                    "to {" +
                    "font-size:18px;" +
                    "}" +
                    "}" +
                    "@keyframes smallFont{" +
                    "from {" +
                    "font-size:18px;" +
                    "}" +
                    "to {" +
                    "font-size:14px;" +
                    "}" +
                    "}" +
                    ".bdy {text-align:justify;width:100%;margin:0;padding:0;background-color:transparent;font-size:14px;}" +
                    ".bigBdy {text-align:justify;width:100%;margin:0;padding:0;background-color:transparent;font-size:16px;}" +
                    "</style>" +
                    "</head><body class=\"bigBdy\">${article.text}" +
                    "</body>" +
                    "</html>"

            body.loadData(bodyText, "text/html", "UTF-8")

        }

        if(databaseHelper.isSaved(article.id)){

            findViewById<AppCompatImageButton>(R.id.saveCurrentArticle).setImageResource(R.drawable.saved)
        }

        findViewById<AppCompatImageButton>(R.id.closeArticle).setOnClickListener {

            finish()
        }
        findViewById<AppCompatTextView>(R.id.readOriginal).setOnClickListener {

            val uri = Uri.parse(article.url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)

        }
        findViewById<AppCompatImageButton>(R.id.saveCurrentArticle).setOnClickListener {

            if(databaseHelper.isSaved(article.id)){
                databaseHelper.removeArticle(article.id)
                findViewById<AppCompatImageButton>(R.id.saveCurrentArticle).setImageResource(R.drawable.save)

            }else{
                databaseHelper.articles = mutableListOf(article)
                findViewById<AppCompatImageButton>(R.id.saveCurrentArticle).setImageResource(R.drawable.saved)
            }
        }
    }

    override fun finish() {

        val intent = Intent()
        intent.putExtra("article", gson.toJson(article))
        setResult(Activity.RESULT_OK, intent)

        super.finish()
    }

}
