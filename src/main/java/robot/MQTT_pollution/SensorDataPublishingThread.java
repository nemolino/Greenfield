package robot.MQTT_pollution;

import org.eclipse.paho.client.mqttv3.*;
import com.google.gson.Gson;
import robot.Robot;

import java.util.List;

import static java.time.Instant.now;
import static utils.Printer.errorln;
import static utils.Printer.logln;

public class SensorDataPublishingThread extends Thread {

    protected volatile boolean stopCondition = false;

    private final Robot r;

    public SensorDataPublishingThread(Robot r){
        this.r = r;
    }

    // bruttino che per quittare potrebbe stare vivo ancora 15 secondi prima di uscire
    @Override
    public void run() {

        String id = r.getId();
        BufferAverages buf = r.getBufferAverages();

        String districtStr = r.getDistrict().toString();
        String topic = "greenfield/pollution/district" + districtStr.charAt(districtStr.length() - 1);

        MqttClient client = r.getMqttClient();
        String clientId = r.getMqttClientId();

        while (!stopCondition) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (client.isConnected()){

                List<Double> averages = buf.readAveragesAndClean();
                long t = System.currentTimeMillis();
                //System.out.println(now().toString());
                String payload = new Gson().toJson(new PollutionMessageWithID(id, t, averages));

                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(2);
                //logln(clientId + " Publishing message: " + payload);
                try {
                    client.publish(topic, message);
                } catch (MqttException me) {
                    errorln("ERROR IN MQTT PUBLISHING");
                    errorln("reason " + me.getReasonCode());
                    errorln("msg " + me.getMessage());
                    errorln("loc " + me.getLocalizedMessage());
                    errorln("cause " + me.getCause());
                    errorln("excep " + me);
                    me.printStackTrace();
                }
                //logln(clientId + " Message published - Thread PID: " + Thread.currentThread().getId());
            }
        }
    }

    public void stopMeGently() {
        stopCondition = true;
    }
}