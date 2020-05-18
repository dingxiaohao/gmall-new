package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品属性
 *
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-08 16:12:11
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryAttrByTypeOrCid(Integer type, Long catelogId, QueryCondition queryCondition);

    void saveAttr(AttrVO attrVO);
}

