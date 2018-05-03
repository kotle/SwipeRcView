package com.kotle

import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.TextView

/**
 * 若红楼梦空，亦初心不变
 * 作者：Kotle
 * 包名：com.kotle.swipercview
 * 时间：2018/5/2 13:42
 * 描述：
 */
class SwipeRcView : ViewGroup, GestureDetector.OnGestureListener {
    //0正常 1下拉 2上拉 3正在刷新 4正在加载
    private var pullStatus = 0
    private var currentPullStatus = SwipePullStatus.PULL_NORMAL
    private var swipePullListener: ((SwipePullStatus) -> Unit)? = null
    private var refreshListener: ((SwipeRcView) -> Unit)? = null
    private var loadMoreListener: ((SwipeRcView) -> Unit)? = null
    private val gestureDetector by lazy { GestureDetector(context, this) }
    val recyclerView by lazy {
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val lp1 = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val lp2 = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val rcv = RecyclerView(context)
        addView(headView, lp1)
        addView(rcv, lp)
        addView(footView, lp2)
        rcv
    }
    var headView: View = initHeadView()
    var footView: View = initFootView()
    private val mixThreshold by lazy {
        dipToPx(3f)
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {

    }

    private fun initHeadView(): View {
        val view = TextView(context)
        view.setPadding(0, dipToPx(16f).toInt(), 0, dipToPx(16f).toInt())
        view.gravity = Gravity.CENTER
        view.text = "下拉刷新"
        return view
    }

    private fun initFootView(): View {
        val view = TextView(context)
        view.setPadding(0, dipToPx(16f).toInt(), 0, dipToPx(16f).toInt())
        view.gravity = Gravity.CENTER
        view.text = "上拉加载"
        return view
    }

    private var oldY = 0f
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (pullStatus == 0) {
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldY = ev.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val offY = ev.y - oldY
                    oldY = ev.y
                    if (isCanRefresh && offY > 0 && !recyclerView.canScrollVertically(-1)) {
                        pullStatus = 1
                        return true
                    } else if (isCanLoadMore && offY < 0 && !recyclerView.canScrollVertically(1)) {
                        pullStatus = 2
                        return true
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (pullStatus == 0) {
            return super.onTouchEvent(event)
        }
        if (event?.action == MotionEvent.ACTION_UP) {
            stop()
        }
        return gestureDetector.onTouchEvent(event)
    }

    //当用户按下，并且没有移动
    override fun onShowPress(e: MotionEvent?) {
    }

    //用户单机的时候调用
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return performClick()
    }

    //用户按下的时候触发,返回false不检测滚动事件，true检测滚动事件
    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    //用户手指松开后，滑动的时候触发
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    //用户滚动的时候 distanceY<0 下拉  -1 是否可下拉
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (Math.abs(distanceY) < 100) {
            when (pullStatus) {
                1, 3 -> {
                    if (distanceY < 0) {
                        offsetTopOrBottom(-(distanceY / 2).toInt())
                    } else {
                        val t = recyclerView.top
                        if (t < mixThreshold) {
                            offsetTopOrBottom(-t)
                            pullStatus = 0
                            currentPullStatus = SwipePullStatus.PULL_NORMAL
                            swipePullListener?.invoke(SwipePullStatus.PULL_NORMAL)
                        } else {
                            offsetTopOrBottom(-(distanceY / 2).toInt())
                        }
                    }
                }
                2, 4 -> {
                    if (distanceY > 0) {
                        offsetTopOrBottom(-(distanceY / 2).toInt())
                    } else {
                        val t = recyclerView.bottom
                        if (height - t < mixThreshold) {
                            offsetTopOrBottom(height - t)
                            pullStatus = 0
                            currentPullStatus = SwipePullStatus.PULL_NORMAL
                            swipePullListener?.invoke(SwipePullStatus.PULL_NORMAL)
                        } else {
                            offsetTopOrBottom(-(distanceY / 2).toInt())
                        }
                    }
                }
            }
        }
        return false
    }

    //长按
    override fun onLongPress(e: MotionEvent?) {
    }

    //开始重新布局
    private fun offsetTopOrBottom(value: Int) {
        if (value == 0) {
            return
        }
        when (pullStatus) {
            1, 3 -> {
                val oldTop = recyclerView.top
                val newTop = oldTop + value
                recyclerView.layout(recyclerView.left, newTop, recyclerView.right, recyclerView.bottom)
                headView.layout(headView.left, newTop - headView.height, headView.right, newTop)
                if (newTop > headView.height) {
                    if (currentPullStatus == SwipePullStatus.PULL_TOP_TOUCH_OFF) {
                        return
                    }
                    (headView as? TextView)?.text = "松手刷新"
                    swipePullListener?.invoke(SwipePullStatus.PULL_TOP_TOUCH_OFF)
                    currentPullStatus = SwipePullStatus.PULL_TOP_TOUCH_OFF
                } else {
                    if (currentPullStatus == SwipePullStatus.PULL_TOP) {
                        return
                    }
                    (headView as? TextView)?.text = "下拉刷新"
                    swipePullListener?.invoke(SwipePullStatus.PULL_TOP)
                    currentPullStatus = SwipePullStatus.PULL_TOP
                }
            }
            2, 4 -> {
                val oldBottopm = recyclerView.bottom
                val newBottom = oldBottopm + value
                recyclerView.layout(recyclerView.left, recyclerView.top, recyclerView.right, newBottom)
                recyclerView.scrollBy(0, -value)
                footView.layout(headView.left, newBottom, headView.right, newBottom + footView.height)
                if (height - newBottom > footView.height) {
                    if (currentPullStatus == SwipePullStatus.PULL_BOTTOM_TOU_OFF) {
                        return
                    }
                    (footView as? TextView)?.text = "松手加载"
                    swipePullListener?.invoke(SwipePullStatus.PULL_BOTTOM_TOU_OFF)
                    currentPullStatus = SwipePullStatus.PULL_BOTTOM_TOU_OFF
                } else {
                    if (currentPullStatus == SwipePullStatus.PULL_BOTTOM) {
                        return
                    }
                    (footView as? TextView)?.text = "下拉加载"
                    swipePullListener?.invoke(SwipePullStatus.PULL_BOTTOM)
                    currentPullStatus = SwipePullStatus.PULL_BOTTOM
                }
            }
        }
    }

    //复位
    private fun offsetTopOrBottomBy(value: Int) {
        when (pullStatus) {
            1, 3 -> {
                recyclerView.layout(recyclerView.left, value, recyclerView.right, recyclerView.bottom)
                headView.layout(headView.left, value - headView.height, headView.right, value)
            }
            2, 4 -> {
                recyclerView.layout(recyclerView.left, recyclerView.top, recyclerView.right, value)
                footView.layout(footView.left, value, footView.right, value + footView.height)
            }
        }
    }

    //复位
    private fun stop() {
        if (pullStatus == 0) {
            return
        }
        if (recyclerView.top == 0 && recyclerView.bottom == height) {
            pullStatus = 0
            return
        }
        var start: Int
        var end: Int
        if (recyclerView.top == 0) {//上拉
            if (height - recyclerView.bottom > footView.height) {
                pullStatus = 4
            }
            start = recyclerView.bottom
            end = if (pullStatus == 4) {
                (footView as? TextView)?.text = "正在加载..."
                loadMoreListener?.invoke(this)
                currentPullStatus = SwipePullStatus.PULL_LOAD_MORE
                swipePullListener?.invoke(SwipePullStatus.PULL_LOAD_MORE)
                height - footView.height
            } else {
                height
            }
        } else {//下拉
            if (recyclerView.top > headView.height) {
                pullStatus = 3
            }
            start = recyclerView.top
            end = if (pullStatus == 3) {
                (headView as? TextView)?.text = "正在刷新..."
                refreshListener?.invoke(this)
                currentPullStatus = SwipePullStatus.PULL_REFRESH
                swipePullListener?.invoke(SwipePullStatus.PULL_REFRESH)
                headView.height
            } else {
                0
            }
        }
        val anim = ValueAnimator.ofInt(start, end)
        anim.duration = 200
        anim.interpolator = LinearInterpolator()
        anim.addUpdateListener {
            val vaule = it.animatedValue as Int
            offsetTopOrBottomBy(vaule)
            if (vaule == end) {
                if (pullStatus == 1 || pullStatus == 2) {
                    pullStatus = 0
                    currentPullStatus = SwipePullStatus.PULL_NORMAL
                    swipePullListener?.invoke(SwipePullStatus.PULL_NORMAL)
                }
            }
        }
        anim.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount
        for (i in 0 until count) {
            val childView = getChildAt(i)
            measureChild(childView, widthMeasureSpec, heightMeasureSpec)
//            childView.measure(widthMeasureSpec, heightMeasureSpec)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) {
            for (i in 0 until childCount) {
                val childView = getChildAt(i)
                when {
                    childView === headView -> childView.layout(paddingLeft, -headView.measuredHeight + paddingTop, r - l - paddingRight, paddingTop)
                    childView === footView -> childView.layout(paddingLeft, b - t - paddingBottom, r - l - paddingRight, b - t + footView.measuredHeight - paddingBottom)
                    childView === recyclerView -> childView.layout(paddingLeft, paddingTop, r - l - paddingRight, b - t - paddingBottom)
                    else -> childView.layout(0, 0, 0, 0)
                }
            }
        } else {
            for (i in 0 until childCount) {
                val childView = getChildAt(i)
                childView.layout(childView.left, childView.top, childView.right, childView.bottom)
            }
        }
    }

    /**********************************************************************/
    var isCanRefresh = true
    var isCanLoadMore = true
    //设置adapter
    var adapter: RecyclerView.Adapter<*>
        set(value) {
            recyclerView.adapter = value
        }
        get() {
            return recyclerView.adapter
        }

    //设置layoutManager
    var layoutManager: RecyclerView.LayoutManager
        set(value) {
            recyclerView.layoutManager = value
        }
        get() {
            return recyclerView.layoutManager
        }

    fun dipToPx(value: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics)
    }

    fun stopSwipe() {
        pullStatus = when (pullStatus) {
            3 -> 1
            4 -> 2
            else -> 0
        }
        stop()
    }

    //当自定义view的时候，设置这个可以监听什么时候触发刷新或者加载事件
    fun setPullSwipeListener(swipePullListener: ((SwipePullStatus) -> Unit)) {
        this.swipePullListener = swipePullListener
    }

    //当正在刷新或者正在加载的时候调用
    fun setRefreshListener(refresh: ((SwipeRcView) -> Unit)) {
        this.refreshListener = refresh
    }

    //当正在刷新或者正在加载的时候调用
    fun setLoadMoreListener(loadMore: ((SwipeRcView) -> Unit)) {
        this.loadMoreListener = loadMore
    }

    enum class SwipePullStatus {
        PULL_TOP,//开始下拉
        PULL_BOTTOM,//开始加载
        PULL_TOP_TOUCH_OFF,//触发了下拉
        PULL_BOTTOM_TOU_OFF,//触发了加载
        PULL_REFRESH,//正在刷新
        PULL_LOAD_MORE,//正在加载
        PULL_NORMAL//正在加载
    }
}