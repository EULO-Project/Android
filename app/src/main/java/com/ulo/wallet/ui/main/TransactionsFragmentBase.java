package com.ulo.wallet.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseRecyclerFragment;
import com.ulo.wallet.base.adapter.BaseRecyclerAdapter;
import com.ulo.wallet.base.adapter.BaseRecyclerViewHolder;
import com.ulo.wallet.base.adapter.ListItemListeners;
import com.ulo.wallet.ui.transaction.TransactionDetailActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import global.PivxRate;
import global.utils.LogHelper;
import global.wrappers.TransactionWrapper;
import wallet.Coin;
import wallet.MonetaryFormat;
import wallet.transaction.Transaction;

import static com.ulo.wallet.ui.transaction.FragmentTxDetail.IS_DETAIL;
import static com.ulo.wallet.ui.transaction.FragmentTxDetail.TX_WRAPPER;
import static com.ulo.wallet.utils.TxUtils.getAddressOrContact;


/**
 * Created by furszy on 6/29/17.
 */

public class TransactionsFragmentBase extends BaseRecyclerFragment<Transaction> {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsFragmentBase.class);

    private PivxRate pivxRate;
    private MonetaryFormat coinFormat = MonetaryFormat.BTC;
    private int scale = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogHelper.v("TransactionsFragmentBase onCreateView");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setEmptyView(R.drawable.img_transaction_empty);
        setEmptyText(getString(R.string.no_transactions));
        setEmptyTextColor(Color.parseColor("#C7CDD6"));
        return view;
    }

    @Override
    protected List<Transaction> onLoading() {

        pivxModule.checkTransactionChanged();

//        if(pivxModule.checkTransactionChanged()){
        List<Transaction>  list=pivxModule.listTx();
//        }
//        List<TransactionWrapper> list =new ArrayList<>();
//        Collections.sort(list, new Comparator<TransactionWrapper>() {
//            public int compare(TransactionWrapper o1, TransactionWrapper o2) {
//                if (o1.getTransaction().getUpdateTime().getTime() == o2.getTransaction().getUpdateTime().getTime())
//                    return 0;
//                return o1.getTransaction().getUpdateTime().getTime() > o2.getTransaction().getUpdateTime().getTime() ? -1 : 1;
//            }
//        });
        return list;
    }

    @Override
    protected BaseRecyclerAdapter<Transaction, ? extends BaseRecyclerViewHolder> initAdapter() {
        BaseRecyclerAdapter<Transaction, TransactionViewHolderBase> adapter = new BaseRecyclerAdapter<Transaction, TransactionViewHolderBase>(getActivity()) {
            @Override
            protected TransactionViewHolderBase createHolder(View itemView, int type) {
                return new TransactionViewHolderBase(itemView);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.transaction_row;
            }

            @Override
            protected void bindHolder(TransactionViewHolderBase holder, Transaction data, int position) {
                String amount = data.getAmount().toFriendlyString();
                if (amount.length() <= 14) {
                    holder.txt_scale.setVisibility(View.GONE);
                    holder.amount.setText(amount);
                } else {
                    // format amount
                    holder.txt_scale.setVisibility(View.VISIBLE);
                    holder.amount.setText(parseToCoinWith4Decimals(data.getAmount().toPlainString()).toFriendlyString());
                }

                String localCurrency = null;
                if (pivxRate != null) {
                    localCurrency = pivxApplication.getCentralFormats().format(
                            new BigDecimal(data.getAmount().getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + pivxRate.getCode();
                    holder.amountLocal.setText(localCurrency);
                    holder.amountLocal.setVisibility(View.VISIBLE);
                } else {
                    holder.amountLocal.setVisibility(View.INVISIBLE);
                }


//                holder.description.setText(data.getDescription());

                if (data.getIsSpend()) {
                    //holder.cv.setBackgroundColor(Color.RED);Color.GREEN
                    holder.imageView.setImageResource(R.mipmap.ic_transaction_send);
                    holder.amount.setTextColor(ContextCompat.getColor(context, R.color.wathet));
                } else{
//                    if (data.isZcSpend()) {
//                    holder.imageView.setImageResource(R.mipmap.ic_transaction_incognito);
//                    holder.amount.setTextColor(ContextCompat.getColor(context, R.color.wathet));
//                } else if (!data.isStake()) {
                    holder.imageView.setImageResource(R.mipmap.ic_transaction_receive);
                    holder.amount.setTextColor(ContextCompat.getColor(context, R.color.main_item_red));
//                } else {
//                    holder.imageView.setImageResource(R.mipmap.ic_transaction_mining);
//                    holder.amount.setTextColor(ContextCompat.getColor(context, R.color.main_item_red));
                }
//                holder.title.setText(getAddressOrContact(pivxModule, data));
                holder.title.setText(data.getHash());

                /*if (data.getOutputLabels()!=null && !data.getOutputLabels().isEmpty()){
                    AddressLabel contact = data.getOutputLabels().get(0);
                    if (contact!=null) {
                        if (contact.getName() != null)
                            holder.title.setText(contact.getName());
                        else
                            holder.title.setText(contact.getAddresses().get(0));
                    }else {
                        holder.title.setText(data.getTransaction().getOutput(0).getScriptPubKey().getToAddress(pivxModule.getConf().getNetworkParams()).toBase58());
                    }
                }else {
                    holder.title.setText(data.getTransaction().getOutput(0).getScriptPubKey().getToAddress(pivxModule.getConf().getNetworkParams()).toBase58());
                }*/
                String memo = data.getDescription();
                holder.description.setText(memo != null ? memo : "No description");
            }
        };
        adapter.setListEventListener(new ListItemListeners<Transaction>() {
            @Override
            public void onItemClickListener(Transaction data, int position) {
                Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(TX_WRAPPER, data);
                bundle.putBoolean(IS_DETAIL, true);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onLongItemClickListener(Transaction data, int position) {

            }
        });
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());
    }

    /**
     * Converts to a coin with max. 4 decimal places. Last place gets rounded.
     * 0.01234 -> 0.0123
     * 0.01235 -> 0.0124
     *
     * @param input
     * @return
     */
    public Coin parseToCoinWith4Decimals(String input) {
        try {
            return Coin.valueOf(new BigDecimal(parseToCoin(cleanInput(input)).value).setScale(-scale - 1,
                    BigDecimal.ROUND_HALF_UP).setScale(scale + 1).toBigInteger().longValue());
        } catch (Throwable t) {
            if (input != null && input.length() > 0)
                logger.warn("Exception at parseToCoinWith4Decimals: " + t.toString());
            return Coin.ZERO;
        }
    }

    public Coin parseToCoin(String input) {
        if (input != null && input.length() > 0) {
            try {
                return coinFormat.parse(cleanInput(input));
            } catch (Throwable t) {
                logger.warn("Exception at parseToBtc: " + t.toString());
                return Coin.ZERO;
            }
        } else {
            return Coin.ZERO;
        }
    }

    private String cleanInput(String input) {
        input = input.replace(",", ".");
        // don't use String.valueOf(Double.parseDouble(input)) as return value as it gives scientific
        // notation (1.0E-6) which screw up coinFormat.parse
        //noinspection ResultOfMethodCallIgnored
        Double.parseDouble(input);
        return input;
    }
}
