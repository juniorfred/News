package com.ex.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class NewsRecyclerAdapter (val items: MutableList<StoriesAdapterItem>, var callback:(StoriesAdapterItem)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {

        return when (items[position].javaClass.simpleName) {
            VlogArticle::class.simpleName -> {
                ARTICLE_TYPE
            }
            else -> {
                NATIVE_AD_TYPE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        ARTICLE_TYPE -> {
            ArticleViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.news_layout, parent, false)
            )
        }
        else -> {
            NativeAdViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.native_ad, parent, false) as UnifiedNativeAdView
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        databaseHelper = DatabaseHelper(holder.itemView.context)
        when (holder.itemViewType) {
            ARTICLE_TYPE -> {
                (holder as ArticleViewHolder).bindProfile((items[position] as VlogArticle))

                if(databaseHelper.isSaved((items[position] as VlogArticle).id)){

                    holder.itemView.findViewById<AppCompatImageButton>(R.id.saveArticle).setImageResource(R.drawable.saved)

                }else{

                    holder.itemView.findViewById<AppCompatImageButton>(R.id.saveArticle).setImageResource(R.drawable.save)
                }

                holder.itemView.findViewById<AppCompatImageButton>(R.id.saveArticle).setOnClickListener {

                    if(databaseHelper.isSaved((items[position] as VlogArticle).id)){

                        databaseHelper.removeArticle((items[position] as VlogArticle).id)
                        holder.itemView.findViewById<AppCompatImageButton>(R.id.saveArticle).setImageResource(R.drawable.save)

                    }else{

                        databaseHelper.articles = mutableListOf((items[position] as VlogArticle))
                        holder.itemView.findViewById<AppCompatImageButton>(R.id.saveArticle).setImageResource(R.drawable.saved)
                    }
                }

                holder.itemView.setOnClickListener {
                    callback(items[position])
                }
            }
            else -> {
                (holder as NativeAdViewHolder).bindProfile((items[position] as NativeAd))
            }
        }

    }

    override fun getItemCount(): Int = items.size

    inner class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val newsHeadline = view.findViewById<AppCompatTextView>(R.id.newsHeadline)
        private val newsBannerPhotoOne = view.findViewById<AppCompatImageView>(R.id.bannerPhoto)
        private val articleDate = view.findViewById<AppCompatTextView>(R.id.articleDate)

        fun bindProfile(story: VlogArticle) {

            with(story) {

                articleDate.text = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault()).format(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(this.publishDate)!!)

                newsHeadline.text = this.headline

                if(this.multimedia.isNullOrEmpty()){

                    newsBannerPhotoOne.visibility = View.GONE

                }else{

                    Picasso.get().load(this.multimedia[0]?.url).into(newsBannerPhotoOne)
                }
            }
        }
    }

    inner class NativeAdViewHolder(private val view: UnifiedNativeAdView) : RecyclerView.ViewHolder(view) {

        private val iconView = view.findViewById<AppCompatImageView>(R.id.adIcon)
        private val titleView = view.findViewById<AppCompatTextView>(R.id.adTitle)
        private val ratingView = view.findViewById<AppCompatRatingBar>(R.id.adRating)
        private val adAdvertiser = view.findViewById<AppCompatTextView>(R.id.adAdvertiser)
        private val mediaView = view.findViewById<MediaView>(R.id.adMediaView)
        private val descriptionView = view.findViewById<AppCompatTextView>(R.id.adDescription)
        private val actionBtn = view.findViewById<AppCompatButton>(R.id.actionBtn)
        private val iconCard = view.findViewById<CardView>(R.id.imageCard)

        fun bindProfile(nativeAd: NativeAd) {

          with(nativeAd) {

                // icon view
                //Picasso.get().load(this.ad.icon.uri).into(iconView)

                if(this.ad.icon?.drawable != null){
                    iconView.setImageDrawable(this.ad.icon.drawable)
                }else{
                    iconCard.visibility = View.GONE
                }
                view.iconView = iconView

                // title view
                titleView.text = this.ad.headline
                view.headlineView = titleView

                if(this.ad.starRating != null){
                    ratingView.rating = this.ad.starRating.toFloat()
                }else{
                    ratingView.visibility = View.GONE
                }
                // rating view
                view.starRatingView = ratingView

                //advertiser view
                adAdvertiser.text = this.ad.advertiser
                view.advertiserView = adAdvertiser

                // media view
                mediaView.setMediaContent(this.ad.mediaContent)
                mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                view.mediaView = mediaView

                // description view
                descriptionView.text = this.ad.body
                view.bodyView = descriptionView



                // call to action
                actionBtn.text = this.ad.callToAction
                view.callToActionView = actionBtn

               view.setNativeAd(nativeAd.ad)
            }
        }
    }

    fun addList(newList: MutableList<StoriesAdapterItem>){
        items.addAll(0, newList)
        notifyItemRangeInserted(0, newList.size)
        notifyItemRangeChanged(0, itemCount)
    }

    fun addRandomPosition(newList: MutableList<StoriesAdapterItem>, count: Int){

        when(count){
            1 -> {
                items.addAll(count, newList)
                notifyItemRangeInserted(count, newList.size)
                notifyItemRangeChanged(count, itemCount)
            }
            else -> {

                if(items.size > 5 * (count - 1)){

                    val index = 5 * (count - 1)
                    items.addAll(index, newList)
                    notifyItemRangeInserted(index, newList.size)
                    notifyItemRangeChanged(index, itemCount)

                }else{

                    (newList[0] as NativeAd).ad.destroy()
                }

            }
        }

    }

    fun updateItemRange(){

        (items as MutableList<VlogArticle>).sortByDescending {

            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(it.publishDate)
        }
        notifyItemRangeChanged(0, itemCount)
    }

    companion object {

            private const val ARTICLE_TYPE = 1
            private const val NATIVE_AD_TYPE = 2
            private lateinit var databaseHelper: DatabaseHelper
        }

}

