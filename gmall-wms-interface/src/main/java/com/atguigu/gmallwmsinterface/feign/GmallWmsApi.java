package com.atguigu.gmallwmsinterface.feign;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmallwmsinterface.entity.WareSkuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallWmsApi {

    @GetMapping("wms/waresku/{skuId}")
    public Resp<List<WareSkuEntity>> queryListBySkuId(@PathVariable("skuId") Long skuId);
}
