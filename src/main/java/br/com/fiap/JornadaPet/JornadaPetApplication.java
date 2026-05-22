package br.com.fiap.JornadaPet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching  
public class JornadaPetApplication {

    public static void main(String[] args) {
        SpringApplication.run(JornadaPetApplication.class, args);
    }

}
