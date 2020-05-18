package com.atguigu.gmall.pms.entity.vo;

import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class GroupVo extends AttrGroupEntity {
    //这里的属性名要和前端传参数的名字一样,这样才能把前端传的参数值给赋到属性上。
    //并且返回的名字就是这个,前端收到返回的结果也是这个名字
    private List<AttrAttrgroupRelationEntity> relations;    //中间表属性
    private List<AttrEntity> attrEntities;      //属性组对应的属性表
}
