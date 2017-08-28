package dk.lessismore.nojpa.masterworker;

import dk.lessismore.nojpa.masterworker.master.JobPool;
import dk.lessismore.nojpa.masterworker.master.Master;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class TestMaster {

    private static final Logger log = LoggerFactory.getLogger(TestMaster.class);


    public static void main(String[] args) throws IOException, InterruptedException {
        log.debug("DEBUGGING");
        log.info("DEBUGGING-INFO");
        log.warn("DEBUGGING-WARN");
        log.error("DEBUGGING-ERROR");
        Master.main(null);
    }

}
