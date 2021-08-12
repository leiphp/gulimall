package cn.lxtkj.gulimall.member.dao;

import cn.lxtkj.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 22:49:16
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
