package cn.lxtkj.gulimall.ware.vo;

import lombok.Data;

@Data
public class PurchaseDoneItem {
    private Long itemId;
    private Integer status;
    private String reason;
}
