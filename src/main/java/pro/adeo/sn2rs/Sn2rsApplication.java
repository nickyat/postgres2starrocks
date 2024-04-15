package pro.adeo.sn2rs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Sn2rsApplication {

    private final BatchService batchService;

    public Sn2rsApplication(BatchService batchService) {
        this.batchService = batchService;
    }


    public static void main(String[] args) {
        SpringApplication.run(Sn2rsApplication.class, args);
    }

}