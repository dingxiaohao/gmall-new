package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;
import java.util.Map;


/**
 * 商品三级分类
 *
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-08 16:12:11
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    List<CategoryEntity> queryCategoryLevelOrParentCid(Map<String, Object> map);
}

