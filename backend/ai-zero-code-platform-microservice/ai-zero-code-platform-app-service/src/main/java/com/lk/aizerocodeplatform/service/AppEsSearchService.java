package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.dto.app.QueryAppDTO;
import com.mybatisflex.core.paginate.Page;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/19 20:15
 * 应用 ES 搜索服务。
 * 设计原则：
 * 1. ES 只负责搜索和排序，返回应用 id 列表。
 * 2. MySQL 仍然是主库，拿到 id 后再回 MySQL 查询 App 实体。
 * 3. 这样不会破坏现有 AppVO 封装逻辑，也方便 ES 挂了时降级到 MySQL。
 */
public interface AppEsSearchService {

    /**
     * 搜索普通应用列表。
     * 用于：
     * /app/getAppVoListByPage
     * /app/admin/pageQuery
     */
    Page<Long> searchAppIds(QueryAppDTO queryAppDTO);

    /**
     * 搜索精选应用列表。
     * 用于：
     * /app/getAppVoListByPageForGood
     * 内部会固定过滤 priority in (GOOD_APP_PRIORITY, TOP_GOOD_APP_PRIORITY)。
     */
    Page<Long> searchGoodAppIds(QueryAppDTO queryAppDTO);
}
