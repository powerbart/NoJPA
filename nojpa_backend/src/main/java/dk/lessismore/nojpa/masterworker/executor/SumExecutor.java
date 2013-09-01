package dk.lessismore.nojpa.masterworker.executor;

public class SumExecutor extends Executor<Long, String> {

    public String run(Long n) {
        String sumString = "0";
        for (int i = 1; i <= n; i++) {
            Long sum = Long.parseLong(sumString);
            sum += i;
            sumString = sum.toString();
            setProgress(((double)i)/n);
        }
        //throw new NullPointerException("Just Kidding");
        return sumString;
    }
}