package fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yf.myzhihu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import activity.DetailNewsActivity;
import model.News;
import utility.HttpUtil;
import utility.ParseUtility;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomepageFragment extends Fragment {

    private ViewPager imageViewPager;
    private RecyclerView contentRecyclerView;
    private TextView titleText; // 存放ViewPager的title的控件
    private LinearLayout pointLayout; // 存放圆点的layout

    private ImageView imageView;
    private List<ImageView> imageViewList = new ArrayList<>(); // 存放所有需要加载的image视图
    private List<News> recommendNewsList; // 存放网络上获取到的News实例
    private ArrayList<String> titleList = new ArrayList<>(); // 存放需要加载在viewPager上的标题

    public static final int VIEWPAGER_MAX = 5;

    private int downloadImageNumber = 0; // 统计下载图片的数量
    private int displayPage = 0; // 当前ViewPager轮播到第几页
    private boolean isStart = false; // 控制图片是否开始轮播，默认为关闭

    private final int IMAGE_DOWNLOAD = 0;
    private final int CURRENT_PAGE = 1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IMAGE_DOWNLOAD:
                    downloadImageNumber++;
                    Bitmap bitmap = (Bitmap) msg.obj;
                    imageView = new ImageView(getContext());
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setImageBitmap(bitmap);
                    imageViewList.add(imageView);
                    if (downloadImageNumber == VIEWPAGER_MAX){
                        imageViewPager.setAdapter(new HotNewsViewPager());
                        createPoint();
                        isStart = true;
                        new ImagePlayThread().start();
                    }
                    break;
                case CURRENT_PAGE:
                    int page = (Integer) msg.obj;
                    imageViewPager.setCurrentItem(page);
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);
        imageViewPager = (ViewPager) view.findViewById(R.id.content_viewpager);
        contentRecyclerView = (RecyclerView) view.findViewById(R.id.content_recyclerview);
        titleText = (TextView) view.findViewById(R.id.viewpager_image_description);
        pointLayout = (LinearLayout) view.findViewById(R.id.show_pointer);
        new DownloadThread().start();
        imageViewPager.addOnPageChangeListener(new ImagePageChangeListener());
        contentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bindRecycleViewAdapter();
        return view;
    }

    @Override
    public void onDestroy() {
        isStart = false; // Fragment销毁时，ViewPager无限循环的线程也应该结束
        super.onDestroy();
    }

    // 图片下载
    class DownloadThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                String response = HttpUtil.getUrlString("http://news-at.zhihu.com/api/3/news/hot");
                recommendNewsList = ParseUtility.handleHotNews(response);
                for (int i = 0; i < HomepageFragment.VIEWPAGER_MAX; i++) {
                    byte[] bytes = HttpUtil.getUrlByte(recommendNewsList.get(i).getImageUrl());
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    titleList.add(recommendNewsList.get(i).getTitle());
                    Message message = new Message();
                    message.what = IMAGE_DOWNLOAD;
                    message.obj = bitmap;
                    mHandler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 控制图片轮播
    class ImagePlayThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(isStart){
                Message message = new Message();
                message.what = CURRENT_PAGE;
                message.obj = displayPage;
                mHandler.sendMessage(message);
                try {
                    //睡眠5秒,在isStart为真的情况下，一直每隔三秒循环
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                displayPage++;
            }
        }
    }

    // 设置ViewPager的小圆点视图
    protected void createPoint(){
        View v = new View(getContext());
        // 设置小点的宽和高
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(8, 8);
        // 设置小点的左边距
        params.leftMargin = 12;
        v.setLayoutParams(params);
        // 设置小点是否可用，默认都不可用，当不可用时，小点是透明的，否则是白色的
        v.setEnabled(false);
        // 设置小点的背景，这个背景是使用xml文件画的一个小圆点
        v.setBackgroundResource(R.drawable.pointer_selector);
        // 把小点添加到它的布局文件中
        pointLayout.addView(v);
    }

    // ViewPager的适配器PagerAdapter
    public class HotNewsViewPager extends PagerAdapter {

        /**
         * 返回图片总数，Integer.MAX_VALUE的值为2147483647，这个数有21亿，也就是说我们的viewpager
         * 理论上在每次使用应用的时候可以滑动21亿次,当然，实际上是没人要去这么做的，这样做是为了实现viewpager循环滑动的效果
         * 即当滑动到viewpager的最后一页时，继续滑动就可以回到第一页
         */

        // 该方法返回视图的个数
        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        // 用来判断instantiateItem(ViewGroup, int)方法返回的Key是否和界面的View相关联，如果关联则返回true，否则返回false。
        // 当返回为true时就将根据当前的position得到的view展示出来，否则就不展示。
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        // 当某一页滑出去的时候，将其销毁
        // 从容器中移除position所对应的视图，而且这个移除的动作是在finishUpdate之前完成的。
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViewList.get(position % imageViewList.size()));
        }

        // 向容器中添加图片，由于要实现循环滑动的效果，所以要对position取模
        // 根据当前的posistion来创建对应的视图，并且将这个创建好的视图添加到容器中，这个添加操作是在调用finishUpdate(ViewGroup)这个方法之前完成的。
        // instantiateItem 方法会返回一个对象，这个对象代表这一个新的视图。
        // 这个对象不一定是一个View，可以是这个视图的其他容器，也就是说只要可以唯一代表这个界面的东西都可以作为这个对象。
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView image = imageViewList.get(position % imageViewList.size());
            // 如果View已经在之前添加到了一个父控件，则必须先remove，否则会抛出IllegalStateException。
            ViewParent viewParent = image.getParent();
            if (viewParent != null){
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(image);
            }
            container.addView(imageViewList.get(position % imageViewList.size()));
            return imageViewList.get(position % imageViewList.size());
        }
    }

    // ViewPager的监听器
    private class ImagePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            // 当页面滑动结束时，先对页面位置取模
            position = position % imageViewList.size();
            // 设置textview的文本内容
            titleText.setText(titleList.get(position));
            // 将上一个点的可用性设置为false
            // pointLayout.getChildAt(previousPoint).setEnabled(false);
            // 把当前点的可用性设置为true
            pointLayout.setEnabled(true);
            // 把当前位置值赋值给previousPoint
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private class RecycleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView newsSmallImage;
        private TextView newsTitle;

        private News mNews;

        public RecycleViewHolder(View itemView) {
            super(itemView);
            newsSmallImage = (ImageView) itemView.findViewById(R.id.news_image);
            newsTitle = (TextView) itemView.findViewById(R.id.news_title);
            contentRecyclerView.setOnClickListener(this);
        }

        public void bindNews(final News news){
            mNews = news;
            newsTitle.setText(news.getTitle());
            new Thread(new Runnable() {
                Bitmap bitmap;
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil.getUrlByte(news.getImageUrl());
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsSmallImage.setImageBitmap(bitmap);
                        }
                    });
                }
            }).start();

        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), DetailNewsActivity.class);
            intent.putExtra("News", mNews.getId());
            startActivity(intent);
        }
    }

    private class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewHolder>{

        private List<News> newsList;

        public RecycleViewAdapter(List<News> newsList){
            this.newsList = newsList;
        }


        @Override
        public RecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.recycleview_item, parent, false);
            return new RecycleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecycleViewHolder holder, int position) {
            News news = newsList.get(position);
            holder.bindNews(news);
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }
    }

    private void bindRecycleViewAdapter(){
        final List<News> newsList = new ArrayList<News>(); // 存放当日网路上获取到的News实例
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = HttpUtil.getUrlString("http://news-at.zhihu.com/api/4/news/latest");
                    JSONObject jsonObject = ParseUtility.getJO(response);
                    String date = jsonObject.getString("date");
                    JSONArray array = ParseUtility.getJOArray(jsonObject, "stories");
                    for (int i = 0; i < array.length(); i++){
                        News news = new News();
                        news.setDate(date);
                        JSONObject object = array.getJSONObject(i);
                        String title = object.getString("title");
                        news.setTitle(title);
                        JSONArray jsonArray = object.getJSONArray("images");
                        String imageUrl = jsonArray.get(0).toString();
                        news.setImageUrl(imageUrl);
                        news.setId(object.getInt("id"));
                        news.setIsFavorite(0);
                        newsList.add(news);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RecycleViewAdapter adapter = new RecycleViewAdapter(newsList);
                            contentRecyclerView.setAdapter(adapter);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}


