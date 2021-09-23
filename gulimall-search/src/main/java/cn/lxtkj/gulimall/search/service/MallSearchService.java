package cn.lxtkj.gulimall.search.service;

import cn.lxtkj.gulimall.search.vo.SearchParam;
import cn.lxtkj.gulimall.search.vo.SearchReult;

public interface MallSearchService {
    /**
     *
     * @param param 检索的所有参数
     * @return  检索的结果
     */
    SearchReult search(SearchParam param);
}
