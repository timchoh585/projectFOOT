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
            heatMap[ sensor.getKey() ] = calculateDistribution( sensor.getValue() );
        }

        return heatMap;
    }



    public double[] calculateHeatMap( double[] total, double time ) {
        double[] heatMap = new double[7];

        for( int i = 0; i < 7; i++ ) heatMap[i] = total[i] / time;

        return heatMap;
    }



    private double calculateDistribution( ArrayList< Integer > data ) {
        int sum = 0;

        for( int d: data )
            sum += d;

        return sum;
    }



    public double getTimeOverMS() {
        return footData.get( 1 ).size() / 3.33;
    }
}
