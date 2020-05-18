package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import dto.SaleDTO;


/**
 * 商品sku积分设置
 *
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-12 16:37:09
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSaleInfo(SaleDTO saleDTO);
}

