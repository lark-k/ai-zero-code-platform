package com.lk.aizerocodeplatform.constant;


/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 15:32
 * 代码文件保存相关常量
 */
public interface CodeFileSaveConstant {
    /**
     * 保存代码文件的根路径
     */
    String ROOT_PATH = System.getProperty("user.dir") + "/temp/code_output";
    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/temp/code_deploy";

    /**
     * 应用部署域名(nginx代理)
     */
    String CODE_DEPLOY_HOST = "http://localhost";
}
