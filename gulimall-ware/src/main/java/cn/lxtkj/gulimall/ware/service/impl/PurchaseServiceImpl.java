package cn.lxtkj.gulimall.ware.service.impl;

import cn.lxtkj.common.constant.WareConstant;
import cn.lxtkj.gulimall.ware.entity.PurchaseDetailEntity;
import cn.lxtkj.gulimall.ware.service.PurchaseDetailService;
import cn.lxtkj.gulimall.ware.service.WareSkuService;
import cn.lxtkj.gulimall.ware.vo.MergeVo;
import cn.lxtkj.gulimall.ware.vo.PurchaseDoneItem;
import cn.lxtkj.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.common.utils.Query;

import cn.lxtkj.gulimall.ware.dao.PurchaseDao;
import cn.lxtkj.gulimall.ware.entity.PurchaseEntity;
import cn.lxtkj.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        //??????purchaseId??????????????????PurchaseEntity???????????????????????????
        //?????????wms_purchase
        if(purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId=purchaseEntity.getId();

        }else{
            //????????????????????????????????????????????????????????????
            PurchaseEntity purchaseEntity = this.baseMapper.selectById(mergeVo.getPurchaseId());
            boolean flage=(purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()) ||
                    (purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
            if(!flage){
                return;
            }

        }
        //??????wms_purchase_detail
        Long finalPurchaseId =purchaseId;
        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
//1. ????????????????????????????????????????????????
        List<PurchaseEntity> purchaseEntities = ids.stream().map(item -> {
            PurchaseEntity byId = this.getById(item);
            return byId;
        }).filter(item -> {
            //???????????????????????????????????????
            boolean flage=(item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()) ||
                    (item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
            return flage;
        }).map(item -> {
            //???????????????????????????????????????????????????????????????????????????
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(item.getId());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            purchaseEntity.setUpdateTime(new Date());
            //item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            //item.setUpdateTime(new Date());
            item = null;//GC
            return  purchaseEntity;
        }).collect(Collectors.toList());


        //2. ????????????????????????
        this.updateBatchById(purchaseEntities);


        //3. ????????????????????????
        purchaseEntities.stream().forEach(item ->{
            List<PurchaseDetailEntity> entities=purchaseDetailService.listDetailByPurchaseId(item.getId());
            //???????????????????????????
            List<PurchaseDetailEntity> collect = entities.stream().map(detailEntity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(detailEntity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                detailEntity=null;//GC
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);
        });
    }
    /**
     * ?????????????????????????????????????????????????????????????????????
     * ???1???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ???2???????????????????????????????????????????????????????????????????????????????????????????????????
     * @param doneVo
     */
    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        //1. ????????????????????????
        List<PurchaseDoneItem> finishVoItems = doneVo.getItems();

        AtomicBoolean flag= new AtomicBoolean(true);
        //????????????????????????
        List<PurchaseDetailEntity> detailEntities = finishVoItems.stream().map(item -> {

            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item.getItemId());
            //??????????????????
            boolean failFlag = item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASEERROR.getCode();
            if (failFlag) {
                detailEntity.setStatus(item.getStatus());
                flag.set(false);
            } else {
                //????????????

                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //2. ??????????????????????????????
                //?????????????????????ID
                Long itemId = item.getItemId();
                //??????sku_id???sku_num???ware_id
                PurchaseDetailEntity byId = purchaseDetailService.getById(itemId);

                wareSkuService.addStock(byId.getSkuId(),byId.getWareId(),byId.getSkuNum());

            }
            item = null;//GC
            return detailEntity;
        }).collect(Collectors.toList());


        purchaseDetailService.updateBatchById(detailEntities);

        //3. ????????????????????????
        Long id = doneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag.get() ? WareConstant.PurchaseStatusEnum.FINISH.getCode():
                WareConstant.PurchaseStatusEnum.HASEERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());

        this.updateById(purchaseEntity);
    }

}