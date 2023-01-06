package top.gaoyuanwang.jetsonphotoforward.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import javax.annotation.Resource;

@Configuration
@Slf4j
public class MqttInBoundConfiguration {

    @Resource
    private MqttConfiguration mqttProperties;

    //==================================== 消费消息==========================================//

    /**
     * 入站通道
     *
     * @return 消息通道对象 {@link MessageChannel}
     */
    @Bean("input")
    public MessageChannel mqttInputChannel() {
        //直连通道
        return new DirectChannel();
    }


    /**
     * 创建MqttPahoClientFactory 设置MQTT的broker的连接属性 如果使用ssl验证 也需要此处设置
     *
     * @return MQTT客户端工厂 {@link MqttPahoClientFactory}
     */
    @Bean
    public MqttPahoClientFactory inClientFactory() {
        //设置连接属性
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(mqttProperties.getUris());
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        options.setConnectionTimeout(mqttProperties.getTimeout());
        options.setKeepAliveInterval(mqttProperties.getKeepalive());
        // 接受离线消息  告诉代理客户端是否要建立持久会话   false为建立持久会话
        options.setCleanSession(false);
        //设置断开后重新连接
        options.setAutomaticReconnect(true);
        factory.setConnectionOptions(options);
        return factory;
    }


    /**
     * 入站
     *
     * @return 消息提供者 {@link MessageProducer}
     */
    @Bean
    public MessageProducer producer() {
        // Paho客户端消息驱动通道适配器，主要用来订阅主题  对inboundTopics主题进行监听
        //clientId 加后缀 不然会报retrying 不能重复
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttProperties.getClientId(), inClientFactory(), mqttProperties.getTopics());
        adapter.setCompletionTimeout(5000);
        // Paho消息转换器
        DefaultPahoMessageConverter defaultPahoMessageConverter = new DefaultPahoMessageConverter();
        // 按字节接收消息
        // defaultPahoMessageConverter.setPayloadAsBytes(true);
        adapter.setConverter(defaultPahoMessageConverter);
        // 设置QoS
        adapter.setQos(mqttProperties.getQos());
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    /**
     * 通过通道获取数据
     * ServiceActivator注解表明：当前方法用于处理MQTT消息，inputChannel参数指定了用于消费消息的channel。
     * tips:
     * 异步处理
     *
     * @return 消息处理 {@link MessageHandler}
     */
    @Bean
    @ServiceActivator(inputChannel = "input")
    public MessageHandler handler() {
        return message -> {
            log.info("收到的完整消息为--->{}", message);
            log.info("----------------------");
            log.info("message:" + message.getPayload());
            log.info("Id:" + message.getHeaders().getId());
            log.info("receivedQos:" + message.getHeaders().get(MqttHeaders.RECEIVED_QOS));
            String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
            log.info("topic:" + topic);
            log.info("----------------------");
        };
    }


}
