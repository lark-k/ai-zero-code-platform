package com.lk.aizerocodeplatform.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.lk.aizerocodeplatform.constant.CodeFileSaveConstant;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.service.AppService;
import com.lk.aizerocodeplatform.service.CodeDownloadService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author lk
 * @Version 1.0
 * @ Date 2026/5/3 16:27
 */
@Service
@Slf4j
public class CodeDownloadServiceImpl implements CodeDownloadService {

    /**
     * 需要排除的目录
     */
    private static final Set<String> EXCLUDED_DIR_NAMES = Set.of(
            "dist",
            "node_modules",
            ".git",
            ".idea",
            ".vscode"
    );

    /**
     * 需要排除的文件
     */
    private static final Set<String> EXCLUDED_FILE_NAMES = Set.of(
            ".DS_Store"
    );

    @Resource
    private AppService appService;

    @Override
    public void downloadAppCodeZip(Long appId, UserLoginVO userLoginVO, HttpServletResponse response) {
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userLoginVO == null, ErrorCode.NOT_LOGIN_ERROR);

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!userLoginVO.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR);

        File sourceDir = getSourceDir(app);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.NOT_FOUND_ERROR, "源码目录不存在");

        String zipRootDirName = buildZipRootDirName(app);
        String zipFileName = zipRootDirName + ".zip";
        prepareResponse(response, zipFileName);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {
            zipDirectory(sourceDir.toPath(), sourceDir.toPath(), zipRootDirName, zipOutputStream);
            zipOutputStream.finish();
        } catch (AsyncRequestNotUsableException | ClientAbortException e) {
            log.warn("client aborted zip download, appId={}, sourceDir={}", appId, sourceDir.getAbsolutePath());
        } catch (IOException e) {
            log.error("download zip failed, appId={}, sourceDir={}", appId, sourceDir.getAbsolutePath(), e);
            handleStreamException(response);
        }
    }

    /**
     * 获取源码目录
     * vue_project 下载整个源码工程，但会排除 dist、node_modules
     */
    private File getSourceDir(App app) {
        String codeGenType = app.getCodeGenType();
        String sourceDir;

        if ("vue_project".equals(codeGenType)) {
            sourceDir = CodeFileSaveConstant.ROOT_PATH
                    + File.separator
                    + "vue_project_" + app.getId();
        } else {
            sourceDir = CodeFileSaveConstant.ROOT_PATH
                    + File.separator
                    + app.getId() + "_" + codeGenType;
        }
        return new File(sourceDir);
    }

    /**
     * 设置下载响应头
     */
    private void prepareResponse(HttpServletResponse response, String zipFileName) {
        String encodedFileName = URLEncoder.encode(zipFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.reset();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    /**
     * 递归压缩目录
     */
    private void zipDirectory(Path currentPath,
                              Path sourceRoot,
                              String zipRootDirName,
                              ZipOutputStream zipOutputStream) throws IOException {

        if (Files.isSymbolicLink(currentPath)) {
            return;
        }

        String currentName = currentPath.getFileName() == null ? "" : currentPath.getFileName().toString();

        if (Files.isDirectory(currentPath)) {
            if (EXCLUDED_DIR_NAMES.contains(currentName)) {
                return;
            }

            try (DirectoryStream<Path> children = Files.newDirectoryStream(currentPath)) {
                boolean emptyDir = true;
                for (Path child : children) {
                    emptyDir = false;
                    zipDirectory(child, sourceRoot, zipRootDirName, zipOutputStream);
                }

                if (emptyDir && !currentPath.equals(sourceRoot)) {
                    String relativePath = sourceRoot.relativize(currentPath).toString().replace("\\", "/");
                    ZipEntry zipEntry = new ZipEntry(zipRootDirName + "/" + relativePath + "/");
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.closeEntry();
                }
            }
            return;
        }

        if (EXCLUDED_FILE_NAMES.contains(currentName)) {
            return;
        }

        String relativePath = sourceRoot.relativize(currentPath).toString().replace("\\", "/");
        ZipEntry zipEntry = new ZipEntry(zipRootDirName + "/" + relativePath);
        zipOutputStream.putNextEntry(zipEntry);
        Files.copy(currentPath, zipOutputStream);
        zipOutputStream.closeEntry();
    }

    /**
     * 生成压缩包根目录名称
     */
    private String buildZipRootDirName(App app) {
        return sanitizeFileName(app.getId().toString()) + "-" + app.getCodeGenType();
    }

    /**
     * 清理非法文件名字符
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 下载流已开始输出后，不能再走统一 JSON 异常返回。
     */
    private void handleStreamException(HttpServletResponse response) {
        if (response.isCommitted()) {
            return;
        }
        try {
            response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "下载代码压缩包失败");
        } catch (IOException e) {
            log.error("failed to send download error response", e);
        }
    }
}
