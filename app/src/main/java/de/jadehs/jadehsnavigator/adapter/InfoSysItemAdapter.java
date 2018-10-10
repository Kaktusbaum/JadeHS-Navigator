/**
 * This file is part of JadeHS-Navigator.
 *
 * JadeHS-Navigator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JadeHS-Navigator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JadeHS-Navigator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jadehs.jadehsnavigator.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.model.InfoSysItem;
import de.jadehs.jadehsnavigator.view.ExpandableTextView;

public class InfoSysItemAdapter extends BaseAdapter{
    private static final String TAG = "InfoSysItemAdapter";

    private Date date;
    Calendar cal = Calendar.getInstance();
    private Context context;
    private ArrayList<InfoSysItem> infoSysItems;

    public InfoSysItemAdapter(Context context, ArrayList<InfoSysItem> infoSysItems) {
        this.context = context;
        this.infoSysItems = infoSysItems;
    }

    @Override
    public int getCount() {
        return this.infoSysItems.size();
    }

    @Override
    public Object getItem(int position) {
        return this.infoSysItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.infosys_list_item, null);
        }

        /* Set all texts for a single item */
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
        ExpandableTextView txtDescription = (ExpandableTextView) convertView.findViewById(R.id.txtDescription);
        TextView txtFooter = (TextView) convertView.findViewById(R.id.txtFooter);

        txtTitle.setText(this.infoSysItems.get(position).getTitle());
        txtTitle.setOnClickListener(new ButtonOnClickListener(this.infoSysItems.get(position).getLink()));

        txtDescription.setText(Html.fromHtml(this.infoSysItems.get(position).getDescription()));
        //txtDescription.setMovementMethod(LinkMovementMethod.getInstance()); // Automatically convert links to be clickable
        // @todo: change aussehen
        String timestamp = this.infoSysItems.get(position).getCreated();
        String dateStr = "";
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            date = sdf2.parse(timestamp);
            cal.setTime(date);

            dateStr = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR) + "   " +
                      String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", cal.get(Calendar.MINUTE)) + " Uhr";
        }catch (Exception ex){
            Log.wtf(TAG, "Err", ex);
        }

        //txtFooter.setText(this.infoSysItems.get(position).getCreator() + ": " + dateStr);
        txtFooter.setText("");

        return convertView; // Returns the created View
    }

    private class ButtonOnClickListener implements View.OnClickListener {
        String url;
        public ButtonOnClickListener(String url){
            this.url = url;
        }

        @Override
        public void onClick(View v) {
            try{
                final String url = this.url;
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext());
                    builder.setMessage(context.getString(R.string.dialog_open_infosys))
                            .setCancelable(true)
                            .setPositiveButton(context.getString(R.string.positive),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            try {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(url));
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                                context.startActivity(intent);
                                            } catch (Exception ex) {
                                                Log.wtf(TAG, "Failed to open website", ex);
                                            }
                                        }
                                    })
                            .setNegativeButton(context.getString(R.string.negative),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                final DialogInterface dialog,
                                                final int id) {
                                            dialog.dismiss();
                                        }
                                    });
                    final AlertDialog alert = builder.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();
                }else{
                    Toast.makeText(context, context.getString(R.string.failed_to_open_website), Toast.LENGTH_LONG).show();
                }
            }catch (Exception ex){
                Log.wtf(TAG, "Failed to create OnClickListener", ex);
            }
        }
    }
}