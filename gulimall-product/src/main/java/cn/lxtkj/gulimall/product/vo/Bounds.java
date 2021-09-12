/**
  * Copyright 2020 bejson.com 
  */
package cn.lxtkj.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class Bounds {

    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}