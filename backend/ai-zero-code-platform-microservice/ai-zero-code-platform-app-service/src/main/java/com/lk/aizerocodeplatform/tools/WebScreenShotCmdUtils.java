package com.lk.aizerocodeplatform.tools;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 不依赖 WebDriver 的网页截图工具
 * 原理：直接调用本机 Edge / Chrome 的无头截图能力
 * @author LK
 */
@Slf4j
public class WebScreenShotCmdUtils {

    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 900;
    private static final long PROCESS_TIMEOUT_SECONDS = 30L;

    /**
     * 保存网页截图
     *
     * @param webUrl 网页地址
     * @return 压缩后的截图路径，失败返回 null
     */
    public static String saveWebScreenShot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页地址不能为空");
            return null;
        }

        String finalUrl = normalizeUrl(webUrl);

        String browserPath = resolveBrowserPath();
        if (StrUtil.isBlank(browserPath)) {
            log.error("未找到可用的 Edge/Chrome 浏览器");
            return null;
        }

        String rootPath = System.getProperty("user.dir")
                + File.separator + "temp"
                + File.separator + "webScreenShot"
                + File.separator + UUID.randomUUID().toString().substring(0, 8);

        FileUtil.mkdir(rootPath);

        String pngPath = rootPath + File.separator + UUID.randomUUID() + ".png";
        String jpgPath = rootPath + File.separator + UUID.randomUUID() + "_compress.jpg";
        String userDataDir = rootPath + File.separator + "browser-profile";

        try {
            List<String> command = new ArrayList<>();
            command.add(browserPath);
            command.add("--headless=new");
            command.add("--disable-gpu");
            command.add("--hide-scrollbars");
            command.add("--no-first-run");
            command.add("--no-default-browser-check");
            command.add("--disable-extensions");
            command.add("--disable-dev-shm-usage");
            command.add("--window-size=" + DEFAULT_WIDTH + "," + DEFAULT_HEIGHT);
            command.add("--virtual-time-budget=10000");
            command.add("--run-all-compositor-stages-before-draw");
            command.add("--user-data-dir=" + userDataDir);
            command.add("--screenshot=" + pngPath);
            command.add(finalUrl);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            String output;
            try (InputStream inputStream = process.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                inputStream.transferTo(baos);
                output = baos.toString(StandardCharsets.UTF_8);
            }

            boolean finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("浏览器截图进程超时");
                return null;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("浏览器截图失败，exitCode={}, output={}", exitCode, output);
                return null;
            }

            if (!FileUtil.exist(pngPath)) {
                log.error("截图文件未生成，output={}", output);
                return null;
            }

            ImgUtil.compress(new File(pngPath), new File(jpgPath), 0.3f);
            FileUtil.del(pngPath);
            FileUtil.del(userDataDir);

            return jpgPath;
        } catch (Exception e) {
            log.error("网页截图失败", e);
            return null;
        }
    }

    /**
     * 自动补全协议
     */
    private static String normalizeUrl(String webUrl) {
        String url = webUrl.trim();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return "https://" + url;
    }

    /**
     * 按常见安装路径查找浏览器
     */
    private static String resolveBrowserPath() {
        List<String> candidates = List.of(
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
        );

        for (String path : candidates) {
            if (FileUtil.exist(path)) {
                return path;
            }
        }
        return null;
    }
}
