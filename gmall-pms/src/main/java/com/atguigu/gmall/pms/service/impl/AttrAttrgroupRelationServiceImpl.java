package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pmsinterface.entity.AttrAttrgroupRelationEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public void deleteAttr(List<AttrAttrgroupRelationEntity> relationEntityList) {
        relationEntityList.forEach(relationEntity ->{
            //使用foreach和lambda表达式遍历集合并循环执行删除方法进行删除,箭头函数那个就是每次循环集合的元素
            //每次遍历循环都执行删除方法,删除条件就是传过来的对象中的数据
            this.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",relationEntity.getAttrId()).eq("attr_group_id",relationEntity.getAttrGroupId()));
        });
    }

}