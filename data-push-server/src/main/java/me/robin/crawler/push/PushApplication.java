package me.robin.crawler.push;

import me.robin.crawler.push.listener.H2InitListener;
import me.robin.crawler.push.servlet.CursorServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by LubinXuan on 2017/7/5.
 */
@SpringBootApplication
@ServletComponentScan(basePackageClasses = {CursorServlet.class, H2InitListener.class})
@EnableScheduling
public class PushApplication {

    public static void main(String[] args) {
        SpringApplication.run(PushApplication.class);
    }

}
