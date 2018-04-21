package foot.project.projectfoot.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalculateFoot {

    HashMap< Integer, ArrayList< Integer > > footData;



    public CalculateFoot( HashMap< Integer, ArrayList< Integer > > data ) {
        footData = data;
    }



    public double[] getHeatMap() {
        double[] heatMap = new double[7];
        double time = getTimeOverMS();

        for( Map.Entry< Integer, ArrayList< Integer > > sensor : footData.entrySet() ) {
            heatMap[ sensor.getKey() ] = calculateDistribution( sensor.getValue(), time );
        }

        return heatMap;
    }



    private double calculateDistribution( ArrayList< Integer > data, double time ) {
        double calculated;
        int sum = 0;

        for( int d: data )
            sum += d;

        calculated = sum / time;

        return calculated;
    }



    private double getTimeOverMS() {
        return footData.get( 1 ).size() / 3.33;
    }
}
