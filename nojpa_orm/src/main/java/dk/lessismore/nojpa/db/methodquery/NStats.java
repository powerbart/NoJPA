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

    public void setMin(Double min) {
        this.min = min;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public void setStddev(Double stddev) {
        this.stddev = stddev;
    }
}
