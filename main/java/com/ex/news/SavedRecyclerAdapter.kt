package com.ex.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class SavedRecyclerAdapter(private val items: MutableList<VlogArticle>, var callback:(Int, VlogArticle)->Unit) : RecyclerView.Adapter<SavedRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =  ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.news_layout, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindProfile(items[position])
        holder.itemView.setOnClickListener {
            callback(0, items[position])
        }
        holder.itemView.findViewById<AppCompatImageButton>(R.id.saveArticle).setOnClickListener {
            callback(1,items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val newsHeadline = view.findViewById<AppCompatTextView>(R.id.newsHeadline)
        private val newsBannerPhotoOne = view.findViewById<AppCompatImageView>(R.id.bannerPhoto)
        private val saveIcon = view.findViewById<AppCompatImageButton>(R.id.saveArticle)
        private val articleDate = view.findViewById<AppCompatTextView>(R.id.articleDate)


        fun bindProfile(story: VlogArticle) {

            with(story) {

                articleDate.text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(this.publishDate)!!)

                saveIcon.setImageResource(R.drawable.saved)
                newsHeadline.text = this.headline
                //  newsSource.text = "${this.published_date}"

                //  Picasso.get().load(this.multimedia[0]?.url).into(newsImage)

                if(this.multimedia.isNullOrEmpty()){

                    newsBannerPhotoOne.visibility = View.GONE
                }else{

                    Picasso.get().load(this.multimedia[0]?.url).into(newsBannerPhotoOne)
                }
            }
        }
    }

   fun remove(data: VlogArticle){

        val index = items.indexOf(data)

        items.remove(data)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, itemCount)

    }
}