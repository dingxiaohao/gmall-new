package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pmsinterface.feign.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface GmallPmsFeign extends GmallPmsApi {
}
