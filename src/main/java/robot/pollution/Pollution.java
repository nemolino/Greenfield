package robot.pollution;

import common.printer.Type;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import robot.Robot;

import static common.Util.MQTT_BROKER_ADDRESS;
import static common.printer.Printer.*;

public class Pollution {

    private final Robot r;

    private BufferAverages b;
    private SensorDataProcessingThread processing;
    private SensorDataPublishingThread publishing;
    private MqttClient client;

    public Pollution(Robot r) {
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

    public void turnOnPollutionProcessing() {

        b = new BufferAverages();
        processing = new SensorDataProcessingThread(b);
        processing.start();
    }

    public void turnOnPollutionPublishing() {

        try {
            client = new MqttClient(MQTT_BROKER_ADDRESS, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            client.connect(connOpts);
            log(Type.B, "... connected to MQTT Broker at " + MQTT_BROKER_ADDRESS);

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {
                }

                public void connectionLost(Throwable cause) {
                    error(Type.B, "... MQTT connection lost : " + cause.getMessage());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            publishing = new SensorDataPublishingThread(this);
            publishing.start();
        } catch (MqttException me) {
            error(Type.B, "... MQTT error in connection to broker : " + me.getMessage());
        }
    }

    public void turnOffPollutionProcessing() {
        processing.stopMeGently();
    }

    public void turnOffPollutionPublishing() {
        publishing.stopMeGently();
        try {
            if (client.isConnected())
                client.disconnect();
            log(Type.B, "... disconnected from MQTT Broker");
        } catch (MqttException me) {
            error(Type.B, "... MQTT error in disconnection from broker : " + me.getMessage());
        }
    }
}
