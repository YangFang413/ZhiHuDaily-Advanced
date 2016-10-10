# ZhiHuDaily-Advanced
The new ZhihuDaily app added more features to the original version.

### Features
- 开屏页面
- 当日新闻展示
- 首页推荐内容滚动
- 网络连接的检查及处理及数据未获取的检查及处理

### Characteristics
使用ViewPager实现推荐内容的轮播（但未实现点击事件）</br>
使用RecycleView实现当日新闻展示</br>
修正第一版的点击返回键返回多个activity的问题</br>

### Problems
ViewPager轮播时下方的小点没有滚动</br>
开屏页面的text呈现动画的形式</br>
夜间功能尚未实现</br>
webView最上层图片不显示</br>
点击recycleView的item将字体变成灰色，以提示已点击。</br>
收藏功能。</br>

### 实现细节  
1. 开屏页面自动跳转使用的是handle让线程休眠。开屏页面的图片使用的是AsyncTask异步加载的。 
2. 首页新闻的简单展示使用的viewpager+fragment+recyclerview，此处的viewpager是无限轮播形式的，每隔5s中切换下一张。（注意，viewpager的pageradapter的getItem()方法应该返回的是Integer.MAX_VALUE，这样做当滑动到最后一页的时候，继续滑动可以回到第一页。向容器中添加图片，由于要实现无限轮播，所以要对position进行取模。这个时候在instantiateItem()方法中得到了新的view要添加给父控件之前，如果这个View已经在之前添加给一个父控件了，父控件必须先调用removeView()方法将View移除，否则将抛出IllegalStateException。 
3. 此处从服务器端获取到的css保存在本地，在加载的时候调用webview的loadDataWithBaseURL()进行加载。


