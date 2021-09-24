package cn.lxtkj.gulimall.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * @Description:
 * @Created: By IntelliJ IDEA.
 * @author: 雷小天
 * @createTime: 2021/9/24 14:52
 **/

@Controller
public class LoginController {

//    @GetMapping(value = "/login.html")
//    public String loginPage() {
//        return "login";
//    }


//
//    @GetMapping(value = "/login.html")
//    public String loginPage(HttpSession session) {
//
//        //从session先取出来用户的信息，判断用户是否已经登录过了
//        Object attribute = session.getAttribute(LOGIN_USER);
//        //如果用户没登录那就跳转到登录页面
//        if (attribute == null) {
//            return "login";
//        } else {
//            return "redirect:http://gulimall.com";
//        }
//
//    }

//    @GetMapping(value = "/reg.html")
//    public String regPage() {
//        return "reg";
//    }

}
