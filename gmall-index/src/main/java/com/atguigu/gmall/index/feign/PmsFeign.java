package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pmsinterface.feign.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface PmsFeign extends GmallPmsApi {
}
