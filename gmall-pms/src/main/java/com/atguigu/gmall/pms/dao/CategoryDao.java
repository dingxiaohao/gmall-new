package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-08 16:12:11
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
