package com.gyz.androiddevelope.view;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.gyz.androiddevelope.activity.custom.AlbumListActivity;
import com.gyz.androiddevelope.listener.ItemRemovedListener;
import com.gyz.androiddevelope.util.DensityUtils;
import com.gyz.androiddevelope.util.LogUtils;
import com.gyz.androiddevelope.util.ScreenUtils;
import com.gyz.androiddevelope.view.percent.PercentFrameLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @version V1.0
 * @FileName: com.gyz.androiddevelope.view.SwipeCardRecyclerView.java
 * @author: ZhaoHao
 * @date: 2016-12-15 15:33
 */
public class SwipeCardRecyclerView extends RecyclerView {
    private static final String TAG = "SwipeCardRecyclerView";
    private float mTopViewX;
    private float mTopViewY;

    private float mTopViewOffsetX = 0;
    private float mTopViewOffsetY = 0;

    private float mTouchDownX;
    private float mTouchDownY;
    private ItemRemovedListener mRemovedListener;

    private float mBorder = dip2px(120);


    private int[] mDecorViewLocation = new int[2];
    private FrameLayout mDecorView;
    private Map<View, Animator> mAnimatorMap;

    public SwipeCardRecyclerView(Context context) {
        this(context,null);
    }

    public SwipeCardRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SwipeCardRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        this.mDecorView = (FrameLayout) ((AppCompatActivity)getContext()).getWindow().getDecorView();
        this.mDecorView.getLocationOnScreen(mDecorViewLocation);
        mAnimatorMap = new HashMap<>();
    }


    public void setRemovedListener(ItemRemovedListener listener) {
        this.mRemovedListener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (getChildCount() == 0) {
            return super.onTouchEvent(e);
        }

        View topView = getChildAt(getChildCount()-1);
        float touchX = e.getX();
        float touchY = e.getY();
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (mAnimatorMap.containsKey(topView)){
                    mAnimatorMap.get(topView).cancel();
                    mAnimatorMap.remove(topView);
                    mTopViewOffsetX = topView.getX();
                    mTopViewOffsetY = topView.getY();
                }else {
                    mTopViewX = topView.getX();
                    mTopViewY = topView.getY();
                    mTopViewOffsetX  = 0;
                    mTopViewOffsetY = 0;
                }
                mTouchDownX = touchX;
                mTouchDownY = touchY;
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = touchX - mTouchDownX;
                float dy = touchY - mTouchDownY;
                topView.setX(mTopViewX+dx+mTopViewOffsetX);
                topView.setY(mTopViewY+dy+mTopViewOffsetY);
                updateNextItem(Math.abs(topView.getX() - mTopViewX) * 0.2 / mBorder + 0.8);
                break;

            case MotionEvent.ACTION_UP:
                mTouchDownX = 0;
                mTouchDownY = 0;
                touchUp(topView);
                break;
        }

        return super.onTouchEvent(e);
    }

    /**
     * 更新下一个View的宽高
     * @param factor
     */
    private void updateNextItem(double factor) {

        if (getChildCount() <2){
            return;
        }
        if (factor >1){
            factor = 1;
        }
        View nextView = getChildAt(getChildCount()-2);
        nextView.setScaleX((float) factor);
        nextView.setScaleY((float) factor);
    }

    /**
     * 手指抬起时 触发动画
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void touchUp(final View view) {
        float targetX = 0;
        float targetY = 0;
        boolean del = false;
        float getx = view.getX();
        LogUtils.e(TAG,"getx=============="+getx);
        if (Math.abs(view.getX() -mTopViewX)<mBorder){
            targetX = mTopViewX;
            targetY = mTopViewY;
        }else if (view.getX()-mTopViewX >mBorder) {
            del = true;
            targetX = ScreenUtils.getScreenWidth(getContext())*2;
            mRemovedListener.onRightRemoved();
        }else {
            del = true;
            targetX = -view.getWidth()-ScreenUtils.getScreenWidth(getContext());
            mRemovedListener.onLeftRemoved();
        }
        View animView = view;
        TimeInterpolator interpolator;
        if (del){
            animView = getMirrorView(view);
            float offsetX = getX() - mDecorView.getX();
            float offsetY = getY() - mDecorView.getY();
            targetY = caculateExitY(mTopViewX + offsetX, mTopViewY + offsetY, animView.getX(), animView.getY(), targetX);
            interpolator = new LinearInterpolator();
        }else {
            interpolator = new OvershootInterpolator();
        }
        final boolean finalDel = del;
        final View finalAnimView = animView;
        animView.animate()
                .setDuration(500)
                .x(targetX)
                .y(targetY)
                .setInterpolator(interpolator)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (!finalDel){
                            mAnimatorMap.put(finalAnimView,animation);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (finalDel){
                            mDecorView.removeView(finalAnimView);
                        }else {
                            mAnimatorMap.remove(finalAnimView);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (!finalDel){
                            updateNextItem(Math.abs(view.getX() - mTopViewX) * 0.2 / mBorder + 0.8);
                        }
                    }
                });

    }

    private float caculateExitY(float x1, float y1, float x2, float y2, float x3) {
        return (y2 - y1) * (x3 - x1) / (x2 - x1) + y1;
    }

    /**
     * 创建镜像view 替代原有顶部View展示删除动画
     * @param view
     * @return
     */
    private ImageView getMirrorView(View view) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        final ImageView mirrorView = new ImageView(getContext());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(),bitmap.getHeight());
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        mirrorView.setAlpha(view.getAlpha());
        view.setVisibility(GONE);
        ((AlbumListActivity.ListAdapter)getAdapter()).delTopItem();
        mirrorView.setX(location[0]-mDecorViewLocation[0]);
        mirrorView.setY(location[1]-mDecorViewLocation[1]);
        mDecorView.addView(mirrorView,params);

        return mirrorView;
    }

    private float dip2px(int i) {
       return DensityUtils.dp2px(getContext(),i);
    }
}
