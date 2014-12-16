package dk.lessismore.nojpa.db.methodquery;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class NStats<N extends Number> {

    Double min = 0d;
    Double max = 0d;
    Double sum = 0d;
    Long count = 0l;
    Double mean = 0d;
    //N sumOfSquares;
    Double stddev = 0d;

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
