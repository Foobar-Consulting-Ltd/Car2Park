package first.alexander.com.car2park;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    final String json_object_test = "{\"name\":\"Franklin\",\"server\":{\"version\":0.1,\"updated\":\"2017-10-11\",\"name\":\"senor pointy\"},\"args\":{\"lat\":\"49.2624\",\"lng\":\"-123.2433\"},\"parsedLocation\":{\"coordinates\":[\"49.2624\",\"-123.2433\",0],\"address\":null},\"parkingSpots\":[{\"location\":{\"location\":{\"coordinates\":[49.26188,-123.24648,0],\"address\":null},\"spot\":{\"chargingPole\":false,\"coordinates\":[49.26188,-123.24648,0],\"name\":\"UBC 6191 Agronomy Rd (Enter from laneway)\",\"totalCapacity\":4,\"usedCapacity\":7}},\"distance\":266}]}";

    @Test
    public void parsing_parseJSONRespond() throws Exception {

        JSONObject test_response = new JSONObject(json_object_test);

        final ArrayList<HashMap> test_parking_list = new ArrayList();

        JSONVolleyController.parseJSONRespond(test_response, test_parking_list);

        HashMap parking_info = test_parking_list.get(0);

        assertEquals("Parsed Name not Equal","UBC 6191 Agronomy Rd (Enter from laneway)",parking_info.get("Name"));
        assertEquals("Parsed Lat not Equal","49.26188",parking_info.get("Lat").toString());
        assertEquals("Parsed Long not Equal","-123.24648",parking_info.get("Long").toString());

    }

}