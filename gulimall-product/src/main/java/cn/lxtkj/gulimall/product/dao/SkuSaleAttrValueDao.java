package cn.lxtkj.gulimall.product.dao;

import cn.lxtkj.gulimall.product.entity.SkuSaleAttrValueEntity;
import cn.lxtkj.gulimall.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 00:20:21
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<String> getSkuSaleAttrValuesAsStringList(@Param("spuId") Long skuId);

    List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId);
}
