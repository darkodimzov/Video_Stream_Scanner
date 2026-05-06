import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VLCLauncher {

    public void openNetworkStream(String ip, int port) {
        String protocol;
        switch (port) {
            case 554:  protocol = "rtsp"; break;
            case 1935: protocol = "rtmp"; break;
            case 443:  protocol = "https"; break;
            default:   protocol = "http"; break;
        }

        String streamUrl = protocol + "://" + ip + ":" + port;
        String os = System.getProperty("os.name").toLowerCase();
        String vlcPath = getVlcExecutablePath(os);

        List<String> command = new ArrayList<>();
        command.add(vlcPath);
        command.add("--fullscreen");
        command.add("--no-video-title-show");
        command.add(streamUrl);

        try {
            System.out.println(">>> Initializing VLC for: " + streamUrl);
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            System.err.println("Failed to launch VLC at: " + vlcPath);
        }
    }

    private String getVlcExecutablePath(String os) {
        if (os.contains("win")) {
            String[] paths = {"C:\\Program Files\\VideoLAN\\VLC\\vlc.exe", "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"};
            for (String p : paths) if (new File(p).exists()) return p;
        } else if (os.contains("mac")) {
            String macPath = "/Applications/VLC.app/Contents/MacOS/VLC";
            if (new File(macPath).exists()) return macPath;
        }
        return "vlc";
    }
}