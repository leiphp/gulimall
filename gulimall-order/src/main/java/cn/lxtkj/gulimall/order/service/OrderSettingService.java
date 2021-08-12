package cn.lxtkj.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.gulimall.order.entity.OrderSettingEntity;

import java.util.Map;

/**
 * 订单配置信息
 *
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 23:12:34
 */
public interface OrderSettingService extends IService<OrderSettingEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

