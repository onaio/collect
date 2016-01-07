package io.ona.collect.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import io.ona.collect.android.R;
import io.ona.collect.android.activities.NewFormDownloadList;

/**
 * Created by onamacuser on 23/12/2015.
 */
public class FormListAdapter extends ArrayAdapter<HashMap<String, String>> {

    private List<HashMap<String, String>> items;
    private int layoutResourceId;
    private Context context;

    public FormListAdapter(Context context, int layoutResourceId, List<HashMap<String, String>> items) {
        super(context, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        FormHolder holder;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new FormHolder();
        holder.formDetails = items.get(position);
        holder.text1 = (TextView)row.findViewById(R.id.text1);
        holder.text2 = (TextView)row.findViewById(R.id.text2);

        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(FormHolder holder) {
        holder.text1.setText(holder.formDetails.get(NewFormDownloadList.FORMNAME));
        holder.text2.setText(String.valueOf(holder.formDetails.get(NewFormDownloadList.FORMID_DISPLAY)));
    }

    public static class FormHolder {
        HashMap<String, String> formDetails;
        TextView text1;
        TextView text2;
    }
}
