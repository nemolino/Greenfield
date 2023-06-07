package robot.pollution;

import com.google.gson.Gson;
import common.printer.Type;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.time.LocalTime;
import java.util.List;

import static common.printer.Printer.error;
import static common.printer.Printer.log;

public class SensorDataPublishingThread extends Thread {

    protected volatile boolean stopCondition = false;

    private final Pollution p;
    private final Object stopLock = new Object();

    public SensorDataPublishingThread(Pollution p){
        this.p = p;
    }

    @Override
    public void run() {

        String id = p.getRobot().getId();
        BufferAverages buf = p.getBufferAverages();
        String districtStr = p.getRobot().getDistrict().toString();
        String topic = "greenfield/pollution/district" + districtStr.charAt(districtStr.length() - 1);

        MqttClient client = p.getMqttClient();

        while (!stopCondition) {

            try {
                synchronized (stopLock){
                    stopLock.wait(15000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (stopCondition) break;

            if (client.isConnected()){

                List<Double> averages = buf.readAveragesAndClean();
                String payload = new Gson().toJson(new PollutionMessageWithID(id, System.currentTimeMillis(), averages));
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(2);
                try {
                    client.publish(topic, message);
                    log(Type.P, "... " + LocalTime.now() + " - 📝 published pollution data at topic " + topic);
                    //log(Type.P, "... " + LocalTime.now() + " - 📝 published pollution data at topic " + topic + " :\n" + message);
                } catch (MqttException me) {
                    error(Type.P, "... MQTT error in publishing pollution data : " + me.getMessage());
                }
            }
        }
    }

    public void stopMeGently() {
        stopCondition = true;
        synchronized (stopLock){
            stopLock.notify();
        }
    }
}