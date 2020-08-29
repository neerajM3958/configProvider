package neerajm.configprovider;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigProvider {
    private Logger mLogger;
    private Properties mProps;
    private String DIR_HOME, DIR_CONFIG_PROVIDER;

    public ConfigProvider() throws IOException {
        this(null);
    }

    public ConfigProvider(String configName) throws IOException {
        mLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        DIR_HOME = System.getenv("HOME");
        if (DIR_HOME == null) {
            DIR_HOME = System.getenv("USERPROFILE");
        }
        DIR_HOME = DIR_HOME == null ? "" : DIR_HOME;
        DIR_CONFIG_PROVIDER = Paths.get(DIR_HOME, ".conf_provider").normalize().toString();
        File file = new File(DIR_CONFIG_PROVIDER);
        file.mkdirs();
        if (!(file.exists() && file.isDirectory()))
            throw new IOException("Unable to find conf Directory.");
        load(configName);
        mLogger.info("Initialized.");
    }

    public static void main(String[] args) {
        System.out.println("Main method do nothing.");
//        System.out.println(getConfig("log_level","none"));
    }

    public void loadAll() throws IOException {
        final File[] confs = new File(DIR_CONFIG_PROVIDER).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".conf");
            }
        });
        mProps = new Properties();
        if (confs != null)
            for (File conf : confs) {
                FileReader reader = new FileReader(conf);
                mProps.load(reader);
                mLogger.info("conf loaded - " + conf.getPath());
                reader.close();
            }
    }

    public void load(String configName) throws IOException {
        if (configName == null || configName.isEmpty()) return;
        if (mProps == null)
            mProps = new Properties();
        final File conf = new File(DIR_CONFIG_PROVIDER, configName);
        FileReader reader = new FileReader(conf);
        mProps.load(reader);
        mLogger.info("conf loaded - " + conf.getPath());
        reader.close();
    }

    public String getConfig(String key) throws IOException {
        String value = mProps.getProperty(key);
        if (value == null || value.isEmpty()) throw new IOException("Key not found - " + key);
        return value;
    }

    public void setConfig(String filename, String key, String value, boolean encrypt) throws IOException {
        File conf = new File(Paths.get(DIR_CONFIG_PROVIDER, filename).toString());
        if (!conf.exists()) conf.createNewFile();
        FileReader reader = new FileReader(conf);
        Properties props = new Properties();
        props.load(reader);
        if (encrypt) {
            try {
                value = new Encryptor().encrypt(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        props.setProperty(key, value);
        props.store(new FileOutputStream(conf), "");
    }

    public String getConfig(String key, String defaultValue) {
        try {
            return getConfig(key);
        } catch (IOException e) {
            return defaultValue;
        }
    }

    public String getConfig(String key, boolean encrypted) throws Exception {
        if (encrypted) return new Encryptor().decrypt(getConfig(key));
        return getConfig(key);
    }
}
