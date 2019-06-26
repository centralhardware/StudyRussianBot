/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 23.06.19 0:38
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * access provides to config file
 */
public class Config {
    private static final Config ourInstance = new Config();

    public static Config getInstance() {
        return ourInstance;
    }

    private final ResourceBundle config;
    private static final String USER_NAME_KEY = "BOT_USER_NAME";
    private static final String TOKEN_KEY = "BOT_TOKEN";
    private static final String USER_TESTING_NAME_KEY = "BOT_TESTING_USER_NAME";
    private static final String TOKEN_TESTING_KEY = "BOT_TESTING_TOKEN";
    private static final String PROXY_HOST_KEY = "PROXY_HOST";
    private static final String PROXY_PORT_KEY = "PROXY_PORT";
    private static final String PROXY_USER_KEY = "PROXY_USER";
    private static final String PROXY_PASSWORD_KEY = "PROXY_PASSWORD";
    private static final String STARTUP_MESSAGE_KEY = "START_MESSAGE";
    private static final String HELP_MESSAGE_KEY = "HELP_MESSAGE";
    private static final String IS_TESTING_KEY = "IS_TESTING";
    private static final String IS_USE_PROXY_KEY = "IS_USE_PROXY";
    private static final String REDIS_HOST_KEY = "REDIS_HOST";
    private static final String REDIS_PORT_KEY = "REDIS_PORT";
    private static final String ADMIN_ID = "ADMIN_ID";
    private static final String RSA_PRIVATE_KEY = "RSA_PRIVATE_KEY";
    private static final String RSA_PUBLIC_KEY = "RSA_PUBLIC_KEY";

    private Config() {
        config = ResourceBundle.getBundle("config");
    }

    /**
     * get list of user that have administration permission
     *
     * @return admin id list
     */
    public ArrayList<Long> getAdminsId(){
        var admins = config.getString(ADMIN_ID).split(",");
        var adminsList = new ArrayList<Long>();
        for (String id : admins){
            adminsList.add(Long.valueOf(id));
        }
        return adminsList;
    }

    public String getRsaPrivateKey(){
        return config.getString(RSA_PRIVATE_KEY);
    }

    public String getRsaPublicKey(){
        return config.getString(RSA_PUBLIC_KEY);
    }

    public boolean isUseProxy(){
        return Boolean.parseBoolean(config.getString(IS_USE_PROXY_KEY));
    }

    public String getBotUserName(){
        return config.getString(USER_NAME_KEY);
    }

    public String getBotToken(){
        return config.getString(TOKEN_KEY);
    }

    public String getBotUserTestingName(){
        return config.getString(USER_TESTING_NAME_KEY);
    }

    public String getBotTestingToken(){
        return config.getString(TOKEN_TESTING_KEY);
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

    public String getRedisHost(){
        return config.getString(REDIS_HOST_KEY);
    }

    public int getRedisPort(){
        return Integer.parseInt(config.getString(REDIS_PORT_KEY));
    }

    public String getStartupMessage(){
        return config.getString(STARTUP_MESSAGE_KEY);
    }

    public String getHelpMessage(){
        return config.getString(HELP_MESSAGE_KEY);
    }

    public boolean isTesting(){
        return Boolean.parseBoolean(config.getString(IS_TESTING_KEY));
    }
}
