package dk.lessismore.reusable_v4.masterworker.executor;

public class ToUpperExecutor extends Executor<String, String> {

    public String run(String input) {
        final int loops = 50;
        for(int i = loops; i > 0; i--) {
            try {
                double progress = ((double)loops-i)/loops;
                setProgress(progress);
                Thread.sleep(100);
                System.out.print(""+i+" ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done");
        return input.toUpperCase();
    }
}
