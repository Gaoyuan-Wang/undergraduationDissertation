package top.gaoyuanwang.jetsonphotoforward.service;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "out")
public interface MqttGateway {
    /**
     * 定义重载方法，用于消息发送
     *
     * @param payload 负载
     */
    void sendToMqtt(String payload);

    /**
     * 指定topic进行消息发送
     *
     * @param topic   topic话题
     * @param payload 负载
     */
    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String payload);

    /**
     * 指定topic和qos进行消息发送
     *
     * @param topic   topic话题
     * @param qos     qos
     * @param payload 负载 （字符串类型）
     */
    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, String payload);

    /**
     * 指定topic和qos进行消息发送
     *
     * @param topic   topic话题
     * @param qos     qos
     * @param payload 负载 （字节数组类型）
     */
    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, byte[] payload);
}
