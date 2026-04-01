import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VLCLauncher {

    public void openNetworkStream(String ip) {
        String streamUrl = "rtmp://@" + ip + ":1935";
        String os = System.getProperty("os.name").toLowerCase();
        String vlcPath = getVlcExecutablePath(os);

        List<String> command = new ArrayList<>();
        command.add(vlcPath);
        command.add("--fullscreen");
        command.add("--no-video-title-show");
        command.add(streamUrl);

        try {
            System.out.println("Attempting to launch: " + vlcPath);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO(); // Optional: redirects VLC errors to your Java console
            pb.start();
        } catch (IOException e) {
            System.err.println("Could not launch VLC. Please ensure it is installed.");
            e.printStackTrace();
        }
    }

    private String getVlcExecutablePath(String os) {
        if (os.contains("win")) {
            String[] commonWindowsPaths = {
                    "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
                    "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"
            };
            for (String path : commonWindowsPaths) {
                if (new File(path).exists()) return path;
            }
            return "vlc"; // Fallback to PATH if not in default folders
        }

        else if (os.contains("mac")) {
            String macPath = "/Applications/VLC.app/Contents/MacOS/VLC";
            if (new File(macPath).exists()) return macPath;
            return "vlc";
        }

        else {
            return "vlc";
        }
    }
}