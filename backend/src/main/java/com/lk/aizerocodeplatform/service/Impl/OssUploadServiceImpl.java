package com.lk.aizerocodeplatform.service.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.lk.aizerocodeplatform.config.OssConfig;
import com.lk.aizerocodeplatform.service.OssUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

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

    @Override
    public String uploadImage(InputStream inputStream, long contentLength, String contentType, String originalFilename) {
        if (inputStream == null) {
            throw new RuntimeException("上传图片失败：inputStream 不能为空");
        }

        String normalizedContentType = normalizeContentType(contentType, originalFilename);
        String extension = resolveExtension(normalizedContentType, originalFilename);

        String datePath = DateUtil.today().replace("-", "/");
        String objectKey = ossConfig.getDirPrefix()
                + datePath + "/"
                + IdUtil.fastSimpleUUID() + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(normalizedContentType);
        if (contentLength >= 0) {
            metadata.setContentLength(contentLength);
        }

        try {
            ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream, metadata);
            return ossConfig.buildFileUrl(objectKey);
        } catch (Exception e) {
            throw new RuntimeException("上传 OSS 失败", e);
        }
    }

    @Override
    public String uploadImage(byte[] bytes, String contentType, String originalFilename) {
        if (bytes == null || bytes.length == 0) {
            throw new RuntimeException("上传图片失败：byte[] 不能为空");
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return uploadImage(inputStream, bytes.length, contentType, originalFilename);
        } catch (Exception e) {
            throw new RuntimeException("上传 OSS 失败", e);
        }
    }

    @Override
    public String uploadImage(URL imageUrl) {
        if (imageUrl == null) {
            throw new RuntimeException("上传图片失败：URL 不能为空");
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new RuntimeException("下载远程图片失败，HTTP 状态码: " + code);
            }

            String contentType = connection.getContentType();
            int contentLength = connection.getContentLength();
            String originalFilename = extractFileName(imageUrl);

            try (InputStream inputStream = connection.getInputStream()) {
                if (contentLength > 0) {
                    return uploadImage(inputStream, contentLength, contentType, originalFilename);
                }

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    inputStream.transferTo(outputStream);
                    return uploadImage(outputStream.toByteArray(), contentType, originalFilename);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("上传远程图片到 OSS 失败: " + imageUrl, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public String uploadImage(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            throw new RuntimeException("上传图片失败：imageUrl 不能为空");
        }

        try {
            return uploadImage(new URL(imageUrl));
        } catch (Exception e) {
            throw new RuntimeException("上传远程图片到 OSS 失败: " + imageUrl, e);
        }
    }

    /**
     * @param contentType      内容类型
     * @param originalFilename 原始文件名
     * @return 图片后缀
     */
    private String normalizeContentType(String contentType, String originalFilename) {
        if (StrUtil.isNotBlank(contentType)) {
            String lower = contentType.toLowerCase(Locale.ROOT);
            if (lower.contains("png")) {
                return "image/png";
            }
            if (lower.contains("jpeg") || lower.contains("jpg")) {
                return "image/jpeg";
            }
            if (lower.contains("webp")) {
                return "image/webp";
            }
            if (lower.contains("gif")) {
                return "image/gif";
            }
            if (lower.contains("svg")) {
                return "image/svg+xml";
            }
        }

        String ext = getExtension(originalFilename);
        return switch (ext) {
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".webp" -> "image/webp";
            case ".gif" -> "image/gif";
            case ".svg" -> "image/svg+xml";
            default -> "application/octet-stream";
        };
    }

    /**
     * 解析后缀
     *
     * @param contentType      内容类型
     * @param originalFilename 原始文件名
     * @return 解析后的后缀
     */
    private String resolveExtension(String contentType, String originalFilename) {
        String ext = getExtension(originalFilename);
        if (StrUtil.isNotBlank(ext)) {
            return ext;
        }

        if (StrUtil.isBlank(contentType)) {
            return ".bin";
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/svg+xml" -> ".svg";
            default -> ".bin";
        };
    }

    /**
     * 获取文件后缀
     *
     * @param filename 文件名
     * @return 文件后缀
     */
    private String getExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }

        String cleanName = filename;
        int queryIndex = cleanName.indexOf('?');
        if (queryIndex >= 0) {
            cleanName = cleanName.substring(0, queryIndex);
        }

        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleanName.length() - 1) {
            return "";
        }

        return cleanName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    /**
     * 获取文件名
     *
     * @param url 文件的url
     * @return 文件名
     */
    private String extractFileName(URL url) {
        String path = url.getPath();
        if (StrUtil.isBlank(path)) {
            return null;
        }

        int slashIndex = path.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == path.length() - 1) {
            return path;
        }

        return path.substring(slashIndex + 1);
    }
}
