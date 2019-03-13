package com.ulo.wallet.ui.transaction.custom;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseActivity;
import com.ulo.wallet.utils.DialogsUtil;

import static com.ulo.wallet.ui.transaction.custom.CustomFeeFragment.*;


/**
 * Created by furszy on 8/3/17.
 */

public class CustomFeeActivity extends BaseActivity {

    private View root;
    private CustomFeeFragment customFeeFragment;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.custom_fee_main, container);
        setTitle("Custom fee");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        customFeeFragment = (CustomFeeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_custom_fee);
        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    CustomFeeFragment.FeeSelector feeSelector = customFeeFragment.getFee();
                    intent.putExtra(INTENT_EXTRA_IS_FEE_PER_KB, feeSelector.isFeePerKbSelected());
                    intent.putExtra(INTENT_EXTRA_IS_TOTAL_FEE, !feeSelector.isFeePerKbSelected());
                    intent.putExtra(INTENT_EXTRA_IS_MINIMUM_FEE, feeSelector.isPayMinimum());
                    intent.putExtra(INTENT_EXTRA_FEE, feeSelector.getAmount());
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (InvalidFeeException e) {
                    e.printStackTrace();
                    DialogsUtil.buildSimpleErrorTextDialog(CustomFeeActivity.this, getString(R.string.invalid_inputs), e.getMessage()).show(getFragmentManager(), "custom_fee_invalid_inputs");
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_default) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_CLEAR, true);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
