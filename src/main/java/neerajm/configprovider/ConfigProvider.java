package neerajm.configprovider;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigProvider {
    private Logger mLogger;
    private Properties mProps;
    private final String PATH_CONFIG_PROVIDER;

    public ConfigProvider() throws IOException {
        mLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        String customHome = System.getProperty("HOME");
        String defaultHome = System.getenv("HOME");
        String defaultHomeWindows = System.getenv("USERPROFILE");
        String pathHome;
        if(customHome!=null)
            pathHome = customHome;
        else if (defaultHome!=null)
            pathHome = defaultHome;
        else if (defaultHomeWindows!=null)
            pathHome = defaultHomeWindows;
        else pathHome ="";

        PATH_CONFIG_PROVIDER = Paths.get(pathHome, ".conf_provider").normalize().toString();
        File file = new File(PATH_CONFIG_PROVIDER);
        if(!file.mkdirs() && (!(file.exists() || file.isFile())))
            throw new IOException("Unable to find conf Directory - " + file.getPath());

        mLogger.debug("Config Provider Initialized with " + file.getPath());
    }

    public ConfigProvider loadAll() throws IOException {
        final File[] confs = new File(PATH_CONFIG_PROVIDER).listFiles(new FilenameFilter() {
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
                mLogger.debug("conf loaded - " + conf.getPath());
                reader.close();
            }
        return this;
    }

    public ConfigProvider load(String configName) throws IOException {
        if (mProps == null) mProps = new Properties();
        if (!(configName == null || configName.isEmpty())) {
            final File conf = new File(PATH_CONFIG_PROVIDER, configName);
            FileReader reader = new FileReader(conf);
            mProps.load(reader);
            mLogger.debug("conf loaded - " + conf.getPath());
            reader.close();
        }
        return this;
    }

    public String getConfig(String key) throws IOException {
        String value = mProps.getProperty(key);
        try {
            value = new Encryptor().decrypt(value);
        } catch (Exception e) {
            mLogger.debug("Failed to decrypt value - " + value);
        }
        if (value == null || value.isEmpty()) throw new IOException("Key not found - " + key);
        return value;
    }

    public String getConfig(String key, String defaultValue) {
        try {
            return getConfig(key);
        } catch (IOException e) {
            return defaultValue;
        }
    }

    public void setConfig(String configName, String key, String value, boolean encrypt) throws IOException {
        File confFile = new File(Paths.get(PATH_CONFIG_PROVIDER, configName).toString());
        if (!confFile.exists())
            if(!confFile.createNewFile())
                throw new IOException("Unable to create configuration file - " + confFile.getPath());
        FileReader reader = new FileReader(confFile);
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
        props.store(new FileOutputStream(confFile), "");
    }

    public static void main(String[] args) {
        System.out.println("Main method do nothing.");
        System.out.println("This is not an cli client.");
        try {
            ConfigProvider cp = new ConfigProvider().loadAll();
            cp.setConfig("test.conf","test_key","test_value",true);
            System.out.println(cp.getConfig("test_key"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
