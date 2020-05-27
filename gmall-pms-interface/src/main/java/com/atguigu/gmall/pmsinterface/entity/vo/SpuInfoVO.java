package com.atguigu.gmall.pmsinterface.entity.vo;

import com.atguigu.gmall.pmsinterface.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

@Data
public class SpuInfoVO extends SpuInfoEntity {

    //图片信息
    private List<String> spuImages;

    //基本属性信息
    private List<BaseAttrsVO> baseAttrs;

    //sku信息
    private List<SkuInfoVO> skus;

}
