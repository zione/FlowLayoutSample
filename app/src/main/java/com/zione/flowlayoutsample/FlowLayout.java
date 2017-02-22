package com.zione.flowlayoutsample;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zione on 2017/2/22.
 * 1.标签布局，自动根据剩余空间换行排列。
 * 2.支持高度不一的子控件，每行高度为这行最高子控件高度。
 * 3.在高度不一样时，可以通过在布局中设置 app:fl_line_align="bottom"、“center”、“top”
 * 来指定行内元素的对齐方式。
 * 4.可以通过布局文件中设置app:fl_content_align=“left”、“center”、"right"来指定
 * 水平方向上的对齐方式。
 */

public class FlowLayout extends ViewGroup {
    private static final int LINE_ALIGN_TOP = 0;
    private static final int LINE_ALIGN_CENTER = 1;
    private static final int LINE_ALIGN_BOTTOM = 2;

    private static final int CONTENT_ALIGN_LEFT = 0;
    private static final int CONTENT_ALIGN_CENTER = 1;
    private static final int CONTENT_ALIGN_RIGHT = 2;

    private int lineAlignMode;          //行的上下对齐方式 0-top/1-center/2-bottom
    private int contentAlignMode;       //左右的对齐方式 0-左/1-center/2-右
    private List<List<View>> views;     //全部行的子控件
    private List<View> lineViews;       //每行的子控件
    private List<Integer> lineHeighs;   //每行的高
    private List<Integer> lineWidths;   //每行的宽

    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.FlowLayout);
        lineAlignMode = ta.getInt(R.styleable.FlowLayout_fl_line_align,0);
        contentAlignMode = ta.getInt(R.styleable.FlowLayout_fl_content_align,0);
        ta.recycle();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heigtMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;  //最宽那行的值
        int height = 0; //每行累加值
        int widthTag = 0;  //每行个子控件累加宽度
        int heightTag = 0; //每行的最高控件的高度作为本行行高

        views = new ArrayList<>();
        lineViews = new ArrayList<View>();
        lineHeighs = new ArrayList<Integer>();
        lineWidths = new ArrayList<Integer>();
        views.add(lineViews);


        for(int i=0;i< getChildCount();i++){
            View chile = getChildAt(i);
            measureChild(chile,getChildWidthMeasureSpec(chile,widthMeasureSpec)
                    ,getChildHeightMeasureSpec(chile,heightMeasureSpec));
            MarginLayoutParams p = (MarginLayoutParams)chile.getLayoutParams();

            int chileWidth = chile.getMeasuredWidth();
            int chileHeight = chile.getMeasuredHeight();

            if(chileWidth + p.leftMargin + p.rightMargin + widthTag > widthSize){  //大于就得换行
                lineViews = new ArrayList<View>();
                views.add(lineViews);


                height += heightTag;
                lineHeighs.add(heightTag);
                lineWidths.add(widthTag);
                widthTag = chileWidth + p.leftMargin + p.rightMargin;
                heightTag = 0;
            }else{
                widthTag += chileWidth + p.leftMargin + p.rightMargin;
            }

            width = Math.max(width,widthTag);
            heightTag = Math.max(heightTag,chileHeight + p.topMargin + p.bottomMargin);
            lineViews.add(chile);
        }
        height += heightTag;
        lineHeighs.add(heightTag);
        lineWidths.add(widthTag);
        width = width > widthSize ? widthSize:width;

        setMeasuredDimension(widthMode== MeasureSpec.AT_MOST?width:widthSize
                ,heigtMode== MeasureSpec.AT_MOST?height:heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int heightTag = 0;  //标记布局到哪行的高度

        for(int i=0;i< views.size();i++){
            List<View> lViews = views.get(i);
            int widthTag = 0;   //标记横向布局的位置
            for(int j=0;j<lViews.size();j++){
                View child = lViews.get(j);
                MarginLayoutParams p = (MarginLayoutParams)child.getLayoutParams();

                //默认左上角对齐
                int cl = 0;
                int ct = 0;
                int cr = 0;
                int cb = 0;


                switch (lineAlignMode){
                    case LINE_ALIGN_BOTTOM:
                        ct = heightTag + lineHeighs.get(i) - p.bottomMargin - child.getMeasuredHeight();
                        cb = heightTag + lineHeighs.get(i) - p.bottomMargin;
                        break;
                    case LINE_ALIGN_CENTER:
                        ct = heightTag + lineHeighs.get(i)/2 - child.getMeasuredHeight()/2;
                        cb = heightTag + lineHeighs.get(i)/2 + child.getMeasuredHeight()/2;
                        break;
                    case LINE_ALIGN_TOP:
                        ct = heightTag + p.topMargin;
                        cb = ct + child.getMeasuredHeight();
                        break;
                }

                int delta = 0;
                switch (contentAlignMode){
                    case CONTENT_ALIGN_LEFT:
                        cl = widthTag + p.leftMargin;
                        cr = cl + child.getMeasuredWidth();
                        break;
                    case CONTENT_ALIGN_CENTER:
                        delta = (getMeasuredWidth() - lineWidths.get(i))/2;
                        cl = widthTag + p.leftMargin + delta;
                        cr = cl + child.getMeasuredWidth();
                        break;
                    case CONTENT_ALIGN_RIGHT:
                        delta = (getMeasuredWidth() - lineWidths.get(i));
                        cl = widthTag + p.leftMargin + delta;
                        cr = cl + child.getMeasuredWidth();
                        break;
                }

                child.layout(cl,ct,cr,cb);

                widthTag += child.getMeasuredWidth() + p.leftMargin + p.rightMargin;
            }
            heightTag += lineHeighs.get(i);
        }
    }

    //考虑了Flowlayout的padding，和child自身的margin
    private int getChildWidthMeasureSpec(View child, int widthMeasureSpec){
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        MarginLayoutParams p = (MarginLayoutParams)child.getLayoutParams();

        int width = widthSize - p.leftMargin -p.rightMargin - getPaddingLeft() - getPaddingRight();
        return MeasureSpec.makeMeasureSpec(width,widthMode);
    }

    private int getChildHeightMeasureSpec(View child, int heightMeasureSpec){
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        MarginLayoutParams p = (MarginLayoutParams)child.getLayoutParams();

        int height = heightSize - p.topMargin -p.bottomMargin - getPaddingTop() - getPaddingBottom();
        return MeasureSpec.makeMeasureSpec(height,heightMode);
    }
}
