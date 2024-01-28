package zone.rong.blahajasm.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HasteUpload {

    public static String uploadToHaste(String str) throws IOException {
        str = "content=" + str;
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        URL uploadURL = new URL("https://api.mclo.gs/1/log");
        HttpURLConnection connection = (HttpURLConnection) uploadURL.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestProperty("User-Agent", "BlahajASM");
        connection.setFixedLengthStreamingMode(bytes.length);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();
        try {
            try (OutputStream os = connection.getOutputStream()) {
                os.write(bytes);
            }
            try (InputStream is = connection.getInputStream()) {
                JsonObject json = new Gson().fromJson(new InputStreamReader(is), JsonObject.class);
                return json.get("url").getAsString();
            }
        } finally {
            connection.disconnect();
        }
    }
}
