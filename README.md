# SwipeRcView
kotlin版本的上拉刷新下拉加载，有点类似于ios的下拉，可以无限下拉到屏幕外边</br>
demo自定义了headView，使用默认的footView

# 使用说明
>  1.在xml中添加view
```Xml
   <com.kotle.SwipeRcView
        android:id="@+id/rcv"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </com.kotle.SwipeRcView>
```
> 2.设置基本参数
```Kotlin
        rcv.layoutManager = YourManager(this)  //设置管理器
        rcv.adapter = yourAdapter  //设置适配器
        
        rcv.isCanLoadMore=true  //是否支持上拉加载 ，默认true
        rcv.isCanRefresh=true //是否支持下拉刷新，默认true
        rcv.recyclerView  //拿到真正的recyclerView对象，以便做其他有关操作
        rcv.headView = yourCustomView //设置自定义headView，不设置，则为默认
        rcv.footView= yourCustomView  //设置自定义footView，不设置，则为默认
        
        rcv.stopSwipe() //停止刷新或者加载状态
        
        //当处于刷新状态的时候被调用
        rcv.setRefreshListener {}
        //当处于加载状态的时候被调用
        rcv.setLoadMoreListener {}
        
        //自定义headView或者footView的时候可以根据一些状态改变ui
        //触发刷新的距离条件是headView的高度
        //触发加载的距离条件是footView的高度
        rcv.setPullSwipeListener {
            when (it) {
                SwipeRcView.SwipePullStatus.PULL_TOP -> {
                    //下拉的距离没有达到刷新条件
                }
                SwipeRcView.SwipePullStatus.PULL_TOP_TOUCH_OFF -> {
                    //下拉距离已经达到了刷新的条件，会触发下拉刷新
                }
                SwipeRcView.SwipePullStatus.PULL_REFRESH -> {
                    //处于下拉刷新状态，和 rcv.setRefreshListener{}效果一样
                }
                SwipeRcView.SwipePullStatus.PULL_NORMAL -> {
                    //处于正常状态，既不是下拉也不是加载。默认初始化界面的状态
                }
                SwipeRcView.SwipePullStatus.PULL_BOTTOM->{
                    //上拉的距离没有达到加载条件
                }
                SwipeRcView.SwipePullStatus.PULL_BOTTOM_TOU_OFF->{
                    //上拉距离达到加载条件，会触发上拉加载 
                }
                SwipeRcView.SwipePullStatus.PULL_LOAD_MORE->{
                    //处于上拉加载状态，和 rcv.setLoadMoreListener{}效果一样
                }
            }
        }
```
