package dk.lessismore.nojpa.masterworker.worker;

import dk.lessismore.nojpa.masterworker.executor.Executor;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class WorkerStarter {


    public static void main(String[] args) throws ClassNotFoundException {
        String classNames = args[0];
        final List<Class<? extends Executor>> csList = new LinkedList<Class<? extends Executor>>();
        StringTokenizer toks = new StringTokenizer(classNames, ", ");
        while(toks.hasMoreTokens()){
            final Class<?> aClass = Class.forName(toks.nextToken());
            csList.add((Class<? extends Executor>) aClass);
        }
        int workerAmount = Integer.parseInt(args[1]);
        for (int i = 0; i < workerAmount; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                public void run() {
                    System.out.println(finalI + ": Start");
                    new CloudWorker(csList);
                    System.out.println(finalI + ": Done");
                }
            }).start();

        }
    }



    private static class CloudWorker extends Worker {
        public CloudWorker(List<Class<? extends Executor>> cs) {
            super(cs);
        }
    }



}
