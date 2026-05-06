import java.net.*;

public class StreamDetector {

    public static boolean isHttpVideo(String ip, int port) {
        String protocol = (port == 443) ? "https" : "http";
        try {
            URL url = new URL(protocol + "://" + ip + ":" + port + "/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(800);
            connection.setReadTimeout(800);

            String contentType = connection.getContentType();
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                return contentType.contains("video") ||
                        contentType.contains("m3u8") ||
                        contentType.contains("x-mixed-replace") ||
                        contentType.contains("mpeg");
            }
        } catch (Exception ignored) {}
        return false;
    }
}