package cn.lxtkj.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description:
 * @Created: By IntelliJ IDEA.
 * @author: 雷小天
 * @createTime: 2021/9/24 23:30
 **/

public class NoStockException extends RuntimeException{
    @Getter
    @Setter
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id："+ skuId + "库存不足！");
    }

    public NoStockException(String msg) {
        super(msg);
    }
}
