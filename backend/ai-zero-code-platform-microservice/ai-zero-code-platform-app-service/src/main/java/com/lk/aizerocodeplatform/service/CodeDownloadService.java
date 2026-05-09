package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/3 16:26
 * 代码下载服务
 */
public interface CodeDownloadService {
    /**
     * 下载应用源码压缩包
     * 会自动排除 dist、node_modules 等目录
     *
     * @param appId       应用 id
     * @param userLoginVO 当前登录用户
     * @param response    响应对象
     */
    void downloadAppCodeZip(Long appId, UserLoginVO userLoginVO, HttpServletResponse response);
}
