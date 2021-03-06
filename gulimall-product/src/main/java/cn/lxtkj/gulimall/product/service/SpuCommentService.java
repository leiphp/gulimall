package cn.lxtkj.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 00:20:21
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

