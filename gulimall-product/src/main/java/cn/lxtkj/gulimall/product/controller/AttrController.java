package cn.lxtkj.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.lxtkj.gulimall.product.entity.ProductAttrValueEntity;
import cn.lxtkj.gulimall.product.service.ProductAttrValueService;
import cn.lxtkj.gulimall.product.vo.AttrGroupRelationVo;
import cn.lxtkj.gulimall.product.vo.AttrRespVo;
import cn.lxtkj.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.lxtkj.gulimall.product.entity.AttrEntity;
import cn.lxtkj.gulimall.product.service.AttrService;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.common.utils.R;



/**
 * 商品属性
 *
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 01:10:55
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /** 功能：根据spuId信息查询出对应的规格参数信息
       API：https://easydoc.xyz/doc/75716633/ZUqEdvA4/GhhJhkg7
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R listForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entityList=productAttrValueService.baseAttrListForSpu(spuId);
        return  R.ok().put("data",entityList);
    };

    /**
     * 获取分类规格参数或获取分类销售属性
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String type){
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,type);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
		//AttrEntity attr = attrService.getById(attrId);
        AttrRespVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 功能：修改商品规格
     * API：https://easydoc.xyz/doc/75716633/ZUqEdvA4/GhnJ0L85
     * @param spuId
     * @param entities
     * @return
     */
    @PostMapping("/update/{spuId}")
    public R update(@PathVariable("spuId") Long spuId,@RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId,entities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
