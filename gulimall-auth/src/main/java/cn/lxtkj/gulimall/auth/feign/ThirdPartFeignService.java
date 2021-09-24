package cn.lxtkj.gulimall.auth.feign;

import cn.lxtkj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description:
 * @Created: By IntelliJ IDEA.
 * @author: 雷小天
 * @createTime: 2021/9/24 23:06
 **/

@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {
    @GetMapping(value = "/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
