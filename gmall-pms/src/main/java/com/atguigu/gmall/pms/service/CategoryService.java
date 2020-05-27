package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;
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

    List<CategoryEntity> queryCategoryLevelOrParentCid(Integer level, Long pid);

    List<CategoryVO> querySubCategory(Long pid);
}

