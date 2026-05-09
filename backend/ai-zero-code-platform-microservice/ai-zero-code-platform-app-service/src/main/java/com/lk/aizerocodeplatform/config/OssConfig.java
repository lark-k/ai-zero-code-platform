package com.lk.aizerocodeplatform.config;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/2 21:58
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class OssConfig {

    private String endpoint;
    private String bucketName;
    private String domain;
    private String accessKeyId;
    private String accessKeySecret;
    private String dirPrefix;

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    public String buildFileUrl(String objectKey) {
        if (domain != null && !domain.isBlank()) {
            return domain + "/" + objectKey;
        }
        return endpoint.replace("https://", "https://" + bucketName + ".")
                .replace("http://", "http://" + bucketName + ".") + "/" + objectKey;
    }
}

