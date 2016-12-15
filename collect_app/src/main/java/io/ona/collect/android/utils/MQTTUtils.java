package io.ona.collect.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.PreferencesActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Jason Rogena - jrogena@ona.io on 09/12/2016.
 */

public class MQTTUtils {
    private static final String TAG = "MQTTUtils";
    /*
    After creating the client key (android.collect.ona.io.key) and certificate (android.collect.ona.io.crt) using the instructions here -> https://github.com/onaio/playbooks/wiki/Creating-a-Mosquitto-Client-Certificate-and-Key
    Create a Bouncycastle Key Store with the pair:

        openssl pkcs12 -export -inkey android.collect.ona.io.key -in android.collect.ona.io.crt -out android.collect.ona.io.p12 -name android.collect.ona.io
        wget -c https://www.bouncycastle.org/download/bcprov-jdk15on-155.jar
        keytool -importkeystore -srckeystore android.collect.ona.io.p12 -srcstorepass bU3mVNhcMHn8RwJBsKEdMBbpN -srcstoretype pkcs12 -destkeystore android.collect.ona.io.bks -deststorepass bU3mVNhcMHn8RwJBsKEdMBbpN -deststoretype bks -providerclass org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-155.jar

    You will also need to create a Bouncycastle Key Store for the Certificate Authority being used:

        keytool -import -alias mosquitto_ca -file mosquitto_ca.crt -keypass usTjh46qEvHeWmhTVB6CXQjFb -keystore mosquitto_ca.bks  -storetype BKS -storepass usTjh46qEvHeWmhTVB6CXQjFb -providerClass org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-155.jar

     */

    private static final String CA_KEYSTORE_FILE = "mosquitto_ca.bks";
    private static final String CA_KEYSTORE_PASSWORD = "usTjh46qEvHeWmhTVB6CXQjFb";
    private static final String COLLECT_KEYSTORE_FILE = "android.collect.ona.io.bks";
    private static final String COLLECT_KEYSTORE_PASSWORD = "bU3mVNhcMHn8RwJBsKEdMBbpN";

    private static MqttAndroidClient mqttAndroidClient;

    public static MqttAndroidClient getMqttAndroidClientInstance() {
        if(mqttAndroidClient == null) {
            initMqttAndroidClient();
        }

        return mqttAndroidClient;
    }

    private static boolean initMqttAndroidClient() {
        if(okToInit()) {
            Log.i(TAG, "Initializing the MQTT Client");
            Context context = Collect.getInstance();
            String clientId = getClientId();

            Log.d(TAG, "Client id is "+clientId);
            mqttAndroidClient = new MqttAndroidClient(context, "ssl://10.20.22.150:8883", clientId);
            try {
                Log.d(TAG, "About to try connection");
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(false);

                options.setSocketFactory(getMosquittoSocketFactory());

                final IMqttToken token = mqttAndroidClient.connect(options);
                Log.d(TAG, "Post connect code");
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        subscribeToAllTopics();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        exception.printStackTrace();
                        Log.w(TAG, "Unable to connect to the MQTT broker because of "+exception.getMessage());
                    }
                });

                mqttAndroidClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.w(TAG, "Connection to broker lost");
                    }
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        if (message != null && message.getPayload() != null) {
                            String payloadString = new String(message.getPayload());
                            Log.d(TAG, "Topic: " + topic + " Message: " + payloadString);
                        } else {
                            Log.w(TAG, "Received a message with a null payload");
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.d(TAG, "Delivery Complete");
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void subscribeToAllTopics() {
        Log.i(TAG, "Subscribing to all topics");
        subscribeToTopic("test", 2);
    }

    /**
     * This method checks whether it's OK to initialize the MqttAndroidClient
     *
     * TODO:write tests
     *
     * @return TRUE if it's OK to initialize the client
     */
    private static boolean okToInit() {
        Context context = Collect.getInstance();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        if(storedUsername != null
                && storedUsername.trim().length() > 0) {
            return true;
        }

        return false;
    }

    /**
     * TODO:write tests
     * @return
     */
    private static String getClientId() {
        Context context = Collect.getInstance().getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String username = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        String deviceId = getDeviceId();

        return username + "_" +deviceId;
    }

    /**
     * This method uses the same logic to determine the device Id used by {@link PropertyManager}
     *
     *
     *
     * @return  The device id
     */
    private static String getDeviceId() {
        Context context = Collect.getInstance();
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        String orDeviceId = null;
        if (deviceId != null ) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId =
                        Settings.Secure
                                .getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                orDeviceId = deviceId;
            } else {
                orDeviceId = deviceId;
            }
        }

        if ( deviceId == null ) {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();
            if ( info != null && !PropertyManager.ANDROID6_FAKE_MAC.equals(info.getMacAddress())) {
                deviceId = info.getMacAddress();
                orDeviceId = deviceId;
            }
        }

        // if it is still null, use ANDROID_ID
        if ( deviceId == null ) {
            deviceId =
                    Settings.Secure
                            .getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            orDeviceId = deviceId;
        }

        return orDeviceId;
    }

    static SSLSocketFactory getMosquittoSocketFactory () throws Exception {
        Context context = Collect.getInstance();

        InputStream caInputStream = context.getAssets().open(CA_KEYSTORE_FILE);
        InputStream clientInputStream = context.getAssets().open(COLLECT_KEYSTORE_FILE);
        try{
            //load ca cert
            SSLContext ctx = null;
            SSLSocketFactory sslSockFactory=null;
            KeyStore caKs;
            caKs = KeyStore.getInstance("BKS");
            caKs.load(caInputStream, CA_KEYSTORE_PASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(caKs);

            //load client key and cert
            KeyStore ks;
            ks = KeyStore.getInstance("BKS");
            ks.load(clientInputStream, COLLECT_KEYSTORE_PASSWORD.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, COLLECT_KEYSTORE_PASSWORD.toCharArray());

            ctx = SSLContext.getInstance("TLSv1");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            sslSockFactory=ctx.getSocketFactory();
            return sslSockFactory;

        } catch (KeyStoreException e) {
            throw new MqttSecurityException(e);
        } catch (CertificateException e) {
            throw new MqttSecurityException(e);
        } catch (FileNotFoundException e) {
            throw new MqttSecurityException(e);
        } catch (IOException e) {
            throw new MqttSecurityException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new MqttSecurityException(e);
        } catch (KeyManagementException e) {
            throw new MqttSecurityException(e);
        }
    }

    public static void subscribeToTopic(final String topic, int qualityOfService) {
        try {
            if (mqttAndroidClient != null) {
                IMqttToken subToken = mqttAndroidClient.subscribe(topic, qualityOfService);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Successfully subscribed to MQTT topic: " + topic);
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (exception != null) exception.printStackTrace();
                        Log.d(TAG, "Failed to subscribe to MQTT topic");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
