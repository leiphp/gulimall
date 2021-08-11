package cn.lxtkj.gulimall.product.dao;

import cn.lxtkj.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 00:20:21
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
