package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.vo.AttrVO;
import com.atguigu.gmall.pmsinterface.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pmsinterface.entity.AttrEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryAttrByTypeOrCid(Integer type, Long catelogId, QueryCondition queryCondition) {
        //这个泛型就是返回的结果实体类是什么,list保存什么对象
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();

        //如果等于null就不进来拼接条件,就没有这个条件就会全查出来,不等于null就是有条件拼接条件
        if (type != null){
            wrapper.eq("attr_type",type);
        }
        //最后拼接好的条件对象,就和xml里写if是一样的。
        wrapper.eq("catelog_id",catelogId);

        //这个page方法就是mapper的selectpage方法
        IPage<AttrEntity> page = page(new Query<AttrEntity>().getPage(queryCondition), wrapper);

        //返回pageVo构造器会自动把查询结果ipage对象转为pagevo要展示给前端的分页vo对象
        return new PageVo(page);
    }

    @Override
    public void saveAttr(AttrVO attrVo) {
        //把这个对象保存到attr表中,这个vo对象继承实体类里面就有父类的所有属性了，所以可以传子类
        //如果有自己的拓展属性也是无所谓的，有其他多余属性但是数据库没有对应的字段是不会赋值的
        //这里有主键策略生成主键id,执行这个方法之后获取这个对象的id就可以获取到生成的主键id了
        this.save(attrVo);

        Long attrId = attrVo.getAttrId();   //获取到主键策略生成的id
        Long attrGroupId = attrVo.getAttrGroupId(); //这个id就是继承实体类之后拓展的一个属性,组id
        //创建中间表对象并保存中间表数据
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        //设置中间表的数据和其他表关联关系的数据保存到数据库
        relationEntity.setAttrId(attrId);
        relationEntity.setAttrGroupId(attrGroupId);
        //注入其他表的dao执行对其他表的操作
        attrAttrgroupRelationDao.insert(relationEntity);
    }

}