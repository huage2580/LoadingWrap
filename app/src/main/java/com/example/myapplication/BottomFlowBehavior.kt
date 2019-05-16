package com.example.myapplication

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller

class BottomFlowBehavior :CoordinatorLayout.Behavior<View>{

    var initBottom = 0
    var offsetTotal = 0
    var dependencyInside:View? = null
    var child:View? = null
    var overScroller:OverScroller? = null
    var action:Action? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor() : super()

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency.id ==R.id.scroll
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        dependencyInside = dependency
        child.top = dependency.bottom
        child.bottom = child.top+ child.measuredHeight
        if (initBottom == 0){
            initBottom = dependency.bottom
        }
        if (this.child == null){
            this.child = child
        }
        if (overScroller == null) {
            overScroller = OverScroller(parent.context)
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        Log.i("fuck","onNestedPreScroll $dy $offsetTotal")
        if (target == child){//自己带来的滚动
            if (target.top > 0){
                consumed[1] = dy
                dependencyInside?.offsetTopAndBottom(-dy)
                offsetTotal -= dy
            }
            return
        }
        if (!target.canScrollVertically(1) && offsetTotal < 0){//不能往下滚了
            consumed[1] = dy
            dependencyInside?.offsetTopAndBottom(-dy)
            offsetTotal -= dy
        }
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: View, ev: MotionEvent): Boolean {
        if (action!=null){
            dependencyInside?.removeCallbacks(action)
            overScroller?.forceFinished(true)
        }
        return super.onTouchEvent(parent, child, ev)
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.i("fuck","onNestedPreFling $velocityY")
        if (target != dependencyInside){
            return false
        }
        val startY = dependencyInside?.let {
//            it.top + it.scrollY +it.height
            it.scrollY
        }?:0
        overScroller?.fling(0,startY,0,Math.round(velocityY),0,0,0,Int.MAX_VALUE)
        if (velocityY > 0 && overScroller!!.computeScrollOffset()){
            action = Action(coordinatorLayout,dependencyInside!!)
            ViewCompat.postOnAnimation(dependencyInside!!,action)
            return true
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    //TODO fling状态没处理

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL !== 0
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        Log.i("fuck","onNestedScroll = [${child.y}] $dyConsumed  $dyUnconsumed [$type]")
        if (target == child){//自己带来的滚动
            if (!target.canScrollVertically(-1)){//自己不能再往上滚
                dependencyInside?.offsetTopAndBottom(-dyUnconsumed)
                offsetTotal -=dyUnconsumed
            }
            return
        }
        if (!target.canScrollVertically(1)){//webview不能滚动，由bottom消费
            dependencyInside?.offsetTopAndBottom(-dyUnconsumed)
            offsetTotal -=dyUnconsumed
//            child.offsetTopAndBottom(-dyUnconsumed)
        }else if (offsetTotal != 0){
            dependencyInside?.offsetTopAndBottom(-offsetTotal)
            offsetTotal -=offsetTotal
        }

    }

    inner class Action(val parent: CoordinatorLayout,val layout:View):Runnable{
        override fun run() {
            if (overScroller!!.computeScrollOffset() && !needStop()){
                Log.i("fuck","fling = ${overScroller!!.currY}")
                dependencyInside!!.scrollBy(0,overScroller!!.currY)
                (dependencyInside as? NestedScrollingChild)?.dispatchNestedScroll(0,overScroller!!.currY,0,overScroller!!.currY,
                    intArrayOf(0,0))
//                dependencyInside?.offsetTopAndBottom(overScroller!!.currY)
//                offsetTotal += overScroller!!.currY
                ViewCompat.postOnAnimation(layout,this)
            }else{
                overScroller!!.forceFinished(true)
            }
        }

        private fun needStop():Boolean{
            return !dependencyInside!!.canScrollVertically(-1) && child!!.bottom <= initBottom
        }

    }
}