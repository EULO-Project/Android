package com.ulo.wallet.ui.inputs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseRecyclerFragment;
import com.ulo.wallet.base.adapter.BaseRecyclerAdapter;
import com.ulo.wallet.base.adapter.BaseRecyclerViewHolder;
import com.ulo.wallet.model.UloContext;
import com.ulo.wallet.ui.transaction.FragmentTxDetail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import global.wrappers.InputWrapper;
import wallet.exceptions.TxNotFoundException;
import wallet.transaction.Spend;

/**
 * Created by furszy on 8/14/17.
 */

public class InputsDetailFragment extends BaseRecyclerFragment<Spend> {

    public static final String INTENT_EXTRA_UNSPENT_WRAPPERS = "unspent_wrappers";

    private List<Spend> list;
    private BaseRecyclerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try {
            list = new ArrayList<>();
            setHasOptionsMenu(true);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (intent.hasExtra(INTENT_EXTRA_UNSPENT_WRAPPERS)) {
                    list = (List<Spend>) intent.getSerializableExtra(INTENT_EXTRA_UNSPENT_WRAPPERS);
//                    for (Spend inputWrapper : list) {
//                        inputWrapper.setUnspent(pivxModule.getUnspent(inputWrapper.getParentTxHash(), inputWrapper.getIndex()));
//                    }
                }
            }
            setSwipeRefresh(false);
//        } catch (TxNotFoundException e) {
//            e.printStackTrace();
//            Toast.makeText(getActivity(), R.string.invalid_inputs,Toast.LENGTH_SHORT).show();
//            getActivity().onBackPressed();
//        }
    }

    @Override
    protected List<Spend> onLoading() {
        return new ArrayList<>(list);
    }

    @Override
    protected BaseRecyclerAdapter<Spend, ? extends BaseRecyclerViewHolder> initAdapter() {
        adapter = new BaseRecyclerAdapter<Spend, FragmentTxDetail.DetailOutputHolder>(getActivity()) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

            @Override
            protected FragmentTxDetail.DetailOutputHolder createHolder(View itemView, int type) {
                return new FragmentTxDetail.DetailOutputHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.detail_output_row;
            }

            @Override
            protected void bindHolder(final FragmentTxDetail.DetailOutputHolder holder, final Spend data, int position) {
                holder.txt_num.setText("Position "+position);
                holder.txt_address.setText(data.getAddress());
                holder.txt_value.setText(data.getAmount().toFriendlyString());
            }
        };
        return adapter;
    }
}
