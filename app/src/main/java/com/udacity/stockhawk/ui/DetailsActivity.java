package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        Intent intent = getIntent();

        String history = "";
        if (intent != null) {

            history = intent.getStringExtra("history");


            String[] historyrow = history.split("\\r?\\n");
            Date[] dates = new Date[historyrow.length];

            float[] values = new float[historyrow.length];
            Calendar calendar = Calendar.getInstance();
            DataPoint[] DataPoints = new DataPoint[historyrow.length];

//
//        int mYear = calendar.get(Calendar.YEAR);
//        int mMonth = calendar.get(Calendar.MONTH);
//        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

            //  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Set your date format
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd"); // Set your date format

            // history da ga 2017-2016-2015
            for (int i = historyrow.length - 1; i >= 0; i--) {
                int lastElement = historyrow.length - 1;
                String split[] = historyrow[i].split(",");
                //     dates[i]=split[0];
                Long timeStamp = Long.parseLong(split[0], 10);
                calendar.setTimeInMillis(timeStamp);
                Date d = calendar.getTime();
                String currentData = sdf.format(d); // Get Date String according to date format
                dates[lastElement - i] = d;
                values[lastElement - i] = Float.parseFloat(split[1]);
                DataPoints[lastElement - i] = new DataPoint(timeStamp, values[lastElement - i]);
                //      Log.v("details loop", " " + d + " " + values[lastElement-i] + " " + timeStamp );

            }
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(DataPoints);
            graph.addSeries(series);

            // set date label formatter
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, sdf));
            //   graph.getGridLabelRenderer().setLabelFormatter(new formatLabel(double value, boolean isValueX);

            graph.getGridLabelRenderer().setNumHorizontalLabels(6); // only 4 because of the space
            graph.getGridLabelRenderer().setVerticalAxisTitle("unit in $");
// set manual x bounds to have nice steps
            //Log.v("gfhgfhg","max " + dates[historyrow.length-1].getTime());
            graph.getViewport().setMaxX(dates[historyrow.length - 1].getTime());
            graph.getViewport().setMinX(dates[0].getTime());
            graph.getViewport().setXAxisBoundsManual(true);

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
            graph.getGridLabelRenderer().setHumanRounding(false);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String to = dateFormat.format(dates[historyrow.length - 1]);
            String from = dateFormat.format(dates[0]);
            String formatGraphRange = this.getString(R.string.graphRange);
            String title = String.format(Locale.US, formatGraphRange, from, to);
            TextView graphTitle = (TextView) findViewById(R.id.graphRange);
            graphTitle.setText(title);
        }
    }
}
