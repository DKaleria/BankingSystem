package by.grgu.appdiscoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class AppDiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppDiscoveryServiceApplication.class, args);
    }

}
