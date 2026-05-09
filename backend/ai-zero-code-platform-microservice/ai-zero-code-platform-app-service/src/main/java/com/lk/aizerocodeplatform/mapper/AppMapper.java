package com.lk.aizerocodeplatform.mapper;

import com.mybatisflex.core.BaseMapper;
import com.lk.aizerocodeplatform.model.entity.App;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 *  映射层。
 *
 * @author LK
 * @since 2026-04-24
 */
@Mapper
public interface AppMapper extends BaseMapper<App> {
    int clearDeployInfo(@Param("appId") Long appId);
}
