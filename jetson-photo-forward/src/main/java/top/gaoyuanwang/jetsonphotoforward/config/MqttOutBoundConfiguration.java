package top.gaoyuanwang.jetsonphotoforward.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import javax.annotation.Resource;

@Configuration
public class MqttOutBoundConfiguration {
    @Resource
    private MqttConfiguration mqttProperties;

    //==================================== 发送消息==========================================//

    /**
     * 出站通道
     *
     * @return 消息通道对象 {@link MessageChannel}
     */
    @Bean("out")
    public MessageChannel mqttOutBoundChannel() {
        //直连通道
        return new DirectChannel();
    }


    /**
     * 创建MqttPahoClientFactory 设置MQTT的broker的连接属性 如果使用ssl验证 也需要此处设置
     *
     * @return MQTT客户端工厂 {@link MqttPahoClientFactory}
     */
    @Bean
    public MqttPahoClientFactory outClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        String[] uris = mqttProperties.getUris();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(uris);
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
     * 出站
     *
     * @return 消息处理 {@link MessageHandler}
     */
    @Bean
    @ServiceActivator(inputChannel = "out")
    public MessageHandler mqttOutbound() {
        //发送消息和消费消息Channel可以使用相同MqttPahoClientFactory
        //clientId 加后缀 不然会报retrying 不能重复
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(mqttProperties.getClientId(), outClientFactory());
        //如果设置成true，即异步，发送消息时将不会阻塞。
        messageHandler.setAsync(true);
        //设置默认QoS
        messageHandler.setDefaultQos(mqttProperties.getQos());
        // Paho消息转换器
        DefaultPahoMessageConverter defaultPahoMessageConverter = new DefaultPahoMessageConverter();
        //发送默认按字节类型发送消息
//        defaultPahoMessageConverter.setPayloadAsBytes(true);
        messageHandler.setConverter(defaultPahoMessageConverter);
        return messageHandler;
    }
}
