package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import java.util.ResourceBundle;

public class Config {
    private static final Config ourInstance = new Config();

    public static Config getInstance() {
        return ourInstance;
    }

    private final ResourceBundle config;
    private static final String USER_NAME_KEY = "BOT_USER_NAME";
    private static final String TOKEN_KEY = "BOT_TOKEN";
    private static final String PROXY_HOST_KEY = "PROXY_HOST";
    private static final String PROXY_PORT_KEY = "PROXY_PORT";
    private static final String PROXY_USER_KEY = "PROXY_USER";
    private static final String PROXY_PASSWORD_KEY = "PROXY_PASSWORD";
    private static final String STARTUP_MESSAGE_KEY = "START_MESSAGE";
    private static final String HELP_MESSAGE_KEY = "HELP_MESSAGE";

    private Config() {
        config = ResourceBundle.getBundle("config");
    }

    public String getBotUserName(){
        return config.getString(USER_NAME_KEY);
    }

    public String getBotToken(){
        return config.getString(TOKEN_KEY);
    }

    public String getProxyHost(){
        return config.getString(PROXY_HOST_KEY);
    }

    public int getProxyPort(){
        return Integer.parseInt(config.getString(PROXY_PORT_KEY));
    }

    public String getProxyUser(){
        return config.getString(PROXY_USER_KEY);
    }

    public String getProxyPassword(){
        return config.getString(PROXY_PASSWORD_KEY);
    }

    public String getStartupMessage(){
        return config.getString(STARTUP_MESSAGE_KEY);
    }

    public String getHelpMessage(){
        return config.getString(HELP_MESSAGE_KEY);
    }
}
