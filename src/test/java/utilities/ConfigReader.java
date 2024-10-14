package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private Properties properties;

    public ConfigReader() {
        try {
            FileInputStream fileInputStream = new FileInputStream("src/test/resources/config.properties");
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Configuration file not found.");
        }
    }

    public String getUrl() {
        return properties.getProperty("app.url");
    }

    public String getUsername() {
        return properties.getProperty("app.username");
    }

    public String getPassword() {
        return properties.getProperty("app.password");
    }
}
