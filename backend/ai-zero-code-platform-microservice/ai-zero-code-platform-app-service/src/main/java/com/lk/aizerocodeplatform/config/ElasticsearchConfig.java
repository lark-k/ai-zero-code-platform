package com.lk.aizerocodeplatform.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/19 20:14
 */
@Configuration
public class ElasticsearchConfig {

    /**
     * ES 主机地址。
     * <p>
     * 默认使用 127.0.0.1，方便本地开发。
     * 如果后续部署到服务器，只需要改 application-local.yaml / application-prod.yaml。
     */
    @Value("${elasticsearch.host:127.0.0.1}")
    private String host;

    /**
     * ES HTTP 端口。
     * <p>
     * Elasticsearch 默认 REST API 端口是 9200。
     */
    @Value("${elasticsearch.port:9200}")
    private int port;

    /**
     * 请求协议。
     * <p>
     * 本地 ES 7.17 通常是 http。
     * 如果你后续开启 xpack security / HTTPS，可以改成 https。
     */
    @Value("${elasticsearch.scheme:http}")
    private String scheme;

    /**
     * 注册 ES 7 High Level REST Client。
     * <p>
     * destroyMethod = "close" 表示 Spring 容器关闭时自动释放 HTTP 连接资源。
     */
    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme))
        );
    }
}
