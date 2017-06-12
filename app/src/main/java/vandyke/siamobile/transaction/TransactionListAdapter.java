package vandyke.siamobile.transaction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.Wallet;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TransactionListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Transaction> data;

    private DateFormat df;

    private int red;
    private int green;

    private boolean hideZero = true;

    public TransactionListAdapter(Context context, int layoutResourceId, ArrayList<Transaction> data) {
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
        df = new SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault());
        red = Color.rgb(186, 63, 63); // TODO: choose better colors maybe
        green = Color.rgb(0, 114, 11);
    }

    @Override
    public View getGroupView(int position, boolean b, View view, ViewGroup viewGroup) {
        TransactionHeaderHolder holder;

        if (view == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, viewGroup, false);

            holder = new TransactionHeaderHolder();
            holder.transactionStatus = (TextView)view.findViewById(R.id.transactionStatus);
            holder.transactionValue = (TextView)view.findViewById(R.id.transactionValue);

            view.setTag(holder);
        } else {
            holder = (TransactionHeaderHolder)view.getTag();
        }

        Transaction transaction = data.get(position);

        String timeString;
        if (transaction.getConfirmationDate() == null) {
            timeString = "Unconfirmed";
            holder.transactionStatus.setTextColor(Color.RED);
        } else {
            timeString = df.format(transaction.getConfirmationDate());
        }
        holder.transactionStatus.setText(timeString);

        String valueText = Wallet.hastingsToSC(transaction.getNetValue()).setScale(2, BigDecimal.ROUND_FLOOR).toPlainString();
        if (valueText.equals("0.00")) {
            holder.transactionValue.setTextColor(Color.GRAY);
        } else if (valueText.contains("-")) {
            holder.transactionValue.setTextColor(red);
        } else {
            valueText = "+" + valueText;
            holder.transactionValue.setTextColor(green);
        }
        holder.transactionValue.setText(valueText);

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    public void setData(ArrayList<Transaction> data) {
        this.data = new ArrayList<>(data);
        if (MainActivity.prefs.getBoolean("hideZero", false)) {
            if (!removeZeroTransactions())
                notifyDataSetChanged();
        } else
            notifyDataSetChanged();
    }

    public boolean removeZeroTransactions() {
        boolean changed = false;
        for (int i = 0; i < data.size(); i++)
            if (data.get(i).getNetValueString().equals("0.00")) {
                data.remove(i);
                changed = true;
                i--;
            }
        if (changed)
            notifyDataSetChanged();
        return changed;
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int position) {
        return data.get(position);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition);
    }

    @Override
    public long getGroupId(int position) {
        return position;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    static class TransactionHeaderHolder {
        TextView transactionStatus;
        TextView transactionValue;
    }

    static class TransactionDetailsHolder {

    }
}
