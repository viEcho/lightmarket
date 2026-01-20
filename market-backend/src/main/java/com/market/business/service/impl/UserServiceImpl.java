package com.market.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.market.business.entity.User;
import com.market.business.mapper.UserMapper;
import com.market.business.service.UserService;
import org.springframework.stereotype.Service;

/**
 * User Service Implementation
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


}
