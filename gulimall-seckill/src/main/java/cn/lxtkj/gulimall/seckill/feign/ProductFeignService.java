package cn.lxtkj.gulimall.seckill.feign;

import cn.lxtkj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 雷小天
 * @createTime: 2021-10-14 21:12
 **/

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

}
