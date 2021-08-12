package cn.lxtkj.gulimall.coupon.dao;

import cn.lxtkj.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 22:32:32
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
