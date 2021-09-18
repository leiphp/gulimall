package cn.lxtkj.gulimall.product.service.impl;

import cn.lxtkj.gulimall.product.service.CategoryBrandRelationService;
import cn.lxtkj.gulimall.product.vo.Catalog3List;
import cn.lxtkj.gulimall.product.vo.Catelog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

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
        //查出所有分类数据
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //找到所有一级分类
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
        //Todo 检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId,paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        log.info("查询一级分类数据");
        //找出一级分类
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        log.info("查询数据库");
        //一次性查询出所有的分类数据，减少对于数据库的访问次数，后面的数据操作并不是到数据库中查询，而是直接从这个集合中获取，
        // 由于分类信息的数据量并不大，所以这种方式是可行的
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        //1.查出所有一级分类
        List<CategoryEntity> level1Categories = getParentCid(categoryEntities,0L);

        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), level1 -> {
            //2. 根据一级分类的id查找到对应的二级分类
            List<CategoryEntity> level2Categories = getParentCid(categoryEntities,level1.getCatId());

            //3. 根据二级分类，查找到对应的三级分类
            List<Catelog2Vo> catelog2Vos =null;

            if(null != level2Categories || level2Categories.size() > 0){
                catelog2Vos = level2Categories.stream().map(level2 -> {
                    //得到对应的三级分类
                    List<CategoryEntity> level3Categories = getParentCid(categoryEntities,level2.getCatId());
                    //封装到Catalog3List
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

    /**
     * 在selectList中找到parentId等于传入的parentCid的所有分类数据
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

    //递归查找所有菜单的子菜单
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