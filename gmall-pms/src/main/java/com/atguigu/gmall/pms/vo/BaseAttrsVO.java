package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pmsinterface.entity.ProductAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class BaseAttrsVO extends ProductAttrValueEntity {

    //属性赋值就是通过set方法赋值的,我重新set方法名字对上属性
    //然后在里面赋值的时候给attrValue赋值
    public void setValueSelected(List<Object> valueSelected){

        // 如果接受的集合为空，则不设置并退出set方法,不为空在底下代码设置
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        //join方法是把集合元素转为字符串,每个元素之间以什么分割
        //split是转为字符串数组
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }

}
