package cumulocity.microservice.translation.manager;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
import org.springframework.boot.SpringApplication;

@MicroserviceApplication
public class App {
    public static void main (String[] args) {
        SpringApplication.run(App.class, args);
    }
}