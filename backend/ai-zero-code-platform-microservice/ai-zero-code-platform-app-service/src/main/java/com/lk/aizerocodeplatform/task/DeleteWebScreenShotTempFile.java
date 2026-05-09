package com.lk.aizerocodeplatform.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/3 01:22
 * 定时删除temp文件夹下的截图临时文件夹
 */
@Slf4j
@EnableScheduling
@Configuration
public class DeleteWebScreenShotTempFile {
    private static final String ROOT_DIR = System.getProperty("user.dir") + File.separator
            + "temp" + File.separator + "webScreenShot";

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanUpScreenShotDir() {
        log.info("开始定时清理过期的临时截图文件夹");
        File file = new File(ROOT_DIR);
        if (file.exists()) {
            try {
                FileUtil.del(file);
            } catch (IORuntimeException e) {
                log.error("定时清理过期的临时截图文件失败：{}", e.getMessage());
            }
        }
    }
}
