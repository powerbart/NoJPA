package dk.lessismore.nojpa.db.methodquery;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class NStats<N extends Number> {

    N min;
    N max;
    N sum;
    Long count;
    N mean;
    //N sumOfSquares;
    N stddev;

    public N getMin() {
        return min;
    }

    public N getMax() {
        return max;
    }

    public N getSum() {
        return sum;
    }

    public Long getCount() {
        return count;
    }

    public N getMean() {
        return mean;
    }

    public N getStddev() {
        return stddev;
    }
}
