package cn.lxtkj.gulimall.order.dao;

import cn.lxtkj.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 23:12:34
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
