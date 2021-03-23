package com.ex.news

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class ActivitySaved : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var gson: Gson
    private lateinit var newsRecycler: RecyclerView
    private lateinit var atcls: MutableList<VlogArticle>

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.saved_news)

        databaseHelper = DatabaseHelper(this)
        gson = Gson()

        atcls = databaseHelper.articles
        atcls.reverse()

        if(atcls.size <= 0){

            findViewById<LinearLayoutCompat>(R.id.emptySavedArticles).visibility = View.VISIBLE
        }else{
            findViewById<LinearLayoutCompat>(R.id.emptySavedArticles).visibility = View.GONE
        }

        val gson = Gson()

        newsRecycler = findViewById(R.id.savedRecyclerview)
        val linearLayoutManager = LinearLayoutManager(this)
        newsRecycler.layoutManager = linearLayoutManager

        newsRecycler.adapter = SavedRecyclerAdapter(atcls){one, two ->

            if(one == 1){

                databaseHelper.removeArticle(two.id)
                (newsRecycler.adapter as SavedRecyclerAdapter).remove(two)

                if((newsRecycler.adapter as SavedRecyclerAdapter).itemCount <= 0){

                    findViewById<LinearLayoutCompat>(R.id.emptySavedArticles).visibility = View.VISIBLE
                }else{
                    findViewById<LinearLayoutCompat>(R.id.emptySavedArticles).visibility = View.GONE
                }

            }else{

                val intent = Intent(this@ActivitySaved, ReadActivity::class.java)
                intent.putExtra("article", gson.toJson(two))
                startActivityForResult(intent, REQUEST_CODE)

            }
        }

    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)

         if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){

             val obj = gson.fromJson(data?.getStringExtra("article"), VlogArticle::class.java)

             if(!databaseHelper.isSaved(obj.id)){

                 (newsRecycler.adapter as SavedRecyclerAdapter).remove(obj)

                 if((newsRecycler.adapter as SavedRecyclerAdapter).itemCount <= 0){

                     findViewById<LinearLayoutCompat>(R.id.emptySavedArticles).visibility = View.VISIBLE
                 }else{
                     findViewById<LinearLayoutCompat>(R.id.emptySavedArticles).visibility = View.GONE
                 }

             }

         }

     }

    companion object {
        private const val REQUEST_CODE = 1
    }

}
