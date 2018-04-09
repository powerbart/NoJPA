package dk.lessismore.nojpa.masterworker.executor;

public class SumExecutor extends Executor<Long, String> {


    static int countOfCalls = 1;


    public String run(Long n) {
        if(countOfCalls++ % 10 == 0){
            System.out.println("We will sleep for 10 sec - START");
            try {
                Thread.sleep(1000 * (countOfCalls % 30));
            } catch (InterruptedException e) {

            }
            System.out.println("We will sleep for 10 sec - DONE");
        }

        String sumString = "0";
        for (int i = 1; i <= n; i++) {
            Long sum = Long.parseLong(sumString);
            sum += i;
            sumString = sum.toString();
            setProgress(((double) i) / n);
            System.out.println("*** SumExecutor("+ n +") " + getProgress());
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        if(n % 2 == 0){
//            throw new NullPointerException("Just Kidding");
//        }
        return sumString;
    }
}