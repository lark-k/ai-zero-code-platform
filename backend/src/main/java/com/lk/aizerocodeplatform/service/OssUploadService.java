package com.lk.aizerocodeplatform.service;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/3 16:01
 */
public interface OssUploadService {
    /**
     * 上传压缩后的截图到阿里云OSS中
     * @param localFilePath 本地压缩后的截图所在位置
     * @return 可访问的OSS路径
     */
    String uploadCompressedScreenshot(String localFilePath);
}
