package com.kzrdut.provider.impl;

import com.kzrdut.pojo.User;
import com.kzrdut.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.UUID;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Integer id) {
        log.info("客户端查询了 id 为 {} 的用户", id);
        Random random = new Random();
        User user = User.builder().userName(UUID.randomUUID().toString())
                .id(id)
                .gender(random.nextBoolean()).build();
        log.info("返回用户信息: {}", user);
        return user;
    }

    @Override
    public Integer insertUserById(User user) {
        log.info("插入数据成功, 用户名 = {}", user.getUserName());
        return user.getId();
    }
}
