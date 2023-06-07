package admin_server;

import com.google.gson.Gson;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import common.printer.Type;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import robot.pollution.PollutionMessageWithID;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static common.Util.ADMIN_SERVER_ADDRESS;
import static common.Util.MQTT_BROKER_ADDRESS;
import static common.printer.Printer.*;

public class StartServer {

    public static void main(String[] args) throws IOException {

        Logger.getLogger("com.sun.jersey").setLevel(Level.SEVERE);
        HttpServer server = HttpServerFactory.create(ADMIN_SERVER_ADDRESS + "/");
        server.start();

        log(Type.B, "... server running at " + ADMIN_SERVER_ADDRESS);

        // MQTT subscription
        MqttClient client = null;
        String[] topics = new String[]{ "greenfield/pollution/district1", "greenfield/pollution/district2",
                                        "greenfield/pollution/district3", "greenfield/pollution/district4"};

        try {
            client = new MqttClient(MQTT_BROKER_ADDRESS, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            client.connect(connOpts);
            log(Type.B, "... connected to MQTT Broker at " + MQTT_BROKER_ADDRESS);

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {
                    PollutionMessageWithID msg = new Gson().fromJson(new String(message.getPayload()), PollutionMessageWithID.class);
                    log(Type.P, "... pollution data received from R_" + msg.getId() + " with timestamp " + msg.getTimestamp() + " on topic " + topic);
                    log(Type.P_LOW, "... robotID: " + msg.getId() + " , timestamp: " + msg.getTimestamp() + " , averages list: " + msg.getAverages());
                    try {
                        SmartCity.getInstance().addPollutionData(msg.getId(), new PollutionMessage(msg.getTimestamp(), msg.getAverages()));
                    } catch (RuntimeException e) {
                        warn(Type.P, "... " + e.getMessage());
                    }
                }

                public void connectionLost(Throwable cause) {
                    error(Type.B, "... MQTT connection lost : " + cause.getMessage());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            for (String t : topics) {
                client.subscribe(t, 2);
                log(Type.P, "... subscribed to topic " + t);
            }

        } catch (MqttException me) {
            error(Type.P, "... MQTT error in connection to broker : " + me.getMessage());
        }

        cli("Hit return to stop");
        System.in.read();

        try {
            client.disconnect();
            log(Type.B, "... disconnected from MQTT broker");
        } catch (MqttException me) {
            error(Type.P, "... MQTT error in disconnection from broker : " + me.getMessage());
        }

        log(Type.B, "... stopping server");
        server.stop(0);
        log(Type.B, "... server stopped");
    }
}
