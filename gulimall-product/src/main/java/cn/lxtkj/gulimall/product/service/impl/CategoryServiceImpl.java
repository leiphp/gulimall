package cn.lxtkj.gulimall.product.service.impl;

import cn.lxtkj.gulimall.product.service.CategoryBrandRelationService;
import cn.lxtkj.gulimall.product.vo.Catalog3List;
import cn.lxtkj.gulimall.product.vo.Catelog2Vo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.common.utils.Query;

import cn.lxtkj.gulimall.product.dao.CategoryDao;
import cn.lxtkj.gulimall.product.entity.CategoryEntity;
import cn.lxtkj.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //????????????????????????
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //????????????????????????
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid()==0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //Todo ?????????????????????????????????????????????????????????
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId,paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * ???1?????????????????????
     * ???2?????????????????????????????????
     * ???3??????????????????????????????????????????????????????redis??????????????????????????????
     * @param category
     */
//    @Caching(evict={
//       @CacheEvict(value = {"category"},key = "'getLevel1Categories'"),
//       @CacheEvict(value = {"category"},key = "'getCatelogJson'")
//    })
    @CacheEvict(value = {"category"},allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Cacheable(value = {"category"},key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        log.info("????????????????????????");
        //??????????????????
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return categoryEntities;
    }

    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        System.out.println("??????????????????......");
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ????????????????????????????????????????????????????????????????????????
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        //1.????????????????????????
        List<CategoryEntity> level1Categories = getParentCid(categoryEntities, 0L);

        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), level1 -> {
            //2. ?????????????????????id??????????????????????????????
            List<CategoryEntity> level2Categories = getParentCid(categoryEntities, level1.getCatId());

            //3. ???????????????????????????????????????????????????
            List<Catelog2Vo> catelog2Vos = null;

            if (null != level2Categories || level2Categories.size() > 0) {
                catelog2Vos = level2Categories.stream().map(level2 -> {
                    //???????????????????????????
                    List<CategoryEntity> level3Categories = getParentCid(categoryEntities, level2.getCatId());
                    //?????????Catalog3List
                    List<Catalog3List> catalog3Lists = null;
                    if (null != level3Categories) {
                        catalog3Lists = level3Categories.stream().map(level3 -> {
                            Catalog3List catalog3List = new Catalog3List(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                            return catalog3List;
                        }).collect(Collectors.toList());
                    }
                    return new Catelog2Vo(level1.getCatId().toString(), catalog3Lists, level2.getCatId().toString(), level2.getName());
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));
        return parent_cid;
    }

//    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson2() {
        //1.????????????????????????????????????json?????????
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(StringUtils.isEmpty(catalogJSON)){
            System.out.println("?????????????????????????????????");
            //2.?????????????????????????????????
            Map<String,List<Catelog2Vo>> catalogJsonFromDb = getCatelogJsonFromDbWithRedisLock();
            return catalogJsonFromDb;
        }
        System.out.println("???????????????????????????");
        //????????????
        Map<String,List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedissonLock() {
        //1.??????????????????????????????????????????
        RLock lock = redisson.getLock("CatelogJson-lock");
        //??????????????????????????????????????????????????????????????????????????????????????????
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            //?????????????????????????????????
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLock() {
        String uuid= UUID.randomUUID().toString();
        //1.?????????????????????redis??????
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock",uuid, 300, TimeUnit.SECONDS);
        if(lock){
            System.out.println("????????????????????????...");
            //???????????????????????????
            Map<String, List<Catelog2Vo>> dataFromDb;
            try{
               dataFromDb = getDataFromDb();
            }finally {
                //????????????????????????
                String script="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript(script,Long.class),Arrays.asList("lock"),uuid);
            }
            return dataFromDb;
        }else{
            System.out.println("????????????????????????...????????????");
            //????????????????????????synchronized()
            //??????200ms
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }
            return getCatelogJsonFromDbWithRedisLock();//???????????????
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //???????????????????????????????????????????????????
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            //????????????null????????????
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("??????????????????......");
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ????????????????????????????????????????????????????????????????????????
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        //1.????????????????????????
        List<CategoryEntity> level1Categories = getParentCid(categoryEntities, 0L);

        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), level1 -> {
            //2. ?????????????????????id??????????????????????????????
            List<CategoryEntity> level2Categories = getParentCid(categoryEntities, level1.getCatId());

            //3. ???????????????????????????????????????????????????
            List<Catelog2Vo> catelog2Vos = null;

            if (null != level2Categories || level2Categories.size() > 0) {
                catelog2Vos = level2Categories.stream().map(level2 -> {
                    //???????????????????????????
                    List<CategoryEntity> level3Categories = getParentCid(categoryEntities, level2.getCatId());
                    //?????????Catalog3List
                    List<Catalog3List> catalog3Lists = null;
                    if (null != level3Categories) {
                        catalog3Lists = level3Categories.stream().map(level3 -> {
                            Catalog3List catalog3List = new Catalog3List(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                            return catalog3List;
                        }).collect(Collectors.toList());
                    }
                    return new Catelog2Vo(level1.getCatId().toString(), catalog3Lists, level2.getCatId().toString(), level2.getName());
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));
        //3.????????????????????????????????????????????????json???????????????
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    //???????????????
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithLocalLock() {
        log.info("???????????????");
        //todo ?????????????????????????????????????????????????????????
        synchronized (this){
            //???????????????????????????????????????????????????
            return getDataFromDb();
        }
    }

    /**
     * ???selectList?????????parentId???????????????parentCid?????????????????????
     * @param selectList
     * @param parentCid
     * @return
     */
    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return  collect;
    }


    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }

    //????????????????????????????????????
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}