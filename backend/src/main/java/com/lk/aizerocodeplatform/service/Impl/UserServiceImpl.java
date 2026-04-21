package com.lk.aizerocodeplatform.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.mapper.UserMapper;
import com.lk.aizerocodeplatform.service.UserService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author LK
 * @since 2026-04-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
