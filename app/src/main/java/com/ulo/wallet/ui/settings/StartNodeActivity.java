package com.ulo.wallet.ui.settings;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseActivity;
import com.ulo.wallet.ui.main.WalletActivity;
import com.ulo.wallet.ui.pincode.PincodeActivity;
import com.ulo.wallet.utils.DialogBuilder;
import com.ulo.wallet.utils.DialogsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import global.PivtrumGlobalData;
import pivtrum.UloNodeData;
import wallet.WalletManager;


/**
 * Created by Neoperol on 6/27/17.
 */

public class StartNodeActivity extends BaseActivity {

    private Button openDialog;
    private Button btnSelectNode;
    private Spinner dropdown;
    private ArrayAdapter<String> adapter;
    private List<String> hosts = new ArrayList<>();

    private List<UloNodeData> trustedNodes;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

        getLayoutInflater().inflate(R.layout.fragment_start_node, container);
        setTitle(R.string.select_node);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        trustedNodes = PivtrumGlobalData.getInstance().listTrustedHosts();
        // Open Dialog
        openDialog = (Button) findViewById(R.id.openDialog);
        openDialog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DialogBuilder dialogBuilder = DialogsUtil.buildtrustedNodeDialog(StartNodeActivity.this, new DialogsUtil.TrustedNodeDialogListener() {
                    @Override
                    public void onNodeSelected(UloNodeData pivtrumPeerData) {
                        if (!trustedNodes.contains(pivtrumPeerData)) {
                            dropdown.setAdapter(null);
                            adapter.clear();
                            hosts = new ArrayList<String>();
                            trustedNodes.add(pivtrumPeerData);
                            for (UloNodeData trustedNode : trustedNodes) {
//                                if (trustedNode.getHost().equals(FURSZY_TESTNET_SERVER)) {
//                                    hosts.add("pivt.furszy.tech");
//                                } else
                                hosts.add(trustedNode.getHost());
                            }
                            adapter.addAll(hosts);
                            dropdown.setAdapter(adapter);
                            dropdown.setSelection(hosts.size() - 1);
                        }
                    }
                });
                dialogBuilder.show();
            }

        });
        findViewById(R.id.btn_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check this..

                dropdown.setSelection(0);
//                pivxApplication.setTrustedServer(null);
//                pivxApplication.stopBlockchain();
//                // now that everything is good, start the service
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        pivxApplication.startPivxService();
//                    }
//                }, TimeUnit.SECONDS.toMillis(5));
//                goNext();
//                finish();
            }
        });

        // Node selected
        btnSelectNode = (Button) findViewById(R.id.btnSelectNode);
        btnSelectNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProgressDialog();
                Executors.newSingleThreadExecutor()
                        .submit(new Runnable() {
                            @Override
                            public void run() {
                                int selected = dropdown.getSelectedItemPosition();
                                UloNodeData selectedNode = trustedNodes.get(selected);
                                try {

                                    pivxModule.addNode(selectedNode.getHost(), selectedNode.getTcpPort());
                                    pivxApplication.getAppConf().saveTrustedNode(selectedNode);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            goNext();
                                            finish();
                                        }
                                    });

                                } catch (WalletManager.WalletErrorException e) {
                                    e.printStackTrace();
                                    showErrorDialog(e.getMessage());
                                }


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressDialog();
                                    }
                                });
//                boolean isStarted = pivxApplication.getAppConf().getTrustedNode() != null;
//                pivxApplication.setTrustedServer(selectedNode);
//
//                if (isStarted) {
//                    pivxApplication.stopBlockchain();
//                    // now that everything is good, start the service
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            pivxApplication.startPivxService();
//                        }
//                    }, TimeUnit.SECONDS.toMillis(5));
//                }
//                goNext();
//                finish();
                            }
                        });

            }
        });

        dropdown = (Spinner) findViewById(R.id.spinner);

        // add connected node if it's not on the list
        UloNodeData pivtrumPeer = pivxApplication.getAppConf().getTrustedNode();
        if (pivtrumPeer != null && !trustedNodes.contains(pivtrumPeer)) {
            trustedNodes.add(pivtrumPeer);
        }

        int selectionPos = 0;

        for (int i = 0; i < trustedNodes.size(); i++) {
            UloNodeData trustedNode = trustedNodes.get(i);
            if (pivtrumPeer != null && pivtrumPeer.getHost().equals(trustedNode.getHost())) {
                selectionPos = i;
            }
//            if (trustedNode.getHost().equals(FURSZY_TESTNET_SERVER)) {
//                hosts.add("pivt.furszy.tech");
//            } else
            hosts.add(trustedNode.getHost());
        }
        adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item, hosts) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.parseColor("#FF333333"));
                view.setPadding(16, 16, 16, 16);
                return view;
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.parseColor("#FF333333"));
                view.setPadding(8, 8, 8, 8);
                return view;
            }
        };
        dropdown.setAdapter(adapter);
        dropdown.setSelection(selectionPos);
    }

    private void goNext() {
        Class clazz = null;
        if (pivxApplication.getAppConf().getPincode() == null) {
            clazz = PincodeActivity.class;
        } else {
            clazz = WalletActivity.class;
        }
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    public static int convertDpToPx(Resources resources, int dp) {
        return Math.round(dp * (resources.getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
