package club.makeable.jetreactscore;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by yuhxie on 10/29/16.
 */

public class MQTTClient {
    private final static String TAG = MQTTClient.class.getSimpleName();

    String mTopic       = "JetReact";
    String mBrokerURL   = "tcp://m13.cloudmqtt.com:18513";
    String mUsername    = "pzrqufih";
    String mPassowrd    = "Nrj1sf3Edw9E";
    String mClientId    = "android-app";
    MqttConnectOptions mOptions = new MqttConnectOptions();
    MqttClient mqttClient    = null;
    MemoryPersistence mPersistence = new MemoryPersistence();

    public void setup(MqttCallback callback) {
        try {
            mqttClient = new MqttClient(mBrokerURL, mClientId, mPersistence);
            mqttClient.setCallback(callback);

            mOptions = new MqttConnectOptions();

            // mOptions.setCleanSession(true);
            mOptions.setUserName(mUsername);
            mOptions.setPassword(mPassowrd.toCharArray());

            mqttClient.connect(mOptions);
            mqttClient.subscribe(mTopic);
        }
        catch(MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "Code=" + e.getReasonCode());
            Log.e(TAG, "Message=" + e.getMessage());
        }
    }

    public void reconnect() {
        try {
            mqttClient.reconnect();
        }
        catch(MqttException e) {
            Log.e(TAG, "Code=" + e.getReasonCode());
            Log.e(TAG, "Message=" + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            mqttClient.disconnect();
        }
        catch(MqttException e) {
            Log.e(TAG, "Code=" + e.getReasonCode());
            Log.e(TAG, "Message=" + e.getMessage());
        }
    }

}