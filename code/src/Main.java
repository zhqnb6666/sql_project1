import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        try {
            String jsonStrings = Files.readString(Path.of("resources/stations.json"));
            JSONObject jsonObject = JSONObject.parseObject(jsonStrings, Feature.OrderedField);
            for (String stationName : jsonObject.keySet()) {
                JSONObject station = jsonObject.getJSONObject(stationName);
                String district = station.getString("district");
                String chineseName = station.getString("chinese_name");
                String intro = station.getString("intro");

                System.out.println("----------Station Name:" + stationName+"----------");
                System.out.println("District: " + district);
                System.out.println("Chinese Name: " + chineseName);
                System.out.println("Introduction: " + intro);
                System.out.println("--BusInfo");
                JSONArray busInfoArray = JSONArray.parseArray(station.getString("bus_info"));

                for (Object busInfoObject : busInfoArray) {
                    JSONObject busInfo = (JSONObject) busInfoObject;
                    System.out.println("\tchukou: " + busInfo.getString("chukou"));
                    JSONArray busOutInfoArray = busInfo.getJSONArray("busOutInfo");
                    for (Object busOutObject : busOutInfoArray) {
                        JSONObject busOutInfo = (JSONObject) busOutObject;
                        String[] buslines = busOutInfo.getString("busInfo").split("、");
                        System.out.println("\tbusInfo: " + Arrays.toString(buslines));
                        System.out.println("\tbusName: " + busOutInfo.getString("busName"));
                    }
                }


                System.out.println("--TexttInfo");
                JSONArray outInfoArray = JSONArray.parseArray(station.getString("out_info"));

                for (Object outInfoObject : outInfoArray) {
                    JSONObject outInfo = (JSONObject) outInfoObject;
                    System.out.println("\toutt: " + outInfo.getString("outt").trim());
                    String[] textt = outInfo.getString("textt").split("、");
                    System.out.println("\ttextt: " + Arrays.toString(textt));

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> List<T> readJsonArray(Path path, Class<T> clz) {
        try {
            String jsonStrings = Files.readString(path);
            return JSON.parseArray(jsonStrings, clz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
