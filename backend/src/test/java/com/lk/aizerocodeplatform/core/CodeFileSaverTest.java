package com.lk.aizerocodeplatform.core;

import com.lk.aizerocodeplatform.ai.AiCodeGenService;
import com.lk.aizerocodeplatform.ai.model.HtmlCodeResult;
import com.lk.aizerocodeplatform.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 16:05
 */
@SpringBootTest
class CodeFileSaverTest {
    @Resource
    private AiCodeGenService aiCodeGenService;

//    @Test
//    void saveHtml() {
//        HtmlCodeResult htmlCodeResult = aiCodeGenService.generateHtmlCode("生成一个用户登录页面，不超过50行代码");
//        CodeFileSaver.saveHtml(htmlCodeResult);
//    }
//
//    @Test
//    void saveMultiFile() {
//        MultiFileCodeResult multiFileCodeResult = aiCodeGenService.generateMultiFileCode("生成一个用户登录页面，背景为紫色，不超过100行代码");
//        CodeFileSaver.saveMultiFile(multiFileCodeResult);
//    }
}