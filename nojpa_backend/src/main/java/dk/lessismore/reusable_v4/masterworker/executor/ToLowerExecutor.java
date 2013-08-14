package dk.lessismore.reusable_v4.masterworker.executor;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 18-09-2009
 * Time: 16:27:58
 * To change this template use File | Settings | File Templates.
 */
public class ToLowerExecutor extends Executor<String, String> {

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
        return input.toLowerCase();
    }
}
