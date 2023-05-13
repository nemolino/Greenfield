package admin_server;

import static utils.Printer.*;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.paho.client.mqttv3.*;
import com.google.gson.Gson;
import robot.MQTT_pollution.PollutionMessageWithID;

import java.io.IOException;

import static utils.Utils.ADMIN_SERVER_ADDRESS;
import static utils.Utils.MQTT_BROKER_ADDRESS;

public class StartServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServerFactory.create(ADMIN_SERVER_ADDRESS + "/");
        server.start();

        warnln("Server running!");
        warnln("Server started on: " + ADMIN_SERVER_ADDRESS);

        // MQTT subscription

        MqttClient client = null;
        String[] topics = new String[]{"greenfield/pollution/district1",
                                        "greenfield/pollution/district2",
                                        "greenfield/pollution/district3",
                                        "greenfield/pollution/district4"};

        try {
            client = new MqttClient(MQTT_BROKER_ADDRESS, MqttClient.generateClientId());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            client.connect(connOpts);
            successln("AdminServer connected to MQTT Broker at " + MQTT_BROKER_ADDRESS);

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {

                    PollutionMessageWithID msg = new Gson().fromJson(new String(message.getPayload()), PollutionMessageWithID.class);
                    System.out.println("PollutionMessageWithID received on topic " + topic +
                            "\nrobotID: " + msg.getId() +
                            " , timestamp: " + msg.getTimestamp() +
                            " , averages list: " + msg.getAverages() + "\n");

                    SmartCity.getInstance().addPollutionData(msg.getId(), new PollutionMessage(msg.getTimestamp(), msg.getAverages()));
                }

                public void connectionLost(Throwable cause) {
                    errorln("Connectionlost! cause:" + cause.getMessage());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}

            });

            for (String t : topics) {
                client.subscribe(t, 2);
                successln("AdminServer subscribed to topic : " + t);
            }

        } catch (MqttException me ) {
            errorln("ERROR IN MQTT CONNECTION");
            errorln("reason " + me.getReasonCode());
            errorln("msg " + me.getMessage());
            errorln("loc " + me.getLocalizedMessage());
            errorln("cause " + me.getCause());
            errorln("excep " + me);
            me.printStackTrace();
        }

        // ------------------

        warnln("Hit return to stop...");
        System.in.read();

        warnln("Disconnection from MQTT broker");
        try {
            client.disconnect();
        } catch (MqttException me) {
            errorln("ERROR IN MQTT DISCONNECTION");
            errorln("reason " + me.getReasonCode());
            errorln("msg " + me.getMessage());
            errorln("loc " + me.getLocalizedMessage());
            errorln("cause " + me.getCause());
            errorln("excep " + me);
            me.printStackTrace();
        }

        warnln("Stopping server");
        server.stop(0);
        warnln("Server stopped");
    }
}
