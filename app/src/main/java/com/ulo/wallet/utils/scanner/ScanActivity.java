package com.ulo.wallet.utils.scanner;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.mylhyl.zxing.scanner.OnScannerCompletionListener;
import com.mylhyl.zxing.scanner.ScannerOptions;
import com.mylhyl.zxing.scanner.ScannerView;
import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseActivity;
import com.ulo.wallet.utils.DialogBuilder;

/**
 * Created by DoubleDa on 2018/1/24.
 */

public class ScanActivity extends BaseActivity implements OnScannerCompletionListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String INTENT_EXTRA_RESULT = "result";
    ScannerView scannerView;


    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        super.onCreateView(savedInstanceState, container);
        setContentView(R.layout.act_options_scanner_layout);
        initView();
        setResult(RESULT_CANCELED);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);

    }


    private void initView() {
        scannerView=findViewById(R.id.scanner_view);
        scannerView.setOnScannerCompletionListener(this);
        ScannerOptions.Builder builder = new ScannerOptions.Builder();
        builder.setFrameSize(256, 256)
                .setFrameCornerLength(22)
                .setFrameCornerWidth(2)
                .setFrameCornerColor(0xff06c1ae)
                .setFrameCornerInside(true)
                .setLaserStyle(ScannerOptions.LaserStyle.RES_GRID, R.mipmap.zfb_grid_scan_line)
                .setFrameCornerColor(0xFF26CEFF)
                .setScanFullScreen(true)
                .setFrameCornerHide(false)
//                .setViewfinderCallback(new ScannerOptions.ViewfinderCallback() {
//                    @Override
//                    public void onDraw(View view, Canvas canvas, Rect frame) {
//                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//                        canvas.drawBitmap(bmp, frame.right / 2, frame.top - bmp.getHeight(), null);
//                    }
//                })
                .setScanMode(BarcodeFormat.QR_CODE)
                .setTipText(getString(R.string.str_scan_qr_tip))
                .setTipTextSize(19)
                .setTipTextColor(getResources().getColor(R.color.white));
        scannerView.setScannerOptions(builder.build());
    }
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            scannerView.onResume();
        else
            ScanActivity.WarnDialogFragment.newInstance(R.string.scan_camera_permission_dialog_title, getString(R.string.scan_camera_permission_dialog_message))
                    .show(getFragmentManager(), "dialog");



    }
    @Override
    public void onScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {
//        EventBus.getDefault().post(new MotionEvent(rawResult.getText()));

        vibrate();
        scannerView.restartPreviewAfterDelay(0);
         Intent result = new Intent();
        result.putExtra(INTENT_EXTRA_RESULT, rawResult.getText());
        setResult(RESULT_OK, result);
        finish();
    }
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    protected void onResume() {
        scannerView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        scannerView.onPause();
        super.onPause();
    }

    public static class WarnDialogFragment extends DialogFragment {
        public static WarnDialogFragment newInstance(final int titleResId, final String message) {
            final WarnDialogFragment fragment = new WarnDialogFragment();
            final Bundle args = new Bundle();
            args.putInt("title", titleResId);
            args.putString("message", message);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Bundle args = getArguments();
            final DialogBuilder dialog = DialogBuilder.warn(getActivity(), args.getInt("title"));
            dialog.setMessage(args.getString("message"));
            dialog.singleDismissButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    getActivity().finish();
                }
            });
            return dialog.create();
        }

        @Override
        public void onCancel(final DialogInterface dialog) {
            getActivity().finish();
        }
    }
}
