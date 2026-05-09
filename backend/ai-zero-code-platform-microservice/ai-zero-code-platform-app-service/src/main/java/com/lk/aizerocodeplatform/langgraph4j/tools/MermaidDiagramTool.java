package com.lk.aizerocodeplatform.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.langgraph4j.model.ImageCategoryEnum;
import com.lk.aizerocodeplatform.langgraph4j.model.ImageResource;
import com.lk.aizerocodeplatform.service.OssUploadService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 将Mermaid代码转换为架构图图片并上传到阿里云oss工具
 * @author LK
 */
@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private OssUploadService ossUploadService;

    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }

        File diagramFile = null;
        try {
            diagramFile = convertMermaidToPng(mermaidCode);
            String ossUrl = ossUploadService.uploadCompressedScreenshot(diagramFile.getAbsolutePath());
            if (StrUtil.isNotBlank(ossUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(ossUrl)
                        .build());
            }
            log.warn("Mermaid 图片上传 OSS 成功返回空地址，description={}", description);
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        } finally {
            if (diagramFile != null) {
                FileUtil.del(diagramFile);
            }
        }
        return new ArrayList<>();
    }

    /**
     * 将 Mermaid 代码转换为 png 图片
     */
    private File convertMermaidToPng(String mermaidCode) {
        File tempInputFile = null;
        File tempOutputFile = null;
        File tempPuppeteerConfigFile = null;

        try {
            tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
            FileUtil.writeUtf8String(mermaidCode, tempInputFile);

            tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".png", true);

            List<String> command = new ArrayList<>();
            command.add(SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc");
            command.add("-i");
            command.add(tempInputFile.getAbsolutePath());
            command.add("-o");
            command.add(tempOutputFile.getAbsolutePath());
            command.add("-b");
            command.add("transparent");

            String browserExecutablePath = resolveBrowserExecutablePath();
            if (StrUtil.isNotBlank(browserExecutablePath)) {
                tempPuppeteerConfigFile = createPuppeteerConfigFile(browserExecutablePath);
                command.add("-p");
                command.add(tempPuppeteerConfigFile.getAbsolutePath());
                log.info("Mermaid CLI 使用浏览器: {}", browserExecutablePath);
            } else {
                log.warn("未找到可用的 Chrome/Edge 路径，将使用 mmdc 默认浏览器配置");
            }

            CommandResult result = exec(command);

            if (result.exitCode != 0) {
                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        "Mermaid CLI 执行失败，exitCode=" + result.exitCode + "，输出信息：" + result.output
                );
            }

            if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        "Mermaid CLI 执行成功但未生成有效 SVG 文件，输出信息：" + result.output
                );
            }

            return tempOutputFile;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid 转换失败：" + e.getMessage());
        } finally {
            if (tempInputFile != null) {
                FileUtil.del(tempInputFile);
            }
            if (tempPuppeteerConfigFile != null) {
                FileUtil.del(tempPuppeteerConfigFile);
            }
        }
    }

    /**
     * 优先级：
     * 1. 环境变量 PUPPETEER_EXECUTABLE_PATH
     * 2. 环境变量 MERMAID_BROWSER_PATH
     * 3. Windows 默认安装路径下的 Edge / Chrome
     */
    private String resolveBrowserExecutablePath() {
        String envPath = System.getenv("PUPPETEER_EXECUTABLE_PATH");
        if (StrUtil.isNotBlank(envPath) && FileUtil.exist(envPath)) {
            return envPath;
        }

        String mermaidBrowserPath = System.getenv("MERMAID_BROWSER_PATH");
        if (StrUtil.isNotBlank(mermaidBrowserPath) && FileUtil.exist(mermaidBrowserPath)) {
            return mermaidBrowserPath;
        }

        if (SystemUtil.getOsInfo().isWindows()) {
            List<String> candidates = List.of(
                    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe"
            );
            for (String path : candidates) {
                if (FileUtil.exist(path)) {
                    return path;
                }
            }
        }

        return null;
    }

    private File createPuppeteerConfigFile(String browserExecutablePath) {
        File configFile = FileUtil.createTempFile("mermaid_puppeteer_", ".json", true);
        String json = """
                {
                  "executablePath": "%s",
                  "args": ["--no-sandbox", "--disable-setuid-sandbox"]
                }
                """.formatted(browserExecutablePath.replace("\\", "\\\\"));
        FileUtil.writeUtf8String(json, configFile);
        return configFile;
    }

    private CommandResult exec(List<String> command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output;
        try (InputStream inputStream = process.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            output = outputStream.toString(StandardCharsets.UTF_8);
        }

        int exitCode = process.waitFor();
        log.info("执行 Mermaid 命令: {}", String.join(" ", command));
        log.info("Mermaid CLI 输出: {}", output);

        return new CommandResult(exitCode, output);
    }

    private record CommandResult(int exitCode, String output) {
    }
}
