package com.lk.aizerocodeplatform.core;

import cn.hutool.core.util.StrUtil;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 17:27
 */
@SpringBootTest
class AiCodeGenFacadeTest {
    @Resource
    private AiCodeGenFacade aiCodeGenFacade;

    @Test
    void generateCodeAndSave() {
        File file = aiCodeGenFacade.generateCodeAndSave("任务记录网站", CodeGenTypeEnum.MULTI_FILE,1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateCodeAndSaveStream() {
        List<String> completeCode = aiCodeGenFacade.generateCodeAndSaveStream("任务记录网站", CodeGenTypeEnum.MULTI_FILE,1L)
                .collectList()
                .block();
        Assertions.assertNotNull(completeCode);
        String result = StrUtil.join("", completeCode);
        Assertions.assertNotNull(result);
    }
}