package com.gyz.androiddevelope.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gyz.androiddevelope.util.ScreenUtils;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;


public abstract class BaseActivity extends AppCompatActivity implements SwipeBackActivityBase {

    public static final String TAG = "BaseActivity";
    public ProgressDialog dlg;
    private SwipeBackActivityHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        initBaseView();
        initVariables();
        initViews(savedInstanceState);
        loadData();
    }

    private void initBaseView() {
        dlg = new ProgressDialog(this);
    }

    /*
        初始化传入参数
         */
    protected abstract void initVariables();

    /*
        初始化界面及view
    	 */
    protected abstract void initViews(Bundle savedInstanceState);

    /*
    网络请求
     */
    protected abstract void loadData();


    //==================================================

//======SwipeBack==begin===========================


    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
//======SwipeBack==end===========================


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dlg != null)
            dlg.dismiss();
    }
}