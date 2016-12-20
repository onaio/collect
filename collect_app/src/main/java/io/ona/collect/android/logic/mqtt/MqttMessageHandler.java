package io.ona.collect.android.logic.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 20/12/2016
 */
public interface MqttMessageHandler {
    String KEY_MESSAGE_ID = "message_id";
    String KEY_MESSAGE_TYPE = "message_type";
    String KEY_TIME = "time";
    String KEY_PAYLOAD = "payload";
    
    boolean canHandle(String topic, MqttMessage message);
    boolean handle(String topic, MqttMessage message);
}
