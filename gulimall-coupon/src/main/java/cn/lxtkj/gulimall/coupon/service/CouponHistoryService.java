package cn.lxtkj.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.gulimall.coupon.entity.CouponHistoryEntity;

import java.util.Map;

/**
 * 优惠券领取历史记录
 *
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 22:32:32
 */
public interface CouponHistoryService extends IService<CouponHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

