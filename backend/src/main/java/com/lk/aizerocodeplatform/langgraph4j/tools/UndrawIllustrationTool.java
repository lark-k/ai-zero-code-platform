package com.lk.aizerocodeplatform.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lk.aizerocodeplatform.langgraph4j.model.ImageCategoryEnum;
import com.lk.aizerocodeplatform.langgraph4j.model.ImageResource;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/5 22:31
 * Undraw插画图收集工具
 */
@Slf4j
@Component
public class UndrawIllustrationTool {

    private static final String UNDRAW_SEARCH_PAGE_URL = "https://undraw.co/search/%s";
    private static final String NEXT_DATA_PREFIX = "<script id=\"__NEXT_DATA__\" type=\"application/json\">";
    private static final String NEXT_DATA_SUFFIX = "</script>";

    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        if (StrUtil.isBlank(query)) {
            return imageList;
        }

        int searchCount = 12;
        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String pageUrl = String.format(UNDRAW_SEARCH_PAGE_URL, encodedQuery);

        try (HttpResponse response = HttpRequest.get(pageUrl).timeout(10000).execute()) {
            if (!response.isOk()) {
                log.warn("unDraw search request failed, status={}, query={}", response.getStatus(), query);
                return imageList;
            }

            String html = response.body();
            String nextDataJson = StrUtil.subBetween(html, NEXT_DATA_PREFIX, NEXT_DATA_SUFFIX);
            if (StrUtil.isBlank(nextDataJson)) {
                log.warn("unDraw __NEXT_DATA__ not found, query={}", query);
                return imageList;
            }

            JSONObject root = JSONUtil.parseObj(nextDataJson);
            JSONObject props = root.getJSONObject("props");
            if (props == null) {
                return imageList;
            }

            JSONObject pageProps = props.getJSONObject("pageProps");
            if (pageProps == null) {
                return imageList;
            }

            JSONArray initialResults = pageProps.getJSONArray("initialResults");
            if (initialResults == null || initialResults.isEmpty()) {
                return imageList;
            }

            int actualCount = Math.min(searchCount, initialResults.size());
            for (int i = 0; i < actualCount; i++) {
                JSONObject illustration = initialResults.getJSONObject(i);
                String title = illustration.getStr("title", "插画");
                String media = illustration.getStr("media", "");

                if (StrUtil.isNotBlank(media)) {
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.ILLUSTRATION)
                            .description(title)
                            .url(media)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("搜索插画失败, query={}", query, e);
        }

        return imageList;
    }
}
