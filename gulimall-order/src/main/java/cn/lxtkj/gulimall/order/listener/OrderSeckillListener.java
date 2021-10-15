package cn.lxtkj.gulimall.order.listener;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import cn.lxtkj.common.to.mq.SeckillOrderTo;
import cn.lxtkj.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 雷小天
 * @createTime: 2021-07-11 16:37
 **/

@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(@Payload SeckillOrderTo orderTo, Channel channel, Message message) throws IOException {

        log.info("准备创建秒杀单的详细信息...,{}", JSONObject.toJSONString(orderTo));

        try {
            orderService.createSeckillOrder(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            log.error("e:",e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

}
