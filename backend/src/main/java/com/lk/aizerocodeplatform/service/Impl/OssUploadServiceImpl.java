package com.lk.aizerocodeplatform.service.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.lk.aizerocodeplatform.config.OssConfig;
import com.lk.aizerocodeplatform.service.OssUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/2 22:16
 * 文件上传阿里云OSS服务
 */
@Service
@RequiredArgsConstructor
public class OssUploadServiceImpl implements OssUploadService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    @Override
    public String uploadCompressedScreenshot(String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            throw new RuntimeException("截图文件不存在: " + localFilePath);
        }

        String datePath = DateUtil.today().replace("-", "/");
        String objectKey = ossConfig.getDirPrefix()
                + datePath + "/"
                + IdUtil.fastSimpleUUID() + ".jpg";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(file.length());

        try (InputStream inputStream = new FileInputStream(file)) {
            ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream, metadata);
            return ossConfig.buildFileUrl(objectKey);
        } catch (Exception e) {
            throw new RuntimeException("上传 OSS 失败", e);
        }
    }
}
