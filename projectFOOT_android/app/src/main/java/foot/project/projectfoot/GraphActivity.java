package foot.project.projectfoot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GraphActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_graph );

        createGraphWithPoints();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void createGraphWithPoints() {
        Intent i = getIntent();
        //get object from other activity
        HashMap<Integer, ArrayList<Integer>> mapNum = (HashMap<Integer, ArrayList<Integer>>) i.getSerializableExtra("hashmap");

        //Main graph object
        GraphView graph = (GraphView) findViewById(R.id.graph);


        //View of the graph renderer properties
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Sensors");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Force");
        graph.getGridLabelRenderer().setGridColor(Color.BLACK);


        double[] lowForceValues = new double[7];
        double[] highForceValues = new double[7];
        double[] avgForceValues = new double[7];

        int lineThickness = 10;
        int pointThickness = 12;

        //put values from hashmap into arrays for graph
        for( Map.Entry< Integer, ArrayList< Integer > > sensor : mapNum.entrySet() ) {
            lowForceValues[sensor.getKey()] = Collections.min(sensor.getValue());
            highForceValues[sensor.getKey()] = Collections.max(sensor.getValue());
            avgForceValues[sensor.getKey()] = 0;
            int numValues = 0;
            for(int d: sensor.getValue()){
                avgForceValues[sensor.getKey()] += d;
                numValues++;
            }
            avgForceValues[sensor.getKey()] /= numValues;
        }

        //Low Force values Graph
        LineGraphSeries<DataPoint> seriesLow = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, lowForceValues[0]),
                new DataPoint(2, lowForceValues[1]),
                new DataPoint(3, lowForceValues[2]),
                new DataPoint(4, lowForceValues[3]),
                new DataPoint(5, lowForceValues[4]),
                new DataPoint(6, lowForceValues[5]),
                new DataPoint(7, lowForceValues[6])
        });
        //Properties for Low Series
        seriesLow.setTitle("Lowest Force");
        seriesLow.setColor(Color.GREEN);
        seriesLow.setThickness(lineThickness);
        seriesLow.setDataPointsRadius(pointThickness);
        seriesLow.setDrawDataPoints(true);


        //High Force values Graph
        LineGraphSeries<DataPoint> seriesHigh = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, highForceValues[0]),
                new DataPoint(2, highForceValues[1]),
                new DataPoint(3, highForceValues[2]),
                new DataPoint(4, highForceValues[3]),
                new DataPoint(5, highForceValues[4]),
                new DataPoint(6, highForceValues[5]),
                new DataPoint(7, highForceValues[6])
        });
        //Properties for High values
        seriesHigh.setTitle("Highest Force");
        seriesHigh.setColor(Color.RED);
        seriesHigh.setThickness(lineThickness);
        seriesHigh.setDataPointsRadius(pointThickness);
        //*********************Remove if the color below this line blocks other lines
        seriesHigh.setDrawBackground(true);
        seriesHigh.setBackgroundColor(Color.argb(60,255, 51, 51 ));

        //Average Force values Graph
        LineGraphSeries<DataPoint> seriesAvg = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, avgForceValues[0]),
                new DataPoint(2, avgForceValues[1]),
                new DataPoint(3, avgForceValues[2]),
                new DataPoint(4, avgForceValues[3]),
                new DataPoint(5, avgForceValues[4]),
                new DataPoint(6, avgForceValues[5]),
                new DataPoint(7, avgForceValues[6])
        });
        seriesAvg.setTitle("Average Force");
        seriesAvg.setColor(Color.YELLOW);
        seriesAvg.setThickness(lineThickness);
        seriesAvg.setDataPointsRadius(pointThickness);


        //Add each line series to the main graph
        graph.addSeries(seriesLow);
        graph.addSeries(seriesHigh);
        graph.addSeries(seriesAvg);


        //Legend for graph
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.MIDDLE);
        graph.getGridLabelRenderer().setNumHorizontalLabels(7);

    }
}