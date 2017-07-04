package me.robin.crawler.push;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by LubinXuan on 2017/7/5.
 */
@SpringBootApplication
@Controller
public class PushApplication {

    @PostMapping("push/data")
    public void data(@RequestBody String data) {
        CrawlerDataDisruptor.ins.pushData(data);
    }

    public static void main(String[] args) {
        SpringApplication.run(PushApplication.class);
    }

}
