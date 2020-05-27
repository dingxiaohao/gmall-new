package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PmsListener {

    @Autowired
    private SearchService searchService;

    /**
     * 处理insert的消息
     *
     * @param spuId
     * @throws Exception
     */
    //Listener监听器,这个就是监听pms微服务的mq的,发送消息之后就会监听匹配注解,匹配上就会执行对应的方法
    //使用注解接收mq发的消息,对应的交换机名称,然后就是key符合什么可以匹配到发送的消息执行这个方法
    //从对应配置文件的交换机队列获取消息
    @RabbitListener(bindings = @QueueBinding(
            //这个队列,持久化
            value = @Queue(value = "GMALL.ITEM.CREATE.QUEUE", durable = "true"),
            exchange = @Exchange(
                    //交换机默认持久化
                    value = "GMALL.ITEM.EXCHANGE",  //获取这个交换机的队列的消息
                    ignoreDeclarationExceptions = "true",   //去重
                    type = ExchangeTypes.TOPIC),    //使用这个类型的方式
            key = {"item.insert"}))
    //如果mq发送的消息对应上这个routingKey,就会执行这个方法,里面再调用其他业务service方法
    public void listenCreate(Long spuId) throws Exception {
        if (spuId == null) {
            return;
        }
        // 执行创建索引的同步方法,pms新增后发送消息给这里,然后获取到spuId传给业务service执行
        this.searchService.createIndex(spuId);
    }
}
