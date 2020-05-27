package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.index.feign.PmsFeign;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import com.atguigu.gmall.pmsinterface.entity.vo.CategoryVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    private final String KEY_PREFIX = "index:category:";

    @Autowired
    private PmsFeign pmsFeign;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<CategoryEntity> queryLevel1() {
        Resp<List<CategoryEntity>> listResp = pmsFeign.queryCategoryLevelOrParentCid(1, null);
        List<CategoryEntity> categoryEntityList = listResp.getData();
        return categoryEntityList;
    }

//    @Override
//    public List<CategoryVO> querySubByPid(Long pid) {
//        //先从缓存查
//        String stringJson = stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        //缓存有的话解析数据直接返回
//        if (StringUtils.isNotBlank(stringJson)){
//            //parse反序列化解析json数据转为你想要的类型
//            List<CategoryVO> categoryVOS = JSON.parseArray(stringJson, CategoryVO.class);
//            //直接返回命中缓存的数据即可
//            return categoryVOS;
//        }
//        //缓存没有的话就走数据库查询并把数据存到缓存,远程调用
//        Resp<List<CategoryVO>> listResp = pmsFeign.querySubCategory(pid);
//        //数据库查询出来的数据,不判断非空直接存可以防止缓存穿透,空数据也会缓存
//        //如果是空数据可以设置几分钟的时间
//        List<CategoryVO> categoryVOList = listResp.getData();
//        if (CollectionUtils.isEmpty(categoryVOList)){
//            //如果是空数据也缓存,三分钟的时间防止缓存穿透恶意攻击
//            stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryVOList),3, TimeUnit.MINUTES);
//        }
//        //不是空数据就缓存随机数天数,防止缓存雪崩
//        // 这样缓存中没有就存入redis,再下次访问的时候redis中就有缓存了,可以直接从缓存拿数据走缓存
//        //减少对数据库的压力,并且效率高很快
//        //缓存的数据就是经常频繁访问,并且不咋变化的数据
//        stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryVOList),new Random().nextInt(10), TimeUnit.DAYS);
//
//        /*
//            缓存击穿就是一个key到期,大量请求访问
//                解决方案:
//                    1.设置热点数据永不过期
//
//                    2.加锁排他,处理高并发,请求串行执行,一个请求过来执行完成后就会把数据缓存到redis
//                        后续请求也就会访问缓存了不会到数据库
//         */
//        return categoryVOList;
//    }

    @Override
    @GmallCache
    public List<CategoryVO> querySubByPid(Long pid) {
        Resp<List<CategoryVO>> listResp = pmsFeign.querySubCategory(pid);
        List<CategoryVO> categoryVOList = listResp.getData();

        /*
            缓存击穿就是一个key到期,大量请求访问
                解决方案:
                    1.设置热点数据永不过期

                    2.加分布式锁排他aop实现注解,处理高并发,请求串行执行,一个请求过来执行完成后就会把数据缓存到redis
                        后续请求也就会访问缓存了不会到数据库
         */
        return categoryVOList;
    }
}
