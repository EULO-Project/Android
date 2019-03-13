package com.ulo.wallet.ui.main;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.google.zxing.common.StringUtils;
import com.ulo.wallet.R;
import com.ulo.wallet.UloApplication;
import com.ulo.wallet.utils.AndroidUtils;
import com.ulo.wallet.utils.QrUtils;
import com.ulo.wallet.utils.TxUtils;

import org.pivxj.core.Address;
import org.pivxj.uri.PivxURI;

import global.UloModule;

import static android.graphics.Color.WHITE;

/**
 * Created by furszy on 6/8/17.
 */

public class MyAddressFragment extends Fragment implements View.OnClickListener {
    private UloModule module;
    private View root;
    private TextView txt_address;
    private Button btn_share;
    private Button btn_copy;
    private ImageView img_qr;
    private String address;

    public static MyAddressFragment newInstance(UloModule pivxModule) {
        MyAddressFragment f = new MyAddressFragment();
        f.setModule(pivxModule);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        module = UloApplication.getInstance().getModule();
        root = inflater.inflate(R.layout.my_address, null);
        txt_address = (TextView) root.findViewById(R.id.txt_address);
        btn_share = (Button) root.findViewById(R.id.btn_share);
        btn_copy = (Button) root.findViewById(R.id.btn_copy);
        btn_copy.setOnClickListener(this);
        img_qr = (ImageView) root.findViewById(R.id.img_qr);
        btn_share.setOnClickListener(this);
        img_qr.setOnClickListener(this);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            // check if the address is already used
            boolean flag = false;
            if (address == null || TxUtils.isEmpty(address)) {
                address = module.getReceiveAddress();
                // todo: cleanup this
                //module.getKeyPairForAddress(address);
                flag = true;
            }
            if (flag) {
                loadAddress(address);
            }
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Problem loading qr", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setModule(UloModule module) {
        this.module = module;
    }

    private void loadAddress( String addressStr) throws WriterException {
        Bitmap qrBitmap = null;//Cache.getQrBigBitmapCache();
        if (qrBitmap == null) {
            Resources r = getResources();
            int px = convertDpToPx(r, 225);
            Log.i("Util", addressStr);
            qrBitmap = QrUtils.encodeAsBitmap(addressStr, px, px, Color.parseColor("#1A1A1A"), WHITE);
        }
        img_qr.setImageBitmap(qrBitmap);
        txt_address.setText(addressStr);
    }


    public static int convertDpToPx(Resources resources, int dp) {
        return Math.round(dp * (resources.getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void share(String address) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, address);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_address_text)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_share) {
            share(address);
        } else if (id == R.id.img_qr) {
            AndroidUtils.copyToClipboard(getActivity(), address);
            Toast.makeText(v.getContext(), R.string.copy_message, Toast.LENGTH_LONG).show();
        } else if (id == R.id.btn_copy) {
            AndroidUtils.copyToClipboard(getActivity(), address);
            Toast.makeText(v.getContext(), R.string.copy_message, Toast.LENGTH_LONG).show();
        }
    }
}
