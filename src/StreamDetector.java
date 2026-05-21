import java.net.*;

public class StreamDetector {

    private static final String[] STREAM_PATHS = {
            "/stream.m3u8",
            "/live",
            "/video.mjpg",
            "/mpeg"
    };

    public static String detectHttpVideoStreamPath(String ip, int port) {
        String protocol = (port == 443) ? "https" : "http";

        for (String path : STREAM_PATHS) {
            try {
                URL url = new URL(protocol + "://" + ip + ":" + port + path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(400);
                connection.setReadTimeout(400);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                String contentType = connection.getContentType();
                if (contentType != null) {
                    contentType = contentType.toLowerCase();
                    if (contentType.contains("video") || contentType.contains("m3u8") ||
                            contentType.contains("x-mixed-replace") || contentType.contains("mpeg")) {

                        return path;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}