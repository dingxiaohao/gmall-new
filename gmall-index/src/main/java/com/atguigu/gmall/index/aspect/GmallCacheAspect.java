package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Aspect //定义切面
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    //分布式锁框架对象,获取锁释放锁
    @Autowired
    private RedissonClient redissonClient;

    //捕获该注解
    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable{
        //获取注解修饰的方法和方法上注解的参数,操作获取数据拼接key
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        Class returnType = method.getReturnType();  //注解修饰方法的返回类型
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);//获取到方法上的注解和参数
        String key = gmallCache.prefix() + Arrays.asList(joinPoint.getArgs());

        //1.查询缓存获取缓存的数据
        //2.判断从缓存获取的数据是否为空
        //不为空直接返回解析好的缓存查出来的数据即可
        Object cache = this.getCache(key, returnType);
        if (cache != null) {
            return cache;
        }

        //3.为空缓存没有的话执行查询数据库,这里就应该加分布式锁了
        //防止大量并发请求数据库,导致数据库宕机,只让一个请求过去查询之后把数据存到缓存
        //后续请求过来再查缓存就有数据了,不访问数据减少数据库压力,并且数据库的速度慢,缓存很快
        RLock lock = redissonClient.getLock("lock");
        lock.lock(gmallCache.timeout(), TimeUnit.MINUTES);

        //4.再查询缓存
        Object cache1 = this.getCache(key, returnType);
        if (cache1 != null) {
            //不为空缓存有数据直接把缓存数据返回
            //返回之前要释放锁防止死锁
            //至于为什么加完锁之后再锁里面还要再查一遍缓存
            //如果不查的话那被锁拦住的请求到这里还是不会查缓存前面查过了而是直接查数据库
            //没有作用不过是一个一个去访问数据库,不是一起访问数据库了
            //加了这个锁,第一个请求把数据放到缓存,后续的请求到这里再查缓存就有数据了
            //就会查缓存而不是查数据库了,查数据库很慢缓存速度很快,所以把数据放到缓存
            lock.unlock();
            return cache1;
        }

        //5.缓存为空没有数据,执行方法查询数据库
        //执行注解修饰的方法,最后的返回结果就是查询数据库的返回结果
        //把查询数据库的结果存到redis缓存,后续请求再查缓存就有了数据
        Object result = joinPoint.proceed(joinPoint.getArgs());

        //6.把这个数据存到缓存中
        //过期时间随机,防止缓存雪崩
        // 缓存穿透就是空数据也缓存
        // 缓存击穿就是加锁防止全都访问一次到数据库导致宕机
        redisTemplate.opsForValue().set(key,JSON.toJSONString(result),
                gmallCache.timeout()+ new Random().nextInt(gmallCache.bound()),TimeUnit.DAYS);
        //如果是空没进去if就在最后释放分布式锁,一定要释放要不然就死锁了
        lock.unlock();

        return result;
    }

    //判断缓存是否有数据,有的话直接返回没有就返回null
    private Object getCache(String key,Class returnType){
        //1.查询缓存获取缓存的数据
        String stringJson = redisTemplate.opsForValue().get(key);

        //2.判断从缓存获取的数据是否为空
        if (StringUtils.isNotBlank(stringJson)){
            //不为空直接返回解析好的缓存查出来的数据即可
            return JSON.parseObject(stringJson,returnType);
        }
        return null;
    }
}
