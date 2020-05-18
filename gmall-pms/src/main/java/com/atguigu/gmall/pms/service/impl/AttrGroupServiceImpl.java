package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.vo.GroupVo;
import com.baomidou.mybatisplus.core.injector.methods.SelectList;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private AttrDao attrDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override           //QueryCondition就是自己封装的一个分页参数对象,用来接口接收分页参数然后再把对象转为mp需要的对象
    public PageVo queryAttrGroupByCid(QueryCondition queryCondition, Long catelogId) {
         //Query对象就是把QueryCondition转为ipage分页对象的，这样mp才可以根据ipage对象执行分页方法
        //这个page方法就是mapper的selectpage方法需要ipage和wrapper参数,最后返回一个分页查询的结果数据ipage可以get获取一些分页的参数
        IPage<AttrGroupEntity> page = page(new Query<AttrGroupEntity>().getPage(queryCondition)
                , new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));


        //PageVo对象的构造器可以把查询出来的ipage对象转为pagevo
        //PageVo对象也是自己封装的一个对象作为分页返回结果的vo对象返回给前端展示的
        //里面有get获取到的分页参数和一个list分页的查询数据结果，返回前端展示，vo对象就是给前端展示的和前端交互用的
        return new PageVo(page);
    }

    @Override   //通过属性组id查询组数据，中间表和attr属性表数据
    public GroupVo queryGroupVoByGid(Long gid) {
        //创建返回给前端自己定义的vo对象继承实体类并拓展了属性，并组装vo对象
        GroupVo groupVo = new GroupVo();

        //通过属性组id查询属性组表数据，属性组的实体类id属性要通过注解指明这个属性就是主键id
        AttrGroupEntity groupEntity = this.getById(gid);
        BeanUtils.copyProperties(groupEntity,groupVo);

        //通过属性组id查询中间关系表关联数据获取到attrId
        QueryWrapper<AttrAttrgroupRelationEntity> relationQueryWrapper = new QueryWrapper<AttrAttrgroupRelationEntity>();
        relationQueryWrapper.eq("attr_group_id",gid);

        //查询中间表的数据,再从中间表获取到attrId
        List<AttrAttrgroupRelationEntity> relationList = attrAttrgroupRelationDao.selectList(relationQueryWrapper);
        groupVo.setRelations(relationList);

        //如果根据属性组gid查询中间表根据属性组id查询中间表数据
        //如果中间表为null,没有关联数据那么就直接return结束方法并返回这个vo对象
        if (CollectionUtils.isEmpty(relationList)){
            return groupVo;
        }

        //使用stream流获取到中间表集合中的每个attrId,并再保存到另一个集合中,stream流就是用来操作集合玩的
        //把集合中对象的属性再保存到一个集合中，自己遍历也可以做的
        List<Long> attrIds = relationList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        //根据一个attrId集合查询这个表所有attrId的数据记录
        List<AttrEntity> attrList = attrDao.selectBatchIds(attrIds);
        //组装vo对象
        groupVo.setAttrEntities(attrList);


        return groupVo;
    }

    @Override   //通过三级分类id查询属性组和属性数据,这个groupvo定义了这些属性还有一个多的中间表属性可以不给值
    public List<GroupVo> queryAttrAndGroupByCatId(Long catId) {
        //通过三级分类id查询属性组group数据，service的list方法根据条件查询列表
        List<AttrGroupEntity> groupList = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));

        //再通过查询出来的属性组id查询中间表,再通过中间表的属性id查询属性组对应的属性,之前写过这个方法直接调
        // 直接通过stream流调用并把上面这个集合元素转换为另一个集合元素,attrGroupEntity这个就是集合的每个元素遍历替换元素
        //箭头函数就是把后面的那个执行的结果返回值，替换集合的元素进行遍历替换。
        //1.stream流处理方式
        //List<GroupVo> groupVos = groupList.stream().map(attrGroupEntity -> this.queryGroupVoByGid(attrGroupEntity.getAttrGroupId())).collect(Collectors.toList());


        //不用stream流的处理方式,循环遍历查询出来的属性组数据
        //再通过属性组id调用之前写好的方法查询中间表,再通过中间表属性id查询属性表数据
        //再把数据拼接成vo对象返回进行展示,这些在之前的方法中写过了
        //明天再看一下这个queryGroupVoByGid方法的业务逻辑,三个表的关联关系,和三级分类关联

        List<GroupVo> groupVos = new ArrayList<>();
        groupList.forEach(attrGroupEntity -> {
            GroupVo groupVo = this.queryGroupVoByGid(attrGroupEntity.getAttrGroupId());
            groupVos.add(groupVo);
        });



        return groupVos;
    }

}