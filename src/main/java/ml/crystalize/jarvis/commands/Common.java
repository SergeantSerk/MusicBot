/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ml.crystalize.jarvis.commands;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Serkan
 */
public class Common {

    private static Logger logLogger = LoggerFactory.getLogger("Crystalize");

    public static JSONObject readJsonFromUrl(String target) throws IOException, JSONException {
        URL url = new URL(target);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Discord:ml.crystalize.JARVIS:v1.0");
            connection.setDoOutput(true);

            try (InputStream stream = connection.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
                String text = readAll(reader);
                JSONObject json = new JSONObject(text);
                return json;
            }
        } catch (IOException e) {
            logLogger.warn(String.format("(C:%s) %s", "JSONGet", e.toString()));
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readAll(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        int cp;
        while ((cp = reader.read()) != -1) {
            builder.append((char) cp);
        }
        return builder.toString();
    }

}
