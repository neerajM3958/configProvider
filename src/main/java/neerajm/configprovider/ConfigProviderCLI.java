package neerajm.configprovider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import neerajm.cli.CLI;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ConfigProviderCLI extends CLI {
    private Logger mLogger;
    public ConfigProviderCLI(String[] args) {
        mLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        mLogger.setLevel(Level.ERROR);
        try {
            parseArgs(args);
        } catch (IllegalArgumentException e) {
            mLogger.error("Invalid arguments provided - " + getOpts());
            mLogger.debug(e.getMessage(),e);
        }
    }

    @Override
    public boolean opts(String key, ArrayList<String> values) {
        mLogger.debug(String.format("OPTS - function: %s | argument: %s ",key, values));
        String value = "";
        ConfigProvider mConfigProvider;
        switch (key){
            case "":
                break;
            case "v":
            case "verbose":
                mLogger.setLevel(Level.DEBUG);
                return true;
            case "g":
            case "get":
                try {
                    mConfigProvider = new ConfigProvider();
                    if(values.size()==2){
                    value = mConfigProvider.load(values.get(0)).getConfig(values.get(1));
                    }else if(values.size()==1){
                        value = mConfigProvider.loadAll().getConfig(values.get(0));
                    }else {
                        throw new IllegalArgumentException("");
                    }
                } catch (Exception e) {
                    mLogger.info(String.format("get function require arguments{1 or 2} but %s is provided.",values.size()));
                    return false;
                }
                System.out.printf("value:%s",value);
                return true;
            case "s":
            case "set":
                try {
                    if(values.size()>=3){
                        mConfigProvider = new ConfigProvider();
                        boolean encrypt = values.size()==4 && values.get(3).toLowerCase().equals("true");
                        mConfigProvider.setConfig(values.get(0),values.get(1),values.get(2),encrypt);
                    }else{
                        throw new IllegalArgumentException(values.toString());
                    }
                } catch (Exception e) {
                    mLogger.info(String.format("set function require arguments{3 or 4} but %s is provided.",values.size()));
                    return false;
                }
                return true;
            default:
                System.out.println("arg is " + key);
                return false;
        }
        return false;
    }

    public static void main(String[] args) {
        new ConfigProviderCLI(args);
    }
}
