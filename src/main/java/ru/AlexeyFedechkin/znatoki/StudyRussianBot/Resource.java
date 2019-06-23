/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * wrapper for Resource bundle with string constant
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Resource {
    private static final Logger logger = Logger.getLogger(Resource.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("string");

    /**
     * @param key key of string constant
     * @return string by giving constant if value is exist
     */
    public String getStringByKey(String key){
        logger.info("get string from resource bundle string.properties. key = " + key);
        return resourceBundle.getString(key);
    }
}
