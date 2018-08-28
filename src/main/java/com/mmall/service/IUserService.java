package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

/**
 * 用户业务层接口
 *
 * @author Huanyu
 * @date 2018/4/16
 */
public interface IUserService {
    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    ServerResponse<User> login(String username, String password);

    /**
     * 用户注册
     * @param user
     * @return
     */
    ServerResponse<String> register(User user);

    /**
     * 校验用户是否存在(通过用户名和邮箱检查，主要用于实时检查)
     * @param str
     * @param type
     * @return
     */
    ServerResponse<String> checkValid(String str, String type);

    /**
     * 用户忘记登录密码时，获取提示问题
     * @param username
     * @return
     */
    ServerResponse<String> selectQuestion(String username);


    /**
     * 检验提示问题的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    ServerResponse<String> checkAnswer(String username, String question, String answer);


    /**
     * 忘记密码时通过用户名更新密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    /**
     * 登录状态重置密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user);

    /**
     * 登录状态下更新用户信息
     * @param user
     * @return
     */
    ServerResponse<User> updateInformation(User user);

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    ServerResponse<User> getInformation(Integer userId);

    ServerResponse checkAdminRole(User user);
}
