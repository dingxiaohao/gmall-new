package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pmsinterface.entity.ProductAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * spu属性值
 * 
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-08 16:12:11
 */
@Mapper
public interface ProductAttrValueDao extends BaseMapper<ProductAttrValueEntity> {

    List<ProductAttrValueEntity> querySearchAttrsBySpuId(Long spuId);
}
