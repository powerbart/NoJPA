package dk.lessismore.masterapp;


import dk.lessismore.nojpa.masterworker.master.Master;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by niakoi on 13/4/16.
 */
@SpringBootApplication(scanBasePackages = "dk.lessismore")
public class MasterWorkerApplication implements CommandLineRunner {

    @Value("${master:false}")
    boolean isMaster;

    public static void main(String[] args) {
        System.out.println("*** M 1");
        SpringApplication app = new SpringApplication(MasterWorkerApplication.class);
        System.out.println("*** M 2");
        app.setWebEnvironment(false);
        System.out.println("*** M 3");
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("MasterWorkerApplication: ******* Starting version 1.0.0 ***** ");
        Master.main(null);
    }
}
