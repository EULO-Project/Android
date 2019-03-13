package com.ulo.wallet.ui.contacts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseDrawerActivity;
import com.ulo.wallet.base.adapter.ListItemListeners;
import com.ulo.wallet.base.dialog.SimpleTwoButtonsDialog;
import com.ulo.wallet.ui.transaction.SendActivity;
import com.ulo.wallet.utils.DialogsUtil;
import com.ulo.wallet.utils.NavigationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import global.AddressLabel;

import static com.ulo.wallet.ui.transaction.SendActivity.INTENT_ADDRESS;

/**
 * Created by Neoperol on 5/11/17.
 */


public class ContactsActivity extends BaseDrawerActivity implements ListItemListeners<AddressLabel> {

    RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<AddressLabel> addressLabels;
    private LinearLayout emptyView;
    private ExecutorService executor;

    private SimpleTwoButtonsDialog deleteAddressLabelDialog;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_address, container);
        setTitle(R.string.address_book_screen_title);
        recyclerView = (RecyclerView) findViewById(R.id.addressList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ContactsAdapter(this);
        adapter.setListEventListener(this);
        recyclerView.setAdapter(adapter);
        emptyView = (LinearLayout) findViewById(R.id.empty_view);
        emptyView.setVisibility(View.GONE);

        setRighTwo(R.drawable.ic_add);
        rightTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsActivity.this, AddContactActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNavigationMenuItemChecked(1);

        load();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private void load() {
        // add loading..
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
        executor.submit(new Runnable() {
            @Override
            public void run() {
                addressLabels = pivxModule.getContacts();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (addressLabels == null || addressLabels.isEmpty()) {
                            adapter.changeDataSet(new ArrayList());
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            adapter.changeDataSet(new ArrayList(addressLabels));
                            emptyView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.address, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_add:
//                Intent intent = new Intent(this, AddContactActivity.class);
//                startActivity(intent);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }


    @Override
    public void onItemClickListener(AddressLabel data, int position) {
        Intent intent=new Intent(this, SendActivity.class);
        intent.putExtra(INTENT_ADDRESS,data.getAddresses().get(0));
        startActivity(intent);
        finish();

    }

    @Override
    public void onLongItemClickListener(AddressLabel oldData, int position) {
        final AddressLabel data = addressLabels.get(position);
        SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener listener = new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
            @Override
            public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                pivxModule.deleteAddressLabel(data);
                load();
                Toast.makeText(ContactsActivity.this, R.string.address_label_deleted, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }

            @Override
            public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                dialog.dismiss();
            }
        };
        if (deleteAddressLabelDialog == null) {
            deleteAddressLabelDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                    this,
                    getString(R.string.options_menu),
                    getString(R.string.delete_address_label_text, data.getAddresses().get(0)),
                    listener
            );
        } else {
            deleteAddressLabelDialog.setListener(listener);
            deleteAddressLabelDialog.setBody(getString(R.string.delete_address_label_text, data.getAddresses().get(0)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            deleteAddressLabelDialog.setRightBtnTextColor(getColor(R.color.bgPurple));
        } else {
            deleteAddressLabelDialog.setRightBtnTextColor(ContextCompat.getColor(this, R.color.bgPurple));
        }

        deleteAddressLabelDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationUtils.goBackToHome(this);
    }
}
