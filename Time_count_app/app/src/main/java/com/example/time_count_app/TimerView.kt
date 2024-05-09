package com.example.time_count_app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class TimerView(context: Context, attrs: AttributeSet): View(context,attrs) {
    private val max = 3600f
    var now = 0f

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth =30f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context,R.color.main)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val padding = 20f
        var value = 360f / max * now
        val rectF = RectF(padding,padding,canvas!!.width - padding,canvas.height - padding)
        canvas.drawArc(rectF,-90f,value,false,paint)
    }
}