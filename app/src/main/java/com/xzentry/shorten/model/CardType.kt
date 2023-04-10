package com.xzentry.shorten.model

sealed class CardType(var value: Int) {
    object News : CardType(0)
    object AdNews : CardType(1)
    object FullAd : CardType(2)
    object MultiPageAd : CardType(3)
    object NoConnection : CardType(4)
    object Last : CardType(5)
    object Intro : CardType(6)
    object IntroFb : CardType(7)
    object Pinned : CardType(8)
    object MultiPageParallaxAd : CardType(9)
    object VersionUpdate : CardType(10)
    object Cartoon : CardType(11)
    object GoogleSignIn : CardType(12)

    companion object {
        fun parse(value: Int): CardType {
            return when (value) {
                News.value -> News
                AdNews.value -> AdNews
                FullAd.value -> FullAd
                MultiPageAd.value -> MultiPageAd
                MultiPageParallaxAd.value -> MultiPageParallaxAd
                NoConnection.value -> NoConnection
                Last.value -> Last
                Intro.value -> Intro
                IntroFb.value -> IntroFb
                Pinned.value -> Pinned
                VersionUpdate.value -> VersionUpdate
                Cartoon.value -> Cartoon
                GoogleSignIn.value -> GoogleSignIn
                else -> News
            }
        }
    }
}