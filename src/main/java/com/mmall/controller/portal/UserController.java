package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 用户管理相关
 *
 * @author Huanyu
 * @date 2018/4/16
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    /**
     * 该数据域字段名称需要与对应类的@Service注释参数名相同，
     */
    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录接口
     *
     * @param username 用户名
     * @param password 用户密码
     * @param session 用户当前会话
     * @return 包含一般提示信息的服务器返回数据
     * 注释@ResponseBody() 将返回值自动序列化json格式数据，
     * 在此之前需要在dispatcher-servlet.xml文件中添加supportedMediaTypes相关的配置
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        // service->mybatis->dao
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 用户登出接口：将session中currentUser对应的对象删除
     * @param session 用户当前会话
     * @return 包含一般提示信息的服务器返回数据
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册接口
     * @param user 用户对象
     * @return 包含一般提示信息的服务器返回数据
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    /**
     * 用户登录校验接口
     * @param str 用户名或者用户email所对应的值
     * @param type 用户名或者用户email
     * @return 包含一般提示信息的服务器返回数据
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    /**
     * 获取当前用户信息
     * @param session 用户当前会话
     * @return 包含当前用户信息的服务器返回数据
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
    }

    /**
     * 用户登录忘记密码，返回密码提示问题
     * @param username 用户名
     * @return 包含登录提示问题的服务器返回数据
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    /**
     * 获得密码提示问题之后，校验密码提示答案
     * @param username 用户名
     * @param question 所属用户的提示问题
     * @param answer 所属用户的提示答案
     * @return 包含通用唯一标识符信息的通用唯一标识符
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    /**
     * 忘记密码时通过用户名更新密码
     * @param username 用户名
     * @param passwordNew 用户新密码
     * @param forgetToken forgetCheckAnswer方法返回的通用唯一标识符，在用户忘记密码后用于将校验提示答案与重设密码关联，即确保为同一用户的操作，
     * @return 包含一般提示信息的服务器返回数据
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 登录状态重置密码
     * @param session 当前会话
     * @param passwordOld 用户旧密码
     * @param passwordNew 用户新密码
     * @return 包含一般提示信息的服务器返回数据
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }


    /**
     * 登录状态修改用户信息
     * @param session 当前会话
     * @param user 前端传来的包含新用户信息的用户对象
     * @return 包含一般提示信息的服务器返回数据
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        // 防治前端传输数据是被抓包，修改包内容提交过来的数据是伪造的，进而采用从当前会话中获得数据，这样可以保证数据不会被修改，
        user.setId(currentUser.getId());
        // user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            // 注意此处由service层放回的response.getData()的username属性为空，需要使用下句给其赋值
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 获取个人信息（未登录，需由前段强制登录），进而采用update_information方法更新用户信息
     * @param session 当前会话
     * @return 返回包括用户信息的服务器返回数据
     */
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(
                    ResponseCode.NEED_LOGIN.getCode(), "用户未登录，需要强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }

}
