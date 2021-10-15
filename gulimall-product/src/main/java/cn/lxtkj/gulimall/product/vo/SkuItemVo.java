package cn.lxtkj.gulimall.product.vo;

import cn.lxtkj.gulimall.product.entity.SkuImagesEntity;
import cn.lxtkj.gulimall.product.entity.SkuInfoEntity;
import cn.lxtkj.gulimall.product.entity.SpuInfoDescEntity;
import cn.lxtkj.gulimall.product.service.SpuInfoDescService;
import lombok.Data;

import java.util.List;

/**
 * 商品详情信息，包含商品基本属性，图片信息，分类信息，描述信息，规格
 */
@Data
public class SkuItemVo {
    //1、sku基本信息的获取  pms_sku_info
    private SkuInfoEntity info;

    private boolean hasStock = true;

    //2、sku的图片信息    pms_sku_images
    private List<SkuImagesEntity> images;

    //3、获取spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttr;

    //4、获取spu的介绍
    private SpuInfoDescEntity desc;

    //5、获取spu的规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;

    //6、秒杀商品的优惠信息
    private SeckillSkuVo seckillSkuVo;


}
