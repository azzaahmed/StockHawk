package com.udacity.stockhawk.widget;


/**
 * Created by azza ahmed on 4/4/2017.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;

/**
 * RemoteViewsService controlling the data being shown in the scrollable stock detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();


      DecimalFormat dollarFormatWithPlus=null;
      DecimalFormat dollarFormat=null;
      DecimalFormat percentageFormat=null;

    // these indices must match the projection
    static final int INDEX_Stock_ID = 0;
    static final int INDEX_SYMBOL= 1;
    static final int INDEX_PRICE= 2;
    static final int INDEX_ABSOLUTE_CHANGE = 3;
    static final int INDEX_PERCENTAGE_CHANGE = 4;
    static final int INDEX_HISTORY = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
                dollarFormatWithPlus.setPositivePrefix("+$");
                dollarFormatWithPlus.setNegativePrefix("-$");
                dollarFormat.setNegativePrefix("$");
                dollarFormat.setPositivePrefix("$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission

                final long identityToken = Binder.clearCallingIdentity();
                Set<String> stockPref = PrefUtils.getStocks(getApplicationContext());
              String[] s=  Contract.Quote.QUOTE_COLUMNS.toArray(new String[stockPref.size()]);
                 data = getContentResolver().query(Contract.Quote.URI,
                       s,
                        null,
                        null,null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                Float price = data.getFloat(INDEX_PRICE);
                String symbol = data.getString(INDEX_SYMBOL);
                float absolute = data.getFloat(INDEX_ABSOLUTE_CHANGE);
                float percentage = data.getFloat(INDEX_PERCENTAGE_CHANGE);
                String history =data.getString(INDEX_HISTORY);

               String p=dollarFormat.format(price);
                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price,p);
                if (absolute > 0) {
                 //   views.setInt(R.id.change, "setBackgroundColor", R.drawable.percent_change_pill_green);
                         //   android.graphics.Color.BLACK);
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {


                  //  views.setInt(R.id.change, "setBackgroundColor",R.drawable.percent_change_pill_red);
                    views.setInt(R.id.change, "setBackgroundResource",R.drawable.percent_change_pill_red);

                }

                String change = dollarFormatWithPlus.format(absolute);
                String percentages = percentageFormat.format(percentage / 100);

          if (PrefUtils.getDisplayMode(getApplicationContext()).equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))) {
              views.setTextViewText(R.id.change,change);
                } else {
              views.setTextViewText(R.id.change, percentages);
                }


                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra("history",history);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_Stock_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}