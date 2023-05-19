package robot.pollution;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import robot.Robot;

import static utils.Printer.errorln;
import static utils.Printer.logln;
import static utils.Utils.MQTT_BROKER_ADDRESS;

public class PollutionMonitoring {

    private final Robot r;

    private BufferAverages b;
    private SensorDataProcessingThread processing;
    private SensorDataPublishingThread publishing;
    private MqttClient client;
    private String clientId;

    public PollutionMonitoring(Robot r){
        this.r = r;
    }

    public Robot getRobot() {
        return r;
    }
    public BufferAverages getBufferAverages() {
        return b;
    }
    public MqttClient getMqttClient() {
        return client;
    }
    public String getMqttClientId() {
        return clientId;
    }

    public void turnOnPollutionProcessing(){

        b = new BufferAverages();
        processing = new SensorDataProcessingThread(b);
        processing.start();
    }

    public void turnOnPollutionPublishing(){

        clientId = MqttClient.generateClientId();
        try {
            client = new MqttClient(MQTT_BROKER_ADDRESS, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            //logln(clientId + " Connecting Broker " + MQTT_BROKER_ADDRESS);
            client.connect(connOpts);
            //logln(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());
            logln("... successfully connected to MQTT broker!");

            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    // Not used Here
                }

                public void connectionLost(Throwable cause) {
                    errorln(clientId + " Connectionlost! cause:" + cause.getMessage());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Until the delivery is completed, messages with QoS 1 or 2 are retained from the client
                    // Delivery for a message is completed when all acknowledgments have been received
                    // When the callback returns from deliveryComplete to the main thread, the client removes the retained messages with QoS 1 or 2.
                    /*
                    if (token.isComplete()) {
                       logln(clientId + " Message delivered - Thread PID: " + Thread.currentThread().getId());
                    }
                    */

                }
            });

        } catch (MqttException me ) {
            errorln("ERROR IN MQTT CONNECTION");
            errorln("reason " + me.getReasonCode());
            errorln("msg " + me.getMessage());
            errorln("loc " + me.getLocalizedMessage());
            errorln("cause " + me.getCause());
            errorln("excep " + me);
            me.printStackTrace();
        }

        publishing = new SensorDataPublishingThread(this);
        publishing.start();
    }

    public void turnOffPollutionProcessing(){

        processing.stopMeGently();
    }

    public void turnOffPollutionPublishing(){

        publishing.stopMeGently();
        try{
            if (client.isConnected())
                client.disconnect();
            logln("Publisher " + clientId + " disconnected - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            errorln("ERROR IN MQTT DISCONNECTION");
            errorln("reason " + me.getReasonCode());
            errorln("msg " + me.getMessage());
            errorln("loc " + me.getLocalizedMessage());
            errorln("cause " + me.getCause());
            errorln("excep " + me);
            me.printStackTrace();
        }
    }
}
