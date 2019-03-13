package com.ulo.wallet.base;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ulo.wallet.R;
import com.ulo.wallet.base.dialog.BitalongProgressDialog;
import com.ulo.wallet.base.dialog.SimpleTextDialog;
import com.ulo.wallet.base.dialog.SimpleTwoButtonsDialog;
import com.ulo.wallet.utils.DialogsUtil;
import com.ulo.wallet.utils.StatusBarUtil;

/**
 * Created by mati on 18/04/17.
 */

public abstract class BaseActivity extends UloActivity {

    protected Toolbar toolbar;
    protected FrameLayout childContainer;
    private TextView tvTitle;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (isFullScreen()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_base_without_toolbar);
        } else {
            setContentView(R.layout.activity_base);
            setStatusBar();
        }
        init();
        // onCreateChildMethod
        onCreateView(savedInstanceState, childContainer);

    }

    private final void init() {
        childContainer = (FrameLayout) findViewById(R.id.content);
        if (hasToolbar() && !isFullScreen()) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            tvTitle = (TextView) findViewById(R.id.tv_title);
            toolbar.setTitle("");
            toolbar.setNavigationIcon(R.mipmap.ic_back);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigationBackPressed();
                    onBackPressed();
                }
            });
        }
    }

    public void setTitle(int titleId) {
        if (tvTitle != null)
            tvTitle.setText(titleId);
    }

    public void setTitle(String title) {
        if (tvTitle != null)
            tvTitle.setText(title);
    }

    public boolean hasToolbar() {
        return true;
    }

    public boolean isFullScreen() {
        return false;
    }

    /**
     * Empty method to override.
     * <p>
     * Launched when the user clicks on the toolbar navigation icon
     */
    protected void onNavigationBackPressed() {

    }

    /**
     * Empty method to override.
     *
     * @param savedInstanceState
     */
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

    }

    protected boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary), 1);
    }


}
