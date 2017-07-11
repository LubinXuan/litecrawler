package me.robin.crawler.service;

import me.robin.crawler.common.KVStoreClient;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Lubin.Xuan on 2017-07-11.
 * {desc}
 */
@Service
public class SpiderService {

    private static final Logger logger = LoggerFactory.getLogger(SpiderService.class);

    @Resource
    private DataSource dataSource;

    private QueryRunner queryRunner;

    @PostConstruct
    private void init() {
        this.queryRunner = new QueryRunner(dataSource);
        KVStoreClient.setService(this);
    }

    public String getValue(String key) {
        try {
            return this.queryRunner.query("select * from KV_STORE where key = ?", new ResultSetHandler<String>() {
                @Override
                public String handle(ResultSet rs) throws SQLException {
                    return rs.getString(1);
                }
            }, key);
        } catch (SQLException e) {
            logger.warn("数据库读取异常", e);
            return null;
        }
    }

    public void setValue(String key, String value) {
        try {
            this.queryRunner.update("merge into KV_STORE(key,value) values(?,?)", key, value);
        } catch (SQLException e) {
            logger.warn("数据库保存异常", e);
        }
    }
}
