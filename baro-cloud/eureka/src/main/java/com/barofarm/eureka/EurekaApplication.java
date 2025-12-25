package com.barofarm.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {

    /**
     * Eureka Server. [1] ?? ??????? ????? ??/???? ?? ?????. [2] ???????
     * lb://{serviceId} ??? ???? ?? ? ? ?????? ????.
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}
