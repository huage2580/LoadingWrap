package com.example.myapplication

import android.widget.TextView
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.widget.CircularProgressDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
//import androidx.core.graphics.drawable.DrawableCompat
//import androidx.swiperefreshlayout.widget.CircularProgressDrawable

/**
 * 为textView等添加loading效果
 * @description: add loading status for object that extends TextView
 * @author: asus-hua [https://github.com/huage2580]
 * @date: 2019/5/16 11:58
 */
object LoadingWrap{

    /**
     * change status to loading ;
     * for [size] ,recommend use [LoadingWrap.SIZE.LARGE]
     * [showText] ,True -》show Like [ text ○ ]，false -》 show like [ ○ ]
     */
    fun toLoadingStatus(target:TextView,size:SIZE = LoadingWrap.SIZE.LARGE,showText:Boolean = false){
        //create loading drawable
        val progressDrawable = CircularProgressDrawable(target.context).apply {
            // let's use large style just to better see one issue
            setStyle(size.value)
            //bounds definition is required to show drawable correctly
            val sizeInt = (centerRadius + strokeWidth).toInt() * 2
            setBounds(0, 0, sizeInt, sizeInt)
        }
        val drawableSpan = DrawableSpan(progressDrawable,10,0,true)
        // create a SpannableString
        val spannableString =
            if (showText)
                SpannableString(target.text.toString()+" ").apply {
                    setSpan(drawableSpan, length -1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            else
                SpannableString(target.text.toString()).apply {
                    setSpan(drawableSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

        //start progress drawable animation
        progressDrawable.start()
        val callback = object : Drawable.Callback {
            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            }

            override fun invalidateDrawable(who: Drawable) {
                target.invalidate()
            }

            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            }
        }
        progressDrawable.callback = callback
        target.text = spannableString
    }

    /**
     * change status to String style
     */
    fun toStringStatus(target: TextView){
        //释放资源，避免内存泄漏
        releaseDrawable(target)
        target.text = target.text.toString().trimEnd()
    }

    private fun releaseDrawable(target: TextView) {
        val span = target.text
        if (span is SpannableString){
            val drawableArray = span.getSpans(0,span.length,DrawableSpan::class.java)
            drawableArray.forEach {
                it.drawable.apply {
                    if(this is Animatable){
                        stop()
                    }
                    callback = null
                }
            }
        }
    }

    //loading drawable size
    enum class SIZE(val value:Int){
        LARGE(0),
        SMALL(1)
    }

}

//---extensions func--------

fun TextView.toLoadingStatus(){
    LoadingWrap.toLoadingStatus(this)
}
fun TextView.toStringStatus(){
    LoadingWrap.toStringStatus(this)
}
//--------------------------

// only resize ,add padding , tint ,for drawable
class DrawableSpan(drawable: Drawable, var paddingStart: Int = 0, var paddingEnd: Int = 0, private val useTextAlpha: Boolean = true) :
    ImageSpan(drawable) {

    override fun getSize(
        paint: Paint, text: CharSequence, start: Int, end: Int,
        fontMetricsInt: Paint.FontMetricsInt?
    ): Int {
        val drawable = drawable
        val rect = drawable.bounds
        fontMetricsInt?.let {
            val fontMetrics = paint.fontMetricsInt
            val lineHeight = fontMetrics.bottom - fontMetrics.top
            val drHeight = Math.max(lineHeight, rect.bottom - rect.top)
            val centerY = fontMetrics.top + lineHeight / 2
            fontMetricsInt.apply {
                ascent = centerY - drHeight / 2
                descent = centerY + drHeight / 2
                top = ascent
                bottom = descent
            }
        }
        return rect.width() + paddingStart + paddingEnd
    }


    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        //tint for CircularProgressDrawable
        (drawable as? CircularProgressDrawable)?.apply {
            setColorSchemeColors(paint.color)
        }
        val drawable = DrawableCompat.wrap(drawable)
        canvas.save()
        val fontMetrics = paint.fontMetricsInt
        val lineHeight = fontMetrics.descent - fontMetrics.ascent
        val centerY = y + fontMetrics.descent - lineHeight / 2
        val transY = centerY - drawable.bounds.height() / 2
        if (paddingStart != 0) {
            canvas.translate(x + paddingStart, transY.toFloat())
        } else {
            canvas.translate(x, transY.toFloat())
        }
        //tint for default drawable
        if (useTextAlpha) {
            val colorAlpha = Color.alpha(paint.color)
            drawable.alpha = colorAlpha
            DrawableCompat.setTint(drawable, paint.color)
        }
        drawable.draw(canvas)
        canvas.restore()
    }

}