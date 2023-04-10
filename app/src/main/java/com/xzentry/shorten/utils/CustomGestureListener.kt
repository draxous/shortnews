package com.xzentry.shorten.utils

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Swipe can happen up/down but not just straight. In an angled swipe up/down.
 * In this scenario VerticalViewPager is not triggering a touch event.
 * Only this type of a situation we can use CustomGestureListener to understand users
 * swipe gesture an manually trigger the swipe up/down action
 *
 */
//https://stackoverflow.com/questions/51281901/how-cand-i-add-swipe-up-down-detection-on-a-viewpager
abstract class CustomGestureListener(private val mView: View) :
    GestureDetector.SimpleOnGestureListener() {

    companion object {

        private const val swipe_Min_Distance = 100
        private const val swipe_Min_Velocity = 100

    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        mView.onTouchEvent(e)
        return super.onSingleTapConfirmed(e)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        onTouch()
        return false
    }

    override fun onFling(
        e1: MotionEvent, e2: MotionEvent, velocityX: Float,
        velocityY: Float
    ): Boolean {
        var velocityY = velocityY
        val yDistance = abs(e1.y - e2.y)
        velocityY = abs(velocityY)
        var result = false

        if (velocityY > swipe_Min_Velocity && yDistance > swipe_Min_Distance) {
            if (e1.y > e2.y)
                onAngledSwipeUp()
            else
                onAngledSwipeDown()
            result = true
        }
        return result
    }

    abstract fun onAngledSwipeUp(): Boolean
    abstract fun onAngledSwipeDown(): Boolean
    abstract fun onTouch(): Boolean
}