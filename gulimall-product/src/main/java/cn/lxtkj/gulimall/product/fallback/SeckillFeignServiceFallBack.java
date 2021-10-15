package cn.lxtkj.gulimall.product.fallback;

import cn.lxtkj.common.exception.BizCodeEnume;
import cn.lxtkj.common.utils.R;
import cn.lxtkj.gulimall.product.feign.SeckillFeignService;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 雷小天
 * @createTime: 2020-07-13 14:45
 **/

@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckilInfo(Long skuId) {
        return R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(),BizCodeEnume.TO_MANY_REQUEST.getMsg());
    }
}
