package dk.lessismore.nojpa.masterworker.worker;

import dk.lessismore.nojpa.masterworker.client.MasterService;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class RestartAllWorkers {


    public static void main(String[] args) {
        MasterService.restartAllWorkers();
    }



}
