package io.ona.collect.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
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
import io.ona.collect.android.application.Collect;
import io.ona.collect.android.provider.FormsProviderAPI;

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
        holder.text1 = (TextView) row.findViewById(R.id.text1);
        holder.text2 = (TextView) row.findViewById(R.id.text2);
        holder.text3 = (TextView) row.findViewById(R.id.updateLabel);

        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(FormHolder holder) {
        HashMap<String, String> item = holder.formDetails;
        holder.text1.setText(holder.formDetails.get(NewFormDownloadList.FORMNAME));
        holder.text2.setText(String.valueOf(holder.formDetails.get(NewFormDownloadList.FORMID_DISPLAY)));
        if (isFormNew(item.get(NewFormDownloadList.FORM_ID_KEY), item.get(NewFormDownloadList.FORM_VERSION_KEY))) {
            holder.text3.setVisibility(View.GONE);
        }
    }

    public static class FormHolder {
        HashMap<String, String> formDetails;
        TextView text1;
        TextView text2;
        TextView text3;
    }

    public static boolean isFormNew(String formId, String latestVersion) {

        if (formId == null) {
            Log.e("", "isLocalFormSuperseded: server is not OpenRosa-compliant. <formID> is null!");
            return true;
        }

        String[] selectionArgs = {formId};
        String selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=?";
        String[] fields = {FormsProviderAPI.FormsColumns.JR_VERSION};

        Cursor formCursor = null;
        try {
            formCursor = Collect.getInstance().getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, fields, selection, selectionArgs, null);
            if (formCursor.getCount() == 0) {
                // form does not already exist locally
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
