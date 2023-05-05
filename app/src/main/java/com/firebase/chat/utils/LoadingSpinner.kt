package com.firebase.chat.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class LoadingSpinner(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var angle = 0f
    private val sweepAngle = 300f
    private val startAngle = 150f
    private var isAnimating = false
    private val animator: ValueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 1000
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            angle = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        paint.color = Color.WHITE
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        if (isAnimating) {
            canvas.drawArc(rectF, startAngle + angle, sweepAngle, false, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Math.min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
        rectF.set(
            0f + paddingLeft,
            0f + paddingTop,
            size.toFloat() - paddingRight,
            size.toFloat() - paddingBottom
        )
    }

    fun start() {
        if (!isAnimating) {
            isAnimating = true
            animator.start()
        }
    }

    fun stop() {
        if (isAnimating) {
            isAnimating = false
            animator.cancel()
            angle = 0f
            invalidate()
        }
    }
}
