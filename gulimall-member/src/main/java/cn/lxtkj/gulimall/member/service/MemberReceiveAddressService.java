package cn.lxtkj.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.lxtkj.common.utils.PageUtils;
import cn.lxtkj.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.Map;

/**
 * 会员收货地址
 *
 * @author leixiaotian
 * @email 1124378213@qq.com
 * @date 2021-08-12 22:49:16
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

