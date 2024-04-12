package pro.adeo.sn2rs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class Sn2rsApplication implements CommandLineRunner {

    private final BatchService batchService;

    public Sn2rsApplication(BatchService batchService) {
        this.batchService = batchService;
    }


    public static void main(String[] args) {
        SpringApplication.run(Sn2rsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        batchService.run();
        }
}