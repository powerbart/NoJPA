package dk.lessismore.nojpa.db.methodquery;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class NStats<N extends Number> {

    Double min;
    Double max;
    Double sum;
    Long count;
    Double mean;
    //N sumOfSquares;
    Double stddev;

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }

    public Double getSum() {
        return sum;
    }

    public Long getCount() {
        return count;
    }

    public Double getMean() {
        return mean;
    }

    public Double getStddev() {
        return stddev;
    }
}
