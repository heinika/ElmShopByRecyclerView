
先看一下效果图：

![效果图](http://upload-images.jianshu.io/upload_images/759172-70151f483c6445e1.gif?imageMogr2/auto-orient/strip)

左边就是一个普通的recyclerView，右边则需要一个有标题的recyclerView。

### sticky-headers-recyclerview

![](https://camo.githubusercontent.com/2712b977a781964db02085035e43281773ab4ffa/687474703a2f2f692e696d6775722e636f6d2f49307a746f50772e676966)

项目地址：https://github.com/timehop/sticky-headers-recyclerview

用它在写adapter的时候，记得写两个viewholder，一个content，一个header的。

其中根据getHeaderId(int position)来确定标题，返回值相同的属于同一个标题。

#### 添加标题

```java
// Add the sticky headers decoration,给球队添加标题
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(teamsAndHeaderAdapter);
        recyclerviewTeams.addItemDecoration(headersDecor);
```

只多了这一步操作，就给recyclerView相同分类的item加上标题了。

####  adapter

```java
/**
 * Created by chenlijin on 2016/3/18.
 */
public class TeamsAndHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private List<Category> categoryList;
    private List<Team> teamList = new ArrayList<>();
    private Context mContext;

    public TeamsAndHeaderAdapter(Context context,List<Category> categoryList) {
        mContext = context;
        setCategoryList(categoryList);
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
        for(int i = 0;i<categoryList.size();i++){
            if(teamList!=null){
                teamList.addAll(categoryList.get(i).getTeamList());
            }
        }
        notifyDataSetChanged();
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    /**
     * 返回值相同会被默认为同一项
     * @param position
     * @return
     */
    @Override
    public long getHeaderId(int position) {
       return getSortType(position);
    }

    //获取当前球队的类型
    public int getSortType(int position) {
        int sort = -1;
        int sum = 0;
        for (int i=0;i<categoryList.size();i++){
            if(position>=sum){
                sort++;
            }else {
                return sort;
            }
            sum += categoryList.get(i).getTeamList().size();
        }
        return sort;
    }

    /**
     * =========================================================================
     * header的ViewHolder
     * ========================================================================
     */
    @Override
    public ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        TextView textView = (TextView) viewHolder.itemView;
        textView.setText(categoryList.get(getSortType(position)).getSortName());
        textView.setBackgroundColor(getRandomColor());
    }


    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private int getRandomColor() {
        SecureRandom rgen = new SecureRandom();
        return Color.HSVToColor(150, new float[]{
                rgen.nextInt(359), 1, 1
        });
    }

    /**
     * =========================================================================
     * 以下为contentViewHolder
     * =========================================================================
     */
    @Override
    public ContentViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.header_team_list, viewGroup, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentViewHolder viewHolder = (ContentViewHolder) holder;
        viewHolder.textView.setText(teamList.get(position).getName());
      	//glide谷歌推荐的图片加载库
        Glide.with(mContext)
                .load(teamList.get(position).getImagePath())
                .placeholder(R.drawable.icon_logo_image_default)
                .centerCrop()
                .into(viewHolder.imageViewTeam);
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageViewTeam;
        public ContentViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textview_teamname);
            imageViewTeam = (ImageView) itemView.findViewById(R.id.imageview_team);
        }
    }
}
```

###  两个recyclerView的相互监听

####  右边滚动，左边切换

监听滚动OnScrollChangeListener，由layoutmanager获取到第一个完全显示的item的position。再进行判断：

```java
recyclerviewTeams.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //第一个完全显示的item和最后一个item。
                int firstVisibleItem = mTeamsLayoutManager.findFirstCompletelyVisibleItemPosition();
                int lastVisibleItem = mTeamsLayoutManager.findLastVisibleItemPosition();
                //此判断，避免左侧点击最后一个item无响应
                if(lastVisibleItem != mTeamsLayoutManager.getItemCount()-1){
                    int sort = teamsAndHeaderAdapter.getSortType(firstVisibleItem);
                    changeSelected(sort);
                }else {
                    changeSelected(categoryAdapter.getItemCount()-1);
                }
                if(needMove){
                    needMove = false;
                    //获取要置顶的项在当前屏幕的位置，mIndex是记录的要置顶项在RecyclerView中的位置
                    int n = movePosition - mTeamsLayoutManager.findFirstVisibleItemPosition();
                    if ( 0 <= n && n < recyclerviewTeams.getChildCount()){
                        //获取要置顶的项顶部离RecyclerView顶部的距离
                        int top = recyclerviewTeams.getChildAt(n).getTop()-dip2px(MainActivity.this,28);
                        //最后的移动
                        recyclerviewTeams.scrollBy(0, top);
                    }
                }
            }
        });
```

#### 左边点击，右边滚动到相应位置

recyclerView有三种滚动的方法：scrollTo，scrollBy，scrollToPosition。

scrollTo这个方法无响应，至于这个方法是做什么的？还请赐教。。。

scrollToPosition这个方法比较神奇。它分三种情况：

1，position在界面内，无反应

2，position在界面下方，则滚动此position到底部。

3，position在界面上方，则滚动此position到顶部。

scrollBy这个是最靠谱的一个方法了，从当前位置相对进行位移，可正可负。

按理说scrollBy是最简单的，**但是recyclerviewTeams.getChildAt(n - firstItem)**，item如果不在屏幕内就会**为空**，所以就需要先把item移到屏幕内。。。这样如果item在屏幕下方，就需要先scrollToPosition，再scrollBy。所以要监听滚动，item在屏幕下方时，第一次滚动完成后，进行第二次滚动。
```java
private void moveToPosition(int n) {
    //先从RecyclerView的LayoutManager中获取第一项和最后一项的Position
    int firstItem = mTeamsLayoutManager.findFirstVisibleItemPosition();
    int lastItem = mTeamsLayoutManager.findLastVisibleItemPosition();
    //然后区分情况
    if (n <= firstItem ){
        //当要置顶的项在当前显示的第一个项的前面时
        recyclerviewTeams.scrollToPosition(n);
    }else if ( n <= lastItem ){
        //当要置顶的项已经在屏幕上显示时
        int top = recyclerviewTeams.getChildAt(n - firstItem).getTop();
        recyclerviewTeams.scrollBy(0, top-dip2px(this,28));
    }else{
        //当要置顶的项在当前显示的最后一项的后面时
        recyclerviewTeams.scrollToPosition(n);
        movePosition = n;
        needMove = true;
    }
}
```

item在屏幕下方时，在监听里添加

```java
if(needMove){
    needMove = false;
    //获取要置顶的项在当前屏幕的位置，mIndex是记录的要置顶项在RecyclerView中的位置
    int n = movePosition - mTeamsLayoutManager.findFirstVisibleItemPosition();
    if ( 0 <= n && n < recyclerviewTeams.getChildCount()){
        //获取要置顶的项顶部离RecyclerView顶部的距离
        int top = recyclerviewTeams.getChildAt(n).getTop()-dip2px(MainActivity.this,28);
        //最后的移动
        recyclerviewTeams.scrollBy(0, top);
    }
}
```

到此，左右两端的相互关联就完成了。