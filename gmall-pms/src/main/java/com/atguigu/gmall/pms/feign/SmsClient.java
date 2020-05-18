package com.atguigu.gmall.pms.feign;

import feign.SmsApi;
import org.springframework.cloud.openfeign.FeignClient;

//要调用的微服务接口,引入了对应服务的feign接口工程,只需要这个接口继承写好的接口即可
//这样这个接口就有了该微服务全部的可调用接口,只需要继承不需要你来提供,接口的开发人提供
@FeignClient("sms-service")
public interface SmsClient extends SmsApi {

}
