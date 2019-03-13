package com.ulo.wallet.ui.pincode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseActivity;
import com.ulo.wallet.ui.initial.StartActivity;
import com.ulo.wallet.ui.main.WalletActivity;

import wallet.WalletManager;

import static com.ulo.wallet.ui.pincode.MnemonicActivity.INTENT_EXTRA_INIT_VIEW;

/**
 * Created by Neoperol on 4/20/17.
 */

public class PincodeActivity extends BaseActivity implements KeyboardFragment.onKeyListener {

    public static final String CHECK_PIN = "check_pin";
    public static final String RESTORE_SET_PIN = "restore_set_pin";

    private boolean checkPin = false;
    private boolean restore_set_pin = false;

    private ImageView i1, i2, i3, i4;
    private int[] pin = new int[4];
    private int lastPos = 0;

    private KeyboardFragment keyboardFragment;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

        if (getIntent() != null && getIntent().hasExtra(CHECK_PIN)) {
            checkPin = true;
        }

        if (getIntent() != null && getIntent().hasExtra(RESTORE_SET_PIN)) {
            restore_set_pin = true;
        }


        if (pivxApplication.getAppConf().getPincode() != null && !checkPin) {
            goNext();
            finish();
        }

        getLayoutInflater().inflate(R.layout.fragment_pincode, container);
        setTitle("Create Pin");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        i1 = (ImageView) findViewById(R.id.imageview_circle1);
        i2 = (ImageView) findViewById(R.id.imageview_circle2);
        i3 = (ImageView) findViewById(R.id.imageview_circle3);
        i4 = (ImageView) findViewById(R.id.imageview_circle4);
        keyboardFragment = (KeyboardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_keyboard);
        keyboardFragment.setOnKeyListener(this);
    }

    private void goNext() {
//        if (pivxApplication.getAppConf().getTrustedNode() == null) {
//            // select random trusted node
//            List<UloNodeData> nodes = PivtrumGlobalData.listTrustedHosts();
//            Random random = new Random();
//            pivxApplication.setTrustedServer(nodes.get(random.nextInt(nodes.size())));
//            pivxApplication.stopBlockchain();
//        }


        if(restore_set_pin){
            try {
                pivxModule.createWallet();
                pivxApplication.getAppConf().setAppInit(true);
                startActivity(new Intent(this, WalletActivity.class));

            } catch (WalletManager.WalletErrorException e) {
                e.printStackTrace();
                showErrorDialog(e.getMessage());
            }

        }else {
            Intent myIntent = new Intent(PincodeActivity.this, MnemonicActivity.class);
            myIntent.putExtra(INTENT_EXTRA_INIT_VIEW, true);
            startActivity(myIntent);
            finish();
        }
    }


    @Override
    public void onKeyClicked(KeyboardFragment.KEYS key) {
        if (lastPos < 4) {
            if (key.getValue() < 10) {
                pin[lastPos] = key.getValue();
                activeCheck(lastPos);
                lastPos++;
                if (lastPos == 4) {
                    String pincode = String.valueOf(pin[0]) + String.valueOf(pin[1]) + String.valueOf(pin[2]) + String.valueOf(pin[3]);

                    if (!checkPin) {
                        pivxApplication.getAppConf().savePincode(pincode);
                        pivxModule.savePinCode(pincode);
                        Toast.makeText(this, R.string.pincode_saved, Toast.LENGTH_SHORT).show();
                        goNext();
                    } else {
                        // check pin and return result
                        if (pivxApplication.getAppConf().getPincode().equals(pincode)) {
                            Intent intent = new Intent();
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(this, R.string.bad_pin_code, Toast.LENGTH_LONG).show();
                            clear();
                        }
                    }
                }
            } else if (key == KeyboardFragment.KEYS.DELETE) {
                if (lastPos != 0) {
                    lastPos--;
                    unactiveCheck(lastPos);
                }
            } else if (key == KeyboardFragment.KEYS.CLEAR) {
                clear();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // todo: controlar esto
        if (pivxApplication.getAppConf().getPincode() == null) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
    }

    private void clear() {
        unactiveCheck(0);
        unactiveCheck(1);
        unactiveCheck(2);
        unactiveCheck(3);
        lastPos = 0;
    }

    private void activeCheck(int pos) {
        switch (pos) {
            case 0:
                i1.setImageResource(R.drawable.pin_circle_active);
                break;
            case 1:
                i2.setImageResource(R.drawable.pin_circle_active);
                break;
            case 2:
                i3.setImageResource(R.drawable.pin_circle_active);
                break;
            case 3:
                i4.setImageResource(R.drawable.pin_circle_active);
                break;
        }
    }

    private void unactiveCheck(int pos) {
        switch (pos) {
            case 0:
                i1.setImageResource(R.drawable.pin_circle);
                break;
            case 1:
                i2.setImageResource(R.drawable.pin_circle);
                break;
            case 2:
                i3.setImageResource(R.drawable.pin_circle);
                break;
            case 3:
                i4.setImageResource(R.drawable.pin_circle);
                break;
        }
    }
}
