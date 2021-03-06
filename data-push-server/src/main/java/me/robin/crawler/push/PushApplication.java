package me.robin.crawler.push;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by LubinXuan on 2017/7/5.
 */
@SpringBootApplication
@ServletComponentScan(basePackages = "me.robin.crawler.push.servlet")
@EnableScheduling
public class PushApplication {

    public static void main(String[] args) {
        SpringApplication.run(PushApplication.class);
    }

}
