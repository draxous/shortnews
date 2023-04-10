package com.xzentry.shorten.model

import com.xzentry.shorten.data.remote.model.AdsModel
import com.xzentry.shorten.data.remote.model.Post

class NewsData(private val postEntity: Post? ,private val cardType: CardType,private val adData: AdsModel? = null) {

    fun getCardType(): CardType {
        return cardType
    }

    fun getPostItem(): Post? {
        return postEntity
    }

    fun getAd(): AdsModel? {
        return adData
    }

}
