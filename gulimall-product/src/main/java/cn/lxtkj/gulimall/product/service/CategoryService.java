package cn.lxtkj.gulimall.product.service;

import cn.lxtkj.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 00:20:21
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Categories();

    Map<String, List<Catelog2Vo>> getCatelogJson();
}

