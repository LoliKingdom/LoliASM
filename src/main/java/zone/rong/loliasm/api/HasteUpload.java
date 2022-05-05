package zone.rong.loliasm.api;

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
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        URL uploadURL = new URL("https://api.pastes.dev/post");
        HttpURLConnection connection = (HttpURLConnection) uploadURL.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        connection.setRequestProperty("User-Agent", "LoliASM");
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
                return "https://pastes.dev/" + json.get("key").getAsString();
            }
        } finally {
            connection.disconnect();
        }
    }
}
