package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_user
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_user
     *
     * @mbggenerated
     */
    int insert(User record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_user
     *
     * @mbggenerated
     */
    User selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_user
     *
     * @mbggenerated
     */
    List<User> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_user
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(User record);

    /**
     * 通过用户名检查用户是否已经存在，登录和注册均会用到
     * @param username
     * @return
     */
    int checkUsername(String username);

    /**
     * 检查登录用户的用户名和密码是否正确匹配
     * @param username
     * @param password
     * @return
     */
    User selectLogin(@Param("username") String username, @Param("password") String password);

    /**
     * 用户注册时检查邮箱是否已注册
     * @param email
     * @return
     */
    int checkEmail(String email);

    /**
     * 用户通过username找回提示问题，进而找回密码
     * @param username
     * @return
     */
    String selectQuestionByUsername(String username);

    /**
     * 校验提示问题的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    int checkAnswer(@Param("username") String username,
                    @Param("question") String question,
                    @Param("answer") String answer);

    /**
     * 忘记密码时通过用户名更新密码
     * @param username
     * @param passwordNew
     * @return
     */
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordNew") String passwordNew);

    /**
     * 登录状态修改密码时，对原密码进行校验
     * @param password
     * @param userId
     * @return
     */
    int checkPassword(@Param("password") String password, @Param("userId") Integer userId);

    /**
     * 选择性地更新，即就是当传入对象的某属性不为空时，才更新数据库中对应记录该属性值
     * @param user
     * @return
     */
    int updateByPrimaryKeySelective(User user);

    /**
     * 检验当前用户的email新值是否与其他用户的email相同
     * @param email
     * @param userId
     * @return
     */
    int checkEmailByUserId(@Param("email") String email, @Param("userId") Integer userId);
}