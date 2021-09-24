package cn.lxtkj.gulimall.auth.controller;

import cn.lxtkj.common.constant.AuthServerConstant;
import cn.lxtkj.common.exception.BizCodeEnume;
import cn.lxtkj.common.utils.R;
import cn.lxtkj.gulimall.auth.feign.ThirdPartFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Created: By IntelliJ IDEA.
 * @author: 雷小天
 * @createTime: 2021/9/24 14:52
 **/

@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ResponseBody
    @GetMapping(value = "/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {

        //1、接口防刷
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            //活动存入redis的时间，用当前时间减去存入redis的时间，判断用户手机号是否在60s内发送验证码
            long currentTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - currentTime < 60000) {
                //60s内不能再发
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        //2、验证码的再次效验 redis.存key-phone,value-code
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String codeNum = String.valueOf(code);
        String redisStorage = codeNum + "_" + System.currentTimeMillis();

        //存入redis，防止同一个手机号在60秒内再次发送验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,
                redisStorage,10, TimeUnit.MINUTES);

        thirdPartFeignService.sendCode(phone, codeNum);

        return R.ok();
    }

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
