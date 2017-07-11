package me.robin.crawler.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderStatus;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2017-07-11.
 * {desc}
 */
@RestController
public class SpiderManagerController {

    @Resource
    private Map<String, Spider> spiderMap;

    @Resource
    private Map<String, SpiderStatus> statusMap;


    @PostMapping("/addUrl")
    @ApiOperation("添加爬虫任务")
    public void addStartUrl(
            @ApiParam(value = "爬虫名称", required = true)
            @RequestParam("spider") String spiderName,
            @ApiParam(value = "链接", required = true)
            @RequestParam("url") String startUrl
    ) {
        spiderMap.get(spiderName).addUrl(startUrl);
    }


    @GetMapping("/showSpiders")
    @ApiOperation("查看爬虫状态")
    public Collection<SpiderStatus> addStartUrl() {
        return statusMap.values();
    }

}
