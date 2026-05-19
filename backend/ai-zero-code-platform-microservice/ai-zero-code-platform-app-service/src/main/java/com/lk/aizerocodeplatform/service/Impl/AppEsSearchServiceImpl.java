package com.lk.aizerocodeplatform.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.lk.aizerocodeplatform.constant.AppConstant;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.model.dto.app.QueryAppDTO;
import com.lk.aizerocodeplatform.service.AppEsSearchService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/19 20:17
 */
@Service
public class AppEsSearchServiceImpl implements AppEsSearchService {
    /**
     * 应用索引名称。
     * <p>
     * 必须和 Kibana 中创建的索引名、Canal Adapter 写入的索引名保持一致。
     */
    private static final String APP_INDEX = "app_index";

    /**
     * ES 7 High Level REST Client。
     * <p>
     * 由 ElasticsearchConfig 注册到 Spring 容器。
     */
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Page<Long> searchAppIds(QueryAppDTO queryAppDTO) {
        // 普通应用搜索：不强制限制 priority。
        return doSearch(queryAppDTO, false);
    }

    @Override
    public Page<Long> searchGoodAppIds(QueryAppDTO queryAppDTO) {
        // 精选应用搜索：强制 priority in (99, 999)。
        return doSearch(queryAppDTO, true);
    }

    /**
     * 执行 ES 搜索。
     *
     * @param queryAppDTO 前端传来的查询参数，复用你项目现有 DTO。
     * @param onlyGoodApp 是否只搜索精选应用
     * @return 只返回 app id 的分页结果
     */
    private Page<Long> doSearch(QueryAppDTO queryAppDTO, boolean onlyGoodApp) {
        int pageNum = Math.max(queryAppDTO.getPageNum(), 1);
        int pageSize = Math.max(queryAppDTO.getPageSize(), 10);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // ES 分页 from 从 0 开始。
        // 第 1 页 from=0，第 2 页 from=pageSize。
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        BoolQueryBuilder boolQuery = boolQuery();

        // 软删除过滤。
        // MyBatis-Flex 可能自动处理逻辑删除，但 ES 不会自动知道，所以这里必须显式过滤。
        boolQuery.filter(termQuery("isDelete", 0));

        // 精确 id 查询。
        if (queryAppDTO.getId() != null) {
            boolQuery.filter(termQuery("id", queryAppDTO.getId()));
        }

        // 用户过滤。
        // 普通用户查询“我的应用”时，AppServiceImpl 会先把当前登录用户 id 塞进 queryAppDTO.userId。
        if (queryAppDTO.getUserId() != null) {
            boolQuery.filter(termQuery("userId", queryAppDTO.getUserId()));
        }

        // 代码生成类型过滤，例如 html/vue_project 等。
        if (StrUtil.isNotBlank(queryAppDTO.getCodeGenType())) {
            boolQuery.filter(termQuery("codeGenType", queryAppDTO.getCodeGenType()));
        }

        // 部署 key 是精确值，不做全文检索。
        if (StrUtil.isNotBlank(queryAppDTO.getDeployKey())) {
            boolQuery.filter(termQuery("deployKey", queryAppDTO.getDeployKey()));
        }

        if (onlyGoodApp) {
            // 精选应用固定过滤 priority。
            // GOOD_APP_PRIORITY：普通精选
            // TOP_GOOD_APP_PRIORITY：置顶精选
            boolQuery.filter(termsQuery(
                    "priority",
                    List.of(AppConstant.GOOD_APP_PRIORITY, AppConstant.TOP_GOOD_APP_PRIORITY)
            ));
        } else if (queryAppDTO.getPriority() != null) {
            // 非精选搜索时，如果前端传了 priority，就按传入值过滤。
            boolQuery.filter(termQuery("priority", queryAppDTO.getPriority()));
        }

        if (StrUtil.isNotBlank(queryAppDTO.getAppName())) {
            // 这里复用现有 QueryAppDTO.appName 字段作为搜索关键词。
            // 当前前端首页和后台应用管理都是按 appName 搜索。
            //
            // multiMatch 同时搜 appName 和 initPrompt：
            // - appName 权重自然更高，因为通常更短、更精确；
            // - initPrompt 可以让用户搜到“作品集/电商/博客”等 prompt 语义。
            boolQuery.must(multiMatchQuery(queryAppDTO.getAppName(), "appName", "initPrompt")
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS));
        }

        sourceBuilder.query(boolQuery);

        String sortField = queryAppDTO.getSortField();
        String sortOrder = queryAppDTO.getSortOrder();

        if (StrUtil.isNotBlank(sortField)) {
            // 尊重前端传入的排序字段。
            // 注意：sortField 必须是 ES mapping 中存在且可排序的字段。
            // text 字段不能直接排序；appName 如需排序应使用 appName.keyword。
            sourceBuilder.sort(sortField, "ascend".equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        } else if (onlyGoodApp) {
            // 精选应用默认：置顶/精选优先，然后新应用靠前。
            sourceBuilder.sort("priority", SortOrder.DESC);
            sourceBuilder.sort("createTime", SortOrder.DESC);
        } else {
            // 普通应用默认：新应用靠前。
            sourceBuilder.sort("createTime", SortOrder.DESC);
        }

        SearchRequest searchRequest = new SearchRequest(APP_INDEX);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            List<Long> ids = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                // Canal 同步时保留了 id 字段，所以这里从 _source.id 读取。
                Object id = hit.getSourceAsMap().get("id");
                if (id != null) {
                    ids.add(Long.valueOf(id.toString()));
                }
            }

            Page<Long> page = new Page<>(pageNum, pageSize);
            page.setRecords(ids);
            page.setTotalRow(response.getHits().getTotalHits().value);
            page.setTotalPage((long) Math.ceil((double) page.getTotalRow() / pageSize));
            return page;
        } catch (IOException e) {
            // 这里抛业务异常。
            // 如果你想做降级，可以在 AppServiceImpl 捕获异常后走原 MySQL 查询。
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ES 搜索失败");
        }
    }
}
