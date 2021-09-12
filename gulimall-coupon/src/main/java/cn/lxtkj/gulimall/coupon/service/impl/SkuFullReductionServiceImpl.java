package cn.lxtkj.gulimall.coupon.service.impl;

import cn.lxtkj.common.to.MemberPrice;
import cn.lxtkj.common.to.SkuReductionTo;
import cn.lxtkj.gulimall.coupon.entity.MemberPriceEntity;
import cn.lxtkj.gulimall.coupon.entity.SkuLadderEntity;
import cn.lxtkj.gulimall.coupon.service.MemberPriceService;
import cn.lxtkj.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.common.utils.Query;

import cn.lxtkj.gulimall.coupon.dao.SkuFullReductionDao;
import cn.lxtkj.gulimall.coupon.entity.SkuFullReductionEntity;
import cn.lxtkj.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //SKU的优惠，满减等信息；sms_sku_ladder；sms_sku_full_reduction；sms_member_price
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        //skuLadderEntity.setPrice();
        skuLadderEntity.setAddOther(skuReductionTo.getFullCount());

        if(skuLadderEntity.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }


        //2.sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
        if(skuFullReductionEntity.getReducePrice().compareTo(new BigDecimal("0")) == 1){
            this.save(skuFullReductionEntity);
        }



        //3.sms_member_price
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuLadderEntity.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(item -> {
            return (item.getMemberPrice().compareTo(new BigDecimal("0")) == 1);
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);

    }

}