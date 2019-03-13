package com.ulo.wallet.ui.contacts;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseActivity;
import com.ulo.wallet.utils.scanner.ScanActivity;

import org.pivxj.uri.PivxURI;

import global.AddressLabel;
import global.exceptions.ContactAlreadyExistException;

import static android.Manifest.permission_group.CAMERA;
import static com.ulo.wallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 6/8/17.
 */

public class AddContactActivity extends BaseActivity implements View.OnClickListener {
    public static final String ADDRESS_TO_ADD = "address";
    private static final int SCANNER_RESULT = 122;

    private View root;
    private EditText edit_name;
    private EditText edit_address;
    private ImageView imgQr;
    private String address;
    private String name;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_new_address, container);
        setTitle("New Address Label");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        edit_name = (EditText) root.findViewById(R.id.edit_name);
        edit_address = (EditText) root.findViewById(R.id.edit_address);
        imgQr = (ImageView) root.findViewById(R.id.img_qr);
        imgQr.setOnClickListener(this);
        edit_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String temp = s.toString();
                if (pivxModule.chechAddress(temp)) {
                    address = temp;
                    edit_address.setTextColor(Color.parseColor("#55476c"));
                } else {
                    edit_address.setTextColor(Color.parseColor("#4d4d4d"));
                }
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(ADDRESS_TO_ADD)) {
                edit_address.setText(intent.getStringExtra(ADDRESS_TO_ADD));
            }
        }

        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = edit_name.getText().toString();
                if (address != null) {
                    if (name.length() > 0 && address.length() > 0) {
                        try {
                            if (!pivxModule.chechAddress(address)) {
                                Toast.makeText(AddContactActivity.this, R.string.invalid_input_address, Toast.LENGTH_LONG).show();
                                return;
                            }
                            AddressLabel addressLabel = new AddressLabel(name);
                            addressLabel.addAddress(address);
                            pivxModule.saveContact(addressLabel);
                            Toast.makeText(AddContactActivity.this, "AddressLabel saved", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        } catch (ContactAlreadyExistException e) {
                            Toast.makeText(AddContactActivity.this, R.string.contact_already_exist, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(AddContactActivity.this, R.string.invalid_input_address, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_qr) {
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            startActivityForResult(new Intent(this, ScanActivity.class), SCANNER_RESULT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT) {
            if (resultCode == RESULT_OK) {
                try {
                    String address = data.getStringExtra(INTENT_EXTRA_RESULT);
                    String usedAddress;
                    if (pivxModule.chechAddress(address)) {
                        usedAddress = address;
                    } else {
                        PivxURI pivxUri = new PivxURI(address);
                        usedAddress = pivxUri.getAddress().toBase58();
                    }
                    final String tempPubKey = usedAddress;
                    edit_address.setText(tempPubKey);
                } catch (Exception e) {
                    Toast.makeText(this, "Bad address", Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
