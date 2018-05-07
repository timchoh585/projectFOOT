package foot.project.projectfoot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
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

        Intent i = getIntent();
        HashMap<Integer, ArrayList<Integer>> mapNum = (HashMap<Integer, ArrayList<Integer>>) i.getSerializableExtra("hashmap");
        GraphView graph = (GraphView) findViewById(R.id.graph);


        double[] lowForceValues = new double[7];
        double[] highForceValues = new double[7];
        double[] avgForceValues = new double[7];


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


        LineGraphSeries<DataPoint> seriesLow = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, lowForceValues[0]),
                new DataPoint(2, lowForceValues[1]),
                new DataPoint(3, lowForceValues[2]),
                new DataPoint(4, lowForceValues[3]),
                new DataPoint(5, lowForceValues[4]),
                new DataPoint(6, lowForceValues[5]),
                new DataPoint(7, lowForceValues[6])
        });
        LineGraphSeries<DataPoint> seriesHigh = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, highForceValues[0]),
                new DataPoint(2, highForceValues[1]),
                new DataPoint(3, highForceValues[2]),
                new DataPoint(4, highForceValues[3]),
                new DataPoint(5, highForceValues[4]),
                new DataPoint(6, highForceValues[5]),
                new DataPoint(7, highForceValues[6])
        });
        LineGraphSeries<DataPoint> seriesAvg = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, avgForceValues[0]),
                new DataPoint(2, avgForceValues[1]),
                new DataPoint(3, avgForceValues[2]),
                new DataPoint(4, avgForceValues[3]),
                new DataPoint(5, avgForceValues[4]),
                new DataPoint(6, avgForceValues[5]),
                new DataPoint(7, avgForceValues[6])
        });

        graph.addSeries(seriesLow);
        graph.addSeries(seriesHigh);
        graph.addSeries(seriesAvg);
        seriesLow.setColor(Color.GREEN);
        seriesHigh.setColor(Color.RED);
        seriesAvg.setColor(Color.YELLOW);

        GridLabelRenderer graphView = new GridLabelRenderer(graph);
        graphView.setHorizontalAxisTitle("Sensors");
        graphView.setVerticalAxisTitle("Force");
    }
}

