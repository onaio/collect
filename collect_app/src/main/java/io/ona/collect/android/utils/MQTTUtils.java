package io.ona.collect.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormListDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.picasa.Content;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.utilities.WebUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.ona.collect.android.R;
import io.ona.collect.android.logic.mqtt.FormSchemaUpdateHandler;
import io.ona.collect.android.logic.mqtt.MqttMessageHandler;
import io.ona.collect.android.provider.FormsProviderAPI;
import io.ona.collect.android.provider.InstanceProviderAPI;

/**
 * Created by Jason Rogena - jrogena@ona.io on 09/12/2016.
 */

public class MqttUtils {
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
    private static final String TOPIC_ODK = "odk";
    private static final String TOPIC_FORMS = "forms";
    private static final String TOPIC_USERS = "users";
    private static final String TOPIC_MESSAGES = "messages";
    private static final String TOPIC_SCHEMA = "schema";

    private static MqttAndroidClient mqttAndroidClient;
    private static HashMap<String, ArrayList<String>> subscribedFormTopics;
    private static HashMap<String, ArrayList<String>> subscribedUserTopics;
    private static ArrayList<MqttMessageHandler> messageHandlers;

    static {
        subscribedFormTopics = new HashMap<>();
        subscribedFormTopics.put(TOPIC_SCHEMA, new ArrayList<String>());
        subscribedFormTopics.put(TOPIC_MESSAGES, new ArrayList<String>());

        subscribedUserTopics = new HashMap<>();
        subscribedUserTopics.put(TOPIC_MESSAGES, new ArrayList<String>());
    }

    public static final int QOS_AT_MOST_ONCE = 0;
    public static final int QOS_AT_LEAST_ONCE = 1;
    public static final int QOS_EXACTLY_ONCE = 2;

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
            messageHandlers = new ArrayList<>();
            messageHandlers.add(new FormSchemaUpdateHandler());
            mqttAndroidClient = new MqttAndroidClient(context, "ssl://10.20.22.169:8883", clientId);
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
                        boolean handled = false;
                        for(MqttMessageHandler curHandler : messageHandlers) {
                            if(curHandler.canHandle(topic, message)) {
                                curHandler.handle(topic, message);
                                handled = true;
                                break;
                            }
                        }

                        if(!handled) {
                            Log.w(TAG, "Could not handle message from topic " + topic);
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

        Context context = Collect.getInstance();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String username = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        String userMessagesTopic = getTopicString(new String[]{TOPIC_USERS, username, TOPIC_MESSAGES}, false);
        ArrayList<String> newUserMessagesTopics = new ArrayList<>();
        newUserMessagesTopics.add(userMessagesTopic);

        updateSubscriptions(subscribedUserTopics.get(TOPIC_MESSAGES), newUserMessagesTopics, QOS_EXACTLY_ONCE);

        refreshFormTopicSubscriptions(true);
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

    public static String subscribeToTopic(final String[] topic, boolean subscribeToSubtopics, int qualityOfService) {
        String topicString = getTopicString(topic, subscribeToSubtopics);
        subscribeToTopic(topicString, qualityOfService);
        return topicString;
    }

    private static String getTopicString(String[] topic, boolean subscribeToSubtopics) {
        String topicString = "";
        for(String curTopic : topic) {
            if(topicString.length() > 0) {
                topicString = topicString + "/";
            }
            topicString = topicString + curTopic;
        }

        if(subscribeToSubtopics) {
            topicString = topicString + "/#";
        }

        return topicString;
    }

    public static void subscribeToTopic(final String topic, int qualityOfService) {
        try {
            if (mqttAndroidClient != null) {
                IMqttToken subToken = mqttAndroidClient.subscribe(topic, qualityOfService);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Successfully subscribed to MQTT topic: " + topic);
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (exception != null) exception.printStackTrace();
                        Log.w(TAG, "Failed to subscribe to MQTT topic");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unsubscribeFromTopic(final String topic) {
        try {
            if (mqttAndroidClient != null) {
                IMqttToken unsubToken = mqttAndroidClient.unsubscribe(topic);
                unsubToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Successfully unsubscribed from MQTT topic " + topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w(TAG, "Could not subscribe to MQTT topic " + topic);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method subscribes to and unsubscribes from form topics depending on whether the forms are
     * currently available on the device (should only subscribe to topics corresponding to forms that
     * are currently on the device)
     */
    public static void refreshFormTopicSubscriptions(final boolean firstTime) {
        DownloadFormListTask downloadFormListTask = new DownloadFormListTask();
        downloadFormListTask.setDownloaderListener(new FormListDownloaderListener() {
            @Override
            public void formListDownloadingComplete(HashMap<String, FormDetails> value) {
                if(value.containsKey(DownloadFormListTask.DL_AUTH_REQUIRED)) {
                    if(firstTime) {
                        Context context = Collect.getInstance().getApplicationContext();
                        SharedPreferences settings =
                                PreferenceManager.getDefaultSharedPreferences(context);
                        String username =
                                settings.getString(PreferencesActivity.KEY_USERNAME, null);
                        String password =
                                settings.getString(PreferencesActivity.KEY_PASSWORD, null);

                        String server =
                                settings
                                        .getString(PreferencesActivity.KEY_SERVER_URL,
                                                context.getResources()
                                                        .getString(R.string.default_server_url));
                        String formListUrl = context
                                .getResources().getString(R.string.default_odk_formlist);
                        final String url = server
                                        + settings.getString(
                                PreferencesActivity.KEY_FORMLIST_URL, formListUrl);
                        Uri u = Uri.parse(url);
                        WebUtils.addCredentials(username, password, u.getHost());
                        refreshFormTopicSubscriptions(false);
                    }
                } else if(value.containsKey(DownloadFormListTask.DL_ERROR_MSG)) {
                    Log.e(TAG, "An error occurred while trying to get the list of available forms");
                } else {
                    ArrayList<String> newFormSchemaSubscriptions = new ArrayList<String>();
                    ArrayList<String> newFormMessagesSubscriptions = new ArrayList<String>();

                    //get the Ona formId from the forms
                    for(FormDetails curForm : value.values()) {
                        //determine if form is currently downloaded
                        if(isFormDownloaded(curForm.formID)) {
                            String onaFormId = getOnaFormId(curForm.downloadUrl);
                            Log.d(TAG, "Ona form id for "+curForm.downloadUrl+" is "+onaFormId);
                            if(onaFormId != null) {
                                String schemaTopicString = getTopicString(
                                        new String[]{TOPIC_ODK,
                                                TOPIC_FORMS,
                                                onaFormId,
                                                TOPIC_SCHEMA},
                                        false);
                                newFormSchemaSubscriptions.add(schemaTopicString);

                                String messagesTopicString = getTopicString(
                                        new String[]{TOPIC_ODK,
                                                TOPIC_FORMS,
                                                onaFormId,
                                                TOPIC_MESSAGES},
                                        false);
                                newFormMessagesSubscriptions.add(messagesTopicString);
                            }
                        } else {
                            Log.d(TAG, curForm.formID + " is not downloaded");
                        }
                    }

                    updateSubscriptions(subscribedFormTopics.get(TOPIC_SCHEMA), newFormSchemaSubscriptions, QOS_EXACTLY_ONCE);
                    updateSubscriptions(subscribedFormTopics.get(TOPIC_MESSAGES), newFormMessagesSubscriptions, QOS_EXACTLY_ONCE);
                }
            }
        });
        downloadFormListTask.execute();
    }

    private static void updateSubscriptions(ArrayList<String> currentSubscriptions, ArrayList<String> newSubscriptions, int qos) {
        ArrayList<String> oldSubscriptions = new ArrayList<>(currentSubscriptions);
        for(String curNewSubscription : newSubscriptions) {
            if(oldSubscriptions.contains(curNewSubscription)) {
                oldSubscriptions.remove(curNewSubscription);
            } else {
                subscribeToTopic(curNewSubscription, qos);
                currentSubscriptions.add(curNewSubscription);
            }

            for(String curOldSubscription : oldSubscriptions) {
                currentSubscriptions.remove(curOldSubscription);
                unsubscribeFromTopic(curOldSubscription);
            }
        }
    }

    private static boolean isFormDownloaded(String jrFormId) {
        String[] selectionArgs = { jrFormId };
        String selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=?";
        String[] fields = { FormsProviderAPI.FormsColumns.JR_VERSION };

        Cursor formCursor = null;
        try {
            formCursor = Collect.getInstance().getContentResolver().query(
                    FormsProviderAPI.FormsColumns.CONTENT_URI,
                    fields, selection, selectionArgs, null);
            if ( formCursor.getCount() == 1 ) {
                // form does not already exist locally
                return true;
            }
        } finally {
            if (formCursor != null) {
                formCursor.close();
            }
        }

        return false;
    }

    private static String getOnaFormId(String formDownloadUrl) {
        //https://odk.ona.io/yri/forms/129132/form.xml
        Pattern pattern = Pattern.compile("http[s]?://[\\w\\._]+/[\\w_]+/forms/([\\d]+)/form\\.xml");
        Matcher matcher = pattern.matcher(formDownloadUrl);
        if(matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
