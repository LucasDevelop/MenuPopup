package com.mmy.kotlinsample.popup

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import com.mmy.menupopup.R
import com.mmy.menupopup.SpringScaleInterpolator


/**
 * @file       ControlPopup.kt
 * @brief      描述
 * @author     lucas
 * @date       2018/5/8 0008
 * @version    V1.0
 * @par        Copyright (c):
 * @par History:
 *             version: zsr, 2017-09-23
 */
class ControlPopup constructor(val context: Context, val targetView: View, val array: IntArray) : PopupWindow() {
    val r = 400.0//半径
    val pints = ArrayList<Point>()//用户存储每个点的坐标
    var degrees: Double = 0.0
    val location = kotlin.IntArray(2)
    var mRootView: RelativeLayout? = null
    val duration = 1200L
    val targetDuration = 800L
    val delay = 220L//按键之前的出现间隔

    val mHandler = Handler()
    var mContainer: RelativeLayout? = null

    init {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent)))
        isOutsideTouchable = true


        //第一个按键固定在正下方
        val element = Point(0, r.toInt())
        element.direction(true, false)
        pints.add(element)
        //算出每个角的度数
        degrees = 360 / array.size.toDouble()
        //算出剩下的图标的坐标
        for (i in 1 until array.size) {
            //夹角度数
            val d = degrees * i
            var point: Point? = null
            //判断是在那个象限,确定方向
            val radians = Math.toRadians(d % 90)
            when (d.toInt()) {
                in 0..90 -> {//第四象限 x,-y
//                    Log.d("popup","90:${d % 90},x:${Math.sin(d % 90) * r},y:${Math.cos(d % 90) * r}")
                    point = Point((Math.sin(radians) * r).toInt(), (Math.cos(radians) * r).toInt())
                    point.direction(true, false)
                }
                in 90..180 -> {//第一象限 x,y
                    point = Point((Math.cos(radians) * r).toInt(), (Math.sin(radians) * r).toInt())
                    point.direction(true, true)
                }
                in 180..270 -> {//第二象限-x,y
                    point = Point((Math.sin(radians) * r).toInt(), (Math.cos(radians) * r).toInt())
                    point.direction(false, true)
                }
                else -> {//第三象限-x,-y
                    point = Point((Math.cos(radians) * r).toInt(), (Math.sin(radians) * r).toInt())
                    point.direction(false, false)
                }
            }
            if (point != null)
                pints.add(point)
        }

        mRootView = LayoutInflater.from(context).inflate(R.layout.popup_control, null, false) as RelativeLayout
        contentView = mRootView
        mContainer = mRootView?.findViewById(R.id.v_container)
        mRootView?.setOnClickListener { dismiss() }
        //添加按键
        array.forEach {
            val imageView = ImageView(context)
            imageView.setImageResource(it)
            val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            imageView.layoutParams = params
            imageView.visibility =View.INVISIBLE
            mContainer?.addView(imageView)
        }
    }

    private fun closeAnim() {
        for (i in 0 until pints.size) {
            pints[i].tostring()
            val childAt = mContainer?.getChildAt(i)
            //x轴平移
            val animatorX = ObjectAnimator.ofFloat(childAt, "translationX", 0.0f, 0.0f)
            //y轴平移
            val animatorY = ObjectAnimator.ofFloat(childAt, "translationY", 0.0f, 0.0f)
            val set = AnimatorSet()
            set.play(animatorX).with(animatorY)
            set.duration = duration
            set.start()
        }
    }

    private fun openAnim() {
        //将目标控件自动到原点
        val midView = mRootView?.findViewById<View>(R.id.v_mid_icon)
        val targetLocation = kotlin.IntArray(2)
        targetView.getLocationOnScreen(targetLocation)
        midView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    midView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                }
                val midLocation = kotlin.IntArray(2)
                midView.getLocationOnScreen(midLocation)
                //x轴平移
                val animatorX = ObjectAnimator.ofFloat(midView, "translationX", (targetLocation[0].toFloat()-midLocation[0].toFloat()), .0f)
                //y轴平移
                val animatorY = ObjectAnimator.ofFloat(midView, "translationY", (targetLocation[1].toFloat()-midLocation[1].toFloat()), .0f)
                //缩放动画
                val scaleAnimatorX = ObjectAnimator.ofFloat(midView, "scaleX", 1.0f, 1.5f)
                val scaleAnimatorY = ObjectAnimator.ofFloat(midView, "scaleY", 1.0f, 1.5f)
                val set = AnimatorSet()
                set.play(animatorX).with(animatorY).with(scaleAnimatorX).with(scaleAnimatorY)
                set.duration = targetDuration
                set.addListener(object :Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        //当目标图片移动结束后开始释放其他按键
                        for (i in 0 until pints.size) {
                            pints[i].tostring()
                            mHandler.postDelayed({
                                val childAt = mContainer?.getChildAt(i)
                                childAt?.visibility=View.VISIBLE
                                //x轴平移
                                val animatorX = ObjectAnimator.ofFloat(childAt, "translationX", 0.0f, pints[i].x.toFloat())
                                //y轴平移
                                val animatorY = ObjectAnimator.ofFloat(childAt, "translationY", 0.0f, pints[i].y.toFloat())
                                val set = AnimatorSet()
                                set.play(animatorX).with(animatorY)
                                set.duration = duration
                                set.interpolator = SpringScaleInterpolator(0.4f)
                                set.start()
                            }, i * delay)
                        }
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }

                })
                set.start()
                Log.d("lucas", "xxx:${midLocation[0].toFloat()},yyy:${midLocation[1].toFloat()}")
            }
        })

    }

    //显示弹窗
    fun show(activity: Activity) {
        showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0)
        openAnim()
    }

    //关闭弹窗
    fun close() {
        closeAnim()
        dismiss()
    }


    /**
     * 给pint扩展个方向的方法
     * Tip:当y的值为正数就是在上方，反之在下方
     *      x值为正数就在右侧，反之左侧
     */
    fun Point.direction(xDirection: Boolean, yDirection: Boolean) {
        if (!xDirection)
            this.x = 0 - this.x
        if (yDirection)
            this.y = 0 - this.y
    }

    fun Point.tostring() {
        Log.d("popup", "x:${this.x},y:${this.y}")
    }
}