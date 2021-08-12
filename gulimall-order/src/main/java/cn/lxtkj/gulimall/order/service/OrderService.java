package cn.lxtkj.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 23:12:34
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

