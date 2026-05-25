package de.cuzim1tigaaa.spectator.extensions;

import de.cuzim1tigaaa.spectator.Spectator;
import lombok.Getter;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.logging.Level;

public class UpdateChecker {

    private final Spectator plugin;

    @Getter private boolean update;
    @Getter private String version;

    public UpdateChecker(Spectator plugin) {
        this.plugin = plugin;

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            boolean isUpdate = checkUpdate();
            //noinspection AssignmentUsedAsCondition
            if(this.update = isUpdate)
                updateAvailable();
        });
    }

    private void updateAvailable() {
        this.plugin.getLogger().log(Level.WARNING, "A new Version [v" + this.version + "] is available!");
        this.plugin.getLogger().log(Level.WARNING, "https://www.spigotmc.org/resources/spectator.93051/");
    }

    private boolean checkUpdate() {
        this.plugin.getLogger().log(Level.INFO, "Checking for Updates…");
        String versionString = this.plugin.getDescription().getVersion();

        try {
            URL url = URI.create("https://api.spigotmc.org/legacy/update.php?resource=93051").toURL();
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return false;

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            this.version = reader.readLine().replace("v", "");
            return !versionString.equalsIgnoreCase(this.version);
        }catch(IOException exception) {
            return false;
        }
    }
}
