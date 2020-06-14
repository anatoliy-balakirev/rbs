package com.rbs;

import com.rbs.config.BookingsProperties;
import com.rbs.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, BookingsProperties.class})
public class RbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RbsApplication.class, args);
    }

}
