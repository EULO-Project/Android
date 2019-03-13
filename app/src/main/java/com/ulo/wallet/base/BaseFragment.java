package com.ulo.wallet.base;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.ulo.wallet.UloApplication;

import global.UloModule;

/**
 * Created by furszy on 6/29/17.
 */

public class BaseFragment extends Fragment {

    protected UloApplication pivxApplication;
    protected UloModule pivxModule;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pivxApplication = UloApplication.getInstance();
        pivxModule = pivxApplication.getModule();
    }

    protected boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getActivity(),permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
