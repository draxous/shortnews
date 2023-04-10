package com.xzentry.shorten.utils

import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.model.CardType
import com.xzentry.shorten.model.NewsData

//Delete this after we create way to add cartoons from the backend
fun List<Post?>.toPostWithCorrectCardType(): List<NewsData> {
    this.let { posts ->
        return posts.map {
            if (it?.category?.catId == 15) {
                NewsData(it, CardType.Cartoon)
            } else {
                NewsData(it, CardType.News)
            }
        }
    }
    return emptyList()
}