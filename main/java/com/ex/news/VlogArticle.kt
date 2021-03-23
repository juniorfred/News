package com.ex.news

data class VlogArticle (val id: String, val type: String, val section: String, val publishDate: String, val headline: String, val url: String, val multimedia: List<VlogArticleMultimedia?>, val text: String) : StoriesAdapterItem() {

    override val itemType: Int get() = ARTICLES_TYPE
}
