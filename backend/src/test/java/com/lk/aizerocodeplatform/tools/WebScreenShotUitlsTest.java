package com.lk.aizerocodeplatform.tools;

import com.lk.aizerocodeplatform.service.OssUploadService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/2 18:07
 */
@SpringBootTest
class WebScreenShotUitlsTest {
    @Resource
    private OssUploadService ossUploadService;

    @Test
    void saveWebScreenShot() {
        String path = WebScreenShotCmdUtils.saveWebScreenShot("www.baidu.com");
        String ossPath = ossUploadService.uploadCompressedScreenshot(path);
        System.out.println(ossPath);
    }
}