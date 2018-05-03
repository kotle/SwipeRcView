package com.kotle.kotlinswiperrcview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.kotle.SwipeRcView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var tv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = View.inflate(this, R.layout.head, null)
        tv = view.findViewById(R.id.tv_head)
        rcv.headView = view
        rcv.layoutManager = LinearLayoutManager(this)
        rcv.adapter = A()
        rcv.setRefreshListener {

        }
        rcv.setLoadMoreListener {

        }
        rcv.setPullSwipeListener {
            when (it) {
                SwipeRcView.SwipePullStatus.PULL_TOP -> {
                    tv.text = "加油，再拉一点点啦"
                }
                SwipeRcView.SwipePullStatus.PULL_TOP_TOUCH_OFF -> {
                    tv.text = "你很棒，松开给你奖励"
                }
                SwipeRcView.SwipePullStatus.PULL_REFRESH -> {
                    tv.text = "正在发送奖励中。。。"
                }
                SwipeRcView.SwipePullStatus.PULL_NORMAL -> {
                    tv.text = "加油，再拉一点点啦"
                }
            }
        }
    }

    fun stop(view: View) {
        rcv.stopSwipe()
    }

    class A : RecyclerView.Adapter<H>() {
        private val list: ArrayList<String> = ArrayList()

        init {
            for (index in 0..20) {
                list.add(index.toString())
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
            val tv = TextView(parent.context)
            tv.setPadding(100, 100, 100, 100)
            return H(tv)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: H, position: Int) {
            val tv = holder.itemView as TextView
            tv.text = list[position]
            tv.setOnClickListener {
                Toast.makeText(it.context, tv.text, Toast.LENGTH_SHORT).show()
            }
        }

    }

    class H(view: View) : RecyclerView.ViewHolder(view)
}
