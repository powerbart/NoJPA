package dk.lessismore.nojpa.masterworker.executor;

public class ToUpperExecutor extends Executor<String, String> {

    public String run(String input) {
        final int loops = 10;
        for(int i = 0; i < loops; i++) {
            try {
                double progress = ((double) i )/loops;
                setProgress(progress);
                Thread.sleep(100);
                System.out.print(""+i+" ");
                if(true){
                    throw new RuntimeException("uuuuphh uppphhhiii");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done");
        return input.toUpperCase();
    }
}
