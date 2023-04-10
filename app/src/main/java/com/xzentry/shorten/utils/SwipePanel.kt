package com.xzentry.shorten.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import com.xzentry.shorten.R
import com.xzentry.shorten.common.error
import java.lang.Exception
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.math.abs
import kotlin.math.min

class SwipePanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    @IntDef(
        LEFT,
        TOP,
        RIGHT,
        BOTTOM
    )
    @Retention(RetentionPolicy.SOURCE)
    annotation class Direction

    private var mWidth = 0
    private var mHeight = 0
    private val mPaint: Paint
    private val halfSize: Float
    private val unit: Float
    private val mTouchSlop: Int
    private val mPath = arrayOfNulls<Path>(4)
    private val mPaintColor = IntArray(4)
    private val mEdgeSizes = IntArray(4)
    private val mDrawables =
        arrayOfNulls<Drawable>(4)
    private val mIsStart = BooleanArray(4)
    private val mDown = FloatArray(4)
    private val progresses = FloatArray(4)
    private val preProgresses = FloatArray(4)
    private val mIsCenter = BooleanArray(4)
    private val mEnabled = booleanArrayOf(true, true, true, true)
    private var mDownX = 0f
    private var mDownY = 0f
    private var mCurrentX = 0f
    private var mCurrentY = 0f
    private val mRect = Rect()
    private var mIsEdgeStart = false
    private var mStartDirection = -1
    private var mLimit = 0
    private var mListener: OnFullSwipeListener? = null
    private var mProgressListener: OnProgressChangedListener? =
        null

    fun setLeftSwipeColor(color: Int) {
        setSwipeColor(color, LEFT)
    }

    fun setTopSwipeColor(color: Int) {
        setSwipeColor(color, TOP)
    }

    fun setRightSwipeColor(color: Int) {
        setSwipeColor(color, RIGHT)
    }

    fun setBottomSwipeColor(color: Int) {
        setSwipeColor(color, BOTTOM)
    }

    private fun setSwipeColor(color: Int, direction: Int) {
        mPaintColor[direction] = color
    }

    fun setLeftEdgeSize(size: Int) {
        mEdgeSizes[LEFT] = size
    }

    fun setTopEdgeSize(size: Int) {
        mEdgeSizes[TOP] = size
    }

    fun setRightEdgeSize(size: Int) {
        mEdgeSizes[RIGHT] = size
    }

    fun setBottomEdgeSize(size: Int) {
        mEdgeSizes[BOTTOM] = size
    }

    fun setLeftDrawable(@DrawableRes drawableId: Int) {
        setDrawable(drawableId, LEFT)
    }

    fun setTopDrawable(@DrawableRes drawableId: Int) {
        setDrawable(drawableId, TOP)
    }

    fun setRightDrawable(@DrawableRes drawableId: Int) {
        setDrawable(drawableId, RIGHT)
    }

    fun setBottomDrawable(@DrawableRes drawableId: Int) {
        setDrawable(drawableId, BOTTOM)
    }

    private fun setDrawable(drawableId: Int, direction: Int) {
        mDrawables[direction] =
            getDrawable(context, drawableId)
    }

    fun setLeftDrawable(drawable: Drawable?) {
        setDrawable(drawable, LEFT)
    }

    fun setTopDrawable(drawable: Drawable?) {
        setDrawable(drawable, TOP)
    }

    fun setRightDrawable(drawable: Drawable?) {
        setDrawable(drawable, RIGHT)
    }

    fun setBottomDrawable(drawable: Drawable?) {
        setDrawable(drawable, BOTTOM)
    }

    private fun setDrawable(
        drawable: Drawable?,
        direction: Int
    ) {
        mDrawables[direction] = drawable
    }

    val leftDrawable: Drawable?
        get() = mDrawables[LEFT]

    val topDrawable: Drawable?
        get() = mDrawables[TOP]

    val rightDrawable: Drawable?
        get() = mDrawables[RIGHT]

    val bottomDrawable: Drawable?
        get() = mDrawables[BOTTOM]

    fun setLeftCenter(isCenter: Boolean) {
        setCenter(isCenter, LEFT)
    }

    fun setTopCenter(isCenter: Boolean) {
        setCenter(isCenter, TOP)
    }

    fun setRightCenter(isCenter: Boolean) {
        setCenter(isCenter, RIGHT)
    }

    fun setBottomCenter(isCenter: Boolean) {
        setCenter(isCenter, BOTTOM)
    }

    private fun setCenter(isCenter: Boolean, direction: Int) {
        mIsCenter[direction] = isCenter
    }

    fun setLeftEnabled(enabled: Boolean) {
        setEnabled(enabled, LEFT)
    }

    fun setTopEnabled(enabled: Boolean) {
        setEnabled(enabled, TOP)
    }

    fun setRightEnabled(enabled: Boolean) {
        setEnabled(enabled, RIGHT)
    }

    fun setBottomEnabled(enabled: Boolean) {
        setEnabled(enabled, BOTTOM)
    }

    private fun setEnabled(enabled: Boolean, direction: Int) {
        mEnabled[direction] = enabled
    }

    fun wrapView(view: View) {
        val parent = view.parent
        if (parent is ViewGroup) {
            val group = parent
            val i = group.indexOfChild(view)
            val layoutParams = view.layoutParams
            group.removeViewAt(i)
            group.addView(this, i, layoutParams)
            addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        } else {
            addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    fun setOnFullSwipeListener(listener: OnFullSwipeListener?) {
        mListener = listener
    }

    fun setOnProgressChangedListener(listener: OnProgressChangedListener?) {
        mProgressListener = listener
    }

    fun isOpen(direction: Int): Boolean {
        return progresses[direction] >= TRIGGER_PROGRESS
    }

    @JvmOverloads
    fun close(isAnim: Boolean = true) {
        if (isAnim) {
            animClose()
        } else {
            progresses[LEFT] = 0f
            progresses[TOP] = 0f
            progresses[RIGHT] = 0f
            progresses[BOTTOM] = 0f
            postInvalidate()
        }
    }

    @JvmOverloads
    fun close(
        @Direction direction: Int,
        isAnim: Boolean = true
    ) {
        if (isAnim) {
            animClose(direction)
        } else {
            progresses[direction] = 0f
            postInvalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth
        mHeight = measuredHeight
        mLimit = Math.min(mWidth, mHeight) / 3
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawPath(canvas)
    }

    private fun drawPath(canvas: Canvas) {
        drawPath(canvas, LEFT)
        drawPath(canvas, TOP)
        drawPath(canvas, RIGHT)
        drawPath(canvas, BOTTOM)
    }

    private fun drawPath(canvas: Canvas, direction: Int) {
        if (mPath[direction] == null || progresses[direction] <= 0) return
        updatePaint(direction)
        canvas.drawPath(getPath(direction)!!, mPaint)
        drawIcon(canvas, direction)
    }

    private fun getPath(direction: Int): Path? {
        if (preProgresses[direction] != progresses[direction]) {
            mPath[direction]!!.reset()
            val edge: Float
            val pivot = mDown[direction]
            val mark: Int
            when (direction) {
                LEFT -> {
                    edge = 0f
                    mark = 1
                }
                TOP -> {
                    edge = 0f
                    mark = 1
                }
                RIGHT -> {
                    edge = mWidth.toFloat()
                    mark = -1
                }
                else -> {
                    edge = mHeight.toFloat()
                    mark = -1
                }
            }
            if (direction == LEFT || direction == RIGHT) {
                curPathX = edge
                curPathY = pivot - halfSize
            } else {
                curPathX = pivot - halfSize
                curPathY = edge
            }
            mPath[direction]!!.moveTo(curPathX, curPathY)
            quad(edge, pivot - halfSize, direction)
            quad(
                edge + progresses[direction] * unit * mark,
                pivot - halfSize + 5 * unit,
                direction
            ) // 1, 5
            quad(edge + progresses[direction] * 10 * unit * mark, pivot, direction) // 10, 16
            quad(
                edge + progresses[direction] * unit * mark,
                pivot + halfSize - 5 * unit,
                direction
            )
            quad(edge, pivot + halfSize, direction)
            quad(edge, pivot + halfSize, direction)
        }
        return mPath[direction]
    }

    private fun drawIcon(canvas: Canvas, direction: Int) {
        if (mDrawables[direction] == null) return
        val dWidth = mDrawables[direction]!!.intrinsicWidth
        val dHeight = mDrawables[direction]!!.intrinsicHeight
        val fitSize = (progresses[direction] * 5 * unit).toInt()
        val width: Int
        val height: Int
        var deltaWidth = 0
        var deltaHeight = 0
        if (dWidth >= dHeight) {
            width = fitSize
            height = width * dHeight / dWidth
            deltaHeight = fitSize - height
        } else {
            height = fitSize
            width = height * dWidth / dHeight
            deltaWidth = fitSize - width
        }
        when (direction) {
            LEFT -> {
                mRect.left = (0 + progresses[direction] * unit * 1 + deltaWidth / 2 * 1).toInt()
                mRect.top =
                    (mDown[LEFT] - height / 2).toInt()
                mRect.right = mRect.left + width
                mRect.bottom = mRect.top + height
            }
            RIGHT -> {
                mRect.right =
                    (mWidth + progresses[direction] * unit * -1 + deltaWidth / 2 * -1).toInt()
                mRect.top =
                    (mDown[RIGHT] - height / 2f).toInt()
                mRect.left = mRect.right - width
                mRect.bottom = mRect.top + height
            }
            TOP -> {
                mRect.left =
                    (mDown[TOP] - width / 2).toInt()
                mRect.top = (0 + progresses[direction] * unit * 1 + deltaHeight / 2 * 1).toInt()
                mRect.right = mRect.left + width
                mRect.bottom = mRect.top + height
            }
            else -> {
                mRect.left =
                    (mDown[BOTTOM] - width / 2).toInt()
                mRect.bottom =
                    (mHeight + progresses[direction] * unit * -1 + deltaHeight / 2 * -1).toInt()
                mRect.top = mRect.bottom - height
                mRect.right = mRect.left + width
            }
        }
        mDrawables[direction]!!.bounds = mRect
        mDrawables[direction]!!.draw(canvas)
    }

    private fun quad(pathX: Float, pathY: Float, direction: Int) {
        val preX = curPathX
        val preY = curPathY
        if (direction == LEFT || direction == RIGHT) {
            curPathX = pathX
            curPathY = pathY
        } else {
            curPathX = pathY
            curPathY = pathX
        }
        mPath[direction]!!.quadTo(preX, preY, (preX + curPathX) / 2, (preY + curPathY) / 2)
    }

    private var curPathX = 0f
    private var curPathY = 0f
    private fun updatePaint(direction: Int) {
        mPaint.color = mPaintColor[direction]
        var alphaProgress = progresses[direction]
        if (alphaProgress < 0.25f) {
            alphaProgress = 0.25f
        } else if (alphaProgress > 0.75f) {
            alphaProgress = 0.75f
        }
        mPaint.alpha = (alphaProgress * 255).toInt()
    }

    private fun animClose() {
        animClose(LEFT)
        animClose(TOP)
        animClose(RIGHT)
        animClose(BOTTOM)
    }

    private fun animClose(direction: Int) {
        if (progresses[direction] > 0) {
            val anim = ValueAnimator.ofFloat(progresses[direction], 0f)
            anim.addUpdateListener {
                progresses[direction] = anim.animatedValue as Float
                if (mProgressListener != null) {
                    mProgressListener!!.onProgressChanged(
                        direction,
                        progresses[direction],
                        false
                    )
                }
                postInvalidate()
            }
            anim.setDuration(100).start()
        }
    }

    @SuppressLint("WrongConstant")
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        super.dispatchTouchEvent(ev)
        try {
            val action = ev.action
            if (action == MotionEvent.ACTION_DOWN) {
                mDownX = ev.x
                mDownY = ev.y
                mIsStart[LEFT] =
                    mEnabled[LEFT] && mDrawables[LEFT] != null && !isOpen(LEFT) && mDownX <= mEdgeSizes[LEFT]
                mIsStart[TOP] =
                    mEnabled[TOP] && mDrawables[TOP] != null && !isOpen(TOP) && mDownY <= mEdgeSizes[TOP]
                mIsStart[RIGHT] =
                    mEnabled[RIGHT] && mDrawables[RIGHT] != null && !isOpen(RIGHT) && mDownX >= width - mEdgeSizes[RIGHT]
                mIsStart[BOTTOM] =
                    mEnabled[BOTTOM] && mDrawables[BOTTOM] != null && !isOpen(BOTTOM) && mDownY >= height - mEdgeSizes[BOTTOM]
                mIsEdgeStart =
                    mIsStart[LEFT] || mIsStart[TOP] || mIsStart[RIGHT] || mIsStart[BOTTOM]
                if (mIsEdgeStart) {
                    mStartDirection = -1
                }
                return true
            }
            if (mIsEdgeStart) {
                if (action == MotionEvent.ACTION_MOVE) {
                    mCurrentX = ev.x
                    mCurrentY = ev.y
                    if (mStartDirection == -1) {
                        val deltaX = mCurrentX - mDownX
                        val deltaY = mCurrentY - mDownY
                        val disX = Math.abs(deltaX)
                        val disY = Math.abs(deltaY)
                        if (disX > mTouchSlop || disY > mTouchSlop) {
                            if (disX >= disY) {
                                if (mIsStart[LEFT] && deltaX > 0) {
                                    decideDirection(LEFT)
                                } else if (mIsStart[RIGHT] && deltaX < 0) {
                                    decideDirection(RIGHT)
                                }
                            } else {
                                if (mIsStart[TOP] && deltaY > 0) {
                                    decideDirection(TOP)
                                } else if (mIsStart[BOTTOM] && deltaY < 0) {
                                    decideDirection(BOTTOM)
                                }
                            }
                        }
                    }
                    if (mStartDirection != -1) {
                        val preProgress = preProgresses[mStartDirection]
                        preProgresses[mStartDirection] = progresses[mStartDirection]
                        progresses[mStartDirection] = calculateProgress()
                        if (abs(preProgress - progresses[mStartDirection]) > 0.01) {
                            postInvalidate()
                            if (mProgressListener != null) {
                                mProgressListener!!.onProgressChanged(
                                    mStartDirection,
                                    progresses[mStartDirection],
                                    true
                                )
                            }
                        } else {
                            preProgresses[mStartDirection] = preProgress
                        }
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    if (mStartDirection != -1) {
                        mCurrentX = ev.x
                        mCurrentY = ev.y
                        progresses[mStartDirection] = calculateProgress()
                        if (isOpen(mStartDirection)) {
                            if (mListener != null) {
                                mListener!!.onFullSwipe(mStartDirection)
                            }
                        } else {
                            close(mStartDirection, true)
                        }
                    }
                }
            }
        }catch (ex: Exception){
            error("swipe panel back swipe error", ex)
        }
        return true
    }

    private fun decideDirection(direction: Int) {
        if (direction == LEFT || direction == RIGHT) {
            if (mIsCenter[direction]) {
                mDown[direction] = mHeight / 2f
            } else {
                when {
                    mDownY < halfSize -> {
                        mDown[direction] = halfSize
                    }
                    mDownY >= mHeight - halfSize -> {
                        mDown[direction] = mHeight - halfSize
                    }
                    else -> {
                        mDown[direction] = mDownY
                    }
                }
            }
        } else {
            if (mIsCenter[direction]) {
                mDown[direction] = mWidth / 2f
            } else {
                when {
                    mDownX < halfSize -> {
                        mDown[direction] = halfSize
                    }
                    mDownX >= mWidth - halfSize -> {
                        mDown[direction] = mWidth - halfSize
                    }
                    else -> {
                        mDown[direction] = mDownX
                    }
                }
            }
        }
        mStartDirection = direction
        if (mPath[direction] == null) {
            mPath[direction] = Path()
        }
        preProgresses[direction] = 0f
        cancelChildViewTouch()
        requestDisallowInterceptTouchEvent(true)
    }

    private fun calculateProgress(): Float {
        return if (mStartDirection == LEFT) {
            val deltaX = mCurrentX - mDownX
            if (deltaX <= 0) 0f else min(deltaX / mLimit, 1f)
        } else if (mStartDirection == TOP) {
            val deltaY = mCurrentY - mDownY
            if (deltaY <= 0) 0f else min(deltaY / mLimit, 1f)
        } else if (mStartDirection == RIGHT) {
            val deltaX = mCurrentX - mDownX
            if (deltaX >= 0) 0f else min(-deltaX / mLimit, 1f)
        } else {
            val deltaY = mCurrentY - mDownY
            if (deltaY >= 0) 0f else min(-deltaY / mLimit, 1f)
        }
    }

    private fun cancelChildViewTouch() {
        val now = SystemClock.uptimeMillis()
        val cancelEvent = MotionEvent.obtain(
            now, now,
            MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0
        )
        val childCount = childCount
        for (i in 0 until childCount) {
            getChildAt(i).dispatchTouchEvent(cancelEvent)
        }
        cancelEvent.recycle()
    }

    interface OnFullSwipeListener {
        fun onFullSwipe(@Direction direction: Int)
    }

    interface OnProgressChangedListener {
        fun onProgressChanged(
            @Direction direction: Int,
            progress: Float,
            isTouch: Boolean
        )
    }

    companion object {
        private const val TAG = "SwipePanel"
        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3
        private const val TRIGGER_PROGRESS = 0.95f
        private fun dp2px(dpValue: Float): Int {
            val scale =
                Resources.getSystem().displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        private val LOCK = Any()
        private var sTempValue: TypedValue? = null
        private fun getDrawable(
            context: Context,
            @DrawableRes id: Int
        ): Drawable? {
            return when {
                Build.VERSION.SDK_INT >= 21 -> {
                    context.getDrawable(id)
                }
                else -> {
                    var resolvedId: Int
                    synchronized(LOCK) {
                        if (sTempValue == null) {
                            sTempValue = TypedValue()
                        }
                        context.resources.getValue(
                            id,
                            sTempValue,
                            true
                        )
                        resolvedId =
                            sTempValue!!.resourceId
                    }
                    context.resources.getDrawable(resolvedId)
                }
            }
        }
    }

    init {
        val edgeSlop = ViewConfiguration.get(context).scaledEdgeSlop
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        mPaint =
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mPaint.style = Paint.Style.FILL
        halfSize = dp2px(72f).toFloat()
        unit = halfSize / 16
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.SwipePanel)
            setLeftSwipeColor(
                ta.getColor(
                    R.styleable.SwipePanel_leftSwipeColor,
                    Color.BLACK
                )
            )
            setTopSwipeColor(
                ta.getColor(
                    R.styleable.SwipePanel_topSwipeColor,
                    Color.BLACK
                )
            )
            setRightSwipeColor(
                ta.getColor(
                    R.styleable.SwipePanel_rightSwipeColor,
                    Color.BLACK
                )
            )
            setBottomSwipeColor(
                ta.getColor(
                    R.styleable.SwipePanel_bottomSwipeColor,
                    Color.BLACK
                )
            )
            setLeftEdgeSize(ta.getDimensionPixelSize(R.styleable.SwipePanel_leftEdgeSize, edgeSlop))
            setTopEdgeSize(ta.getDimensionPixelSize(R.styleable.SwipePanel_topEdgeSize, edgeSlop))
            setRightEdgeSize(
                ta.getDimensionPixelSize(
                    R.styleable.SwipePanel_rightEdgeSize,
                    edgeSlop
                )
            )
            setBottomEdgeSize(
                ta.getDimensionPixelSize(
                    R.styleable.SwipePanel_bottomEdgeSize,
                    edgeSlop
                )
            )
            setLeftDrawable(ta.getDrawable(R.styleable.SwipePanel_leftDrawable))
            setTopDrawable(ta.getDrawable(R.styleable.SwipePanel_topDrawable))
            setRightDrawable(ta.getDrawable(R.styleable.SwipePanel_rightDrawable))
            setBottomDrawable(ta.getDrawable(R.styleable.SwipePanel_bottomDrawable))
            setLeftCenter(ta.getBoolean(R.styleable.SwipePanel_isLeftCenter, false))
            setTopCenter(ta.getBoolean(R.styleable.SwipePanel_isTopCenter, false))
            setRightCenter(ta.getBoolean(R.styleable.SwipePanel_isRightCenter, false))
            setBottomCenter(ta.getBoolean(R.styleable.SwipePanel_isBottomCenter, false))
            setLeftEnabled(ta.getBoolean(R.styleable.SwipePanel_isLeftEnabled, true))
            setTopEnabled(ta.getBoolean(R.styleable.SwipePanel_isTopEnabled, true))
            setRightEnabled(ta.getBoolean(R.styleable.SwipePanel_isRightEnabled, true))
            setBottomEnabled(ta.getBoolean(R.styleable.SwipePanel_isBottomEnabled, true))
            ta.recycle()
        }
    }
}