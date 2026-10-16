package pl.czujka;

import org.springframework.boot.SpringApplication;

public class TestCzujkaApplication {

    public static void main(String[] args) {
        SpringApplication.from(CzujkaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
