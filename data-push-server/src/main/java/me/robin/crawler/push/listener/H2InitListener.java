package me.robin.crawler.push.listener;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.SQLException;

/**
 * Created by Lubin.Xuan on 2017-07-11.
 * {desc}
 */
@WebListener("H2数据库启动监听器")
public class H2InitListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(H2InitListener.class);

    private Server server;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("正在启动h2数据库...");
            //使用org.h2.tools.Server这个类创建一个H2数据库的服务并启动服务，由于没有指定任何参数，那么H2数据库启动时默认占用的端口就是8082
            server = Server.createTcpServer("-baseDir","E://spider_db").start();
            logger.info("h2数据库启动成功...");
        } catch (SQLException e) {
            logger.error("启动h2数据库出错", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (this.server != null) {
            // 停止H2数据库
            this.server.stop();
            this.server = null;
        }
    }
}
