package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.constant.CodeFileSaveConstant;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;

@Tag(name = "Static resource APIs")
@RestController
@RequestMapping("/static")
public class StaticResourceController {

    @jakarta.annotation.Resource
    private AppService appService;

    @GetMapping("/deploy/{deployKey}/**")
    @Operation(summary = "Serve deployed app")
    public ResponseEntity<Resource> serveDeployedResource(
            @PathVariable String deployKey,
            HttpServletRequest request) {
        ThrowUtils.throwIf(deployKey == null || deployKey.isBlank(), ErrorCode.PARAMS_ERROR);
        try {
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/deploy/" + deployKey).length());
            String baseDir = CodeFileSaveConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
            return serveResourceFromBaseDir(baseDir, resourcePath, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{appId}/**")
    @Operation(summary = "Serve generated app preview")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable Long appId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        String codeGenType = app.getCodeGenType();
        try {
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/" + appId).length());
            String baseDir;
            if ("vue_project".equals(codeGenType)) {
                baseDir = CodeFileSaveConstant.ROOT_PATH
                        + File.separator
                        + "vue_project_" + appId
                        + File.separator
                        + "dist";
            } else {
                baseDir = CodeFileSaveConstant.ROOT_PATH
                        + File.separator
                        + appId + "_" + codeGenType;
            }
            return serveResourceFromBaseDir(baseDir, resourcePath, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> serveResourceFromBaseDir(
            String baseDir,
            String resourcePath,
            HttpServletRequest request) throws Exception {
        if (resourcePath.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", request.getRequestURI() + "/");
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        }
        if (resourcePath.equals("/")) {
            resourcePath = "/index.html";
        }

        String relativePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        File baseDirectory = new File(baseDir).getCanonicalFile();
        File file = new File(baseDirectory, relativePath).getCanonicalFile();
        String basePath = baseDirectory.getPath();
        String filePath = file.getPath();
        if (!filePath.equals(basePath) && !filePath.startsWith(basePath + File.separator)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header("Content-Type", getContentTypeWithCharset(filePath))
                .body(resource);
    }

    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (filePath.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (filePath.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (filePath.endsWith(".png")) {
            return "image/png";
        }
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (filePath.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (filePath.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        return "application/octet-stream";
    }
}
