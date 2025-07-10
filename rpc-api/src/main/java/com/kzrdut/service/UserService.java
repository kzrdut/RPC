package com.kzrdut.service;

import com.kzrdut.annotation.Retryable;
import com.kzrdut.pojo.User;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public interface UserService {
    @Retryable
    User getUserById(Integer id);

    @Retryable
    Integer insertUserById(User user);
}
