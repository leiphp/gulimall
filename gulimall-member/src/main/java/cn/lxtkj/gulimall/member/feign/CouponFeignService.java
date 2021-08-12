package cn.lxtkj.gulimall.member.feign;

import cn.lxtkj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 这是一个声明式的远程调用
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @RequestMapping("/conpon/coupon/member/list")
    public R membercoupons();
}
