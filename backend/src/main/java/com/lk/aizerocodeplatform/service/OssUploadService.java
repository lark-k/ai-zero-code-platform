package com.lk.aizerocodeplatform.service;

import java.io.InputStream;
import java.net.URL;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/3 16:01
 */
public interface OssUploadService {
    /**
     * 上传压缩后的截图到阿里云OSS中
     *
     * @param localFilePath 本地压缩后的截图所在位置
     * @return 可访问的OSS路径
     */
    String uploadCompressedScreenshot(String localFilePath);

    /**
     * 上传图片
     *
     * @param inputStream      图片输入流
     * @param contentLength    内容长度
     * @param contentType      内容类型
     * @param originalFilename 原始文件名
     * @return 可访问的OSS路径
     */
    String uploadImage(InputStream inputStream, long contentLength, String contentType, String originalFilename);

    /**
     * 上传图片
     *
     * @param bytes            图片字节数组
     * @param contentType      内容类型
     * @param originalFilename 原始文件名
     * @return 可访问的OSS路径
     */
    String uploadImage(byte[] bytes, String contentType, String originalFilename);

    /**
     * 上传图片
     *
     * @param imageUrl 图片的url
     * @return 可访问的OSS路径
     */
    String uploadImage(URL imageUrl);

    /**
     * 上传图片
     *
     * @param imageUrl 图片的字符串url
     * @return 可访问的OSS路径
     */
    String uploadImage(String imageUrl);
}
