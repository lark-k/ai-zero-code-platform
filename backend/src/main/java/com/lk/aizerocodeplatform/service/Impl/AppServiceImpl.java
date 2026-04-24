package com.lk.aizerocodeplatform.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.mapper.AppMapper;
import com.lk.aizerocodeplatform.service.AppService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author LK
 * @since 2026-04-24
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
