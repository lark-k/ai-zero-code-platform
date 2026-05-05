package com.lk.aizerocodeplatform.langgraph4j.tools;

import com.lk.aizerocodeplatform.langgraph4j.model.ImageCategoryEnum;
import com.lk.aizerocodeplatform.langgraph4j.model.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/5 22:37
 */
@SpringBootTest
class UndrawIllustrationToolTest {
    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Test
    void searchIllustrations() {
        // 测试正常搜索插画
        List<ImageResource> illustrations = undrawIllustrationTool.searchIllustrations("happy");
        assertNotNull(illustrations);
        // 验证返回的插画资源
        ImageResource firstIllustration = illustrations.getFirst();
        assertEquals(ImageCategoryEnum.ILLUSTRATION, firstIllustration.getCategory());
        assertNotNull(firstIllustration.getDescription());
        assertNotNull(firstIllustration.getUrl());
        assertTrue(firstIllustration.getUrl().startsWith("http"));
        System.out.println("搜索到 " + illustrations.size() + " 张插画");
        illustrations.forEach(illustration ->
                System.out.println("插画: " + illustration.getDescription() + " - " + illustration.getUrl())
        );
    }
}