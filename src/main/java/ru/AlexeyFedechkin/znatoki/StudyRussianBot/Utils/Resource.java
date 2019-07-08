/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Interfaces.ResourceInt;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * wrapper for Resource bundle with string constant
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Resource implements ResourceInt {
    private static final Logger logger = Logger.getLogger(Resource.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("string");

    /**
     * get string from resource bundle file
     * @param key key of string constant
     * @return string by giving constant if value is exist
     */
    public String getStringByKey(String key){
        logger.info("get string from resource bundle string.properties. key = " + key);
        return resourceBundle.getString(key);
    }

    /**
     * get string from resource folder file
     *
     * @param fileName name of file to search
     * @return string from file that placed in resource folder
     */
    public String getStringFromResources(String fileName) throws IOException {
        var classLoader = ClassLoader.getSystemClassLoader();
        var inputStream = classLoader.getResourceAsStream(fileName);
        var writer = new StringWriter();
        IOUtils.copy(Objects.requireNonNull(inputStream), writer);
        return writer.toString();
    }
}
