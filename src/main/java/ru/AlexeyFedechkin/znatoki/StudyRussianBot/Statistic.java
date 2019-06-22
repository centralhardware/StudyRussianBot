/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 23.06.19 0:39
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import redis.clients.jedis.Jedis;

public class Statistic {
    private static Statistic ourInstance = new Statistic();

    public static Statistic getInstance() {
        return ourInstance;
    }

    private Jedis jedis = new Jedis(Config.getInstance().getRedisHost(), Config.getInstance().getRedisPort());

    private Statistic() {
        jedis.auth(Config.getInstance().getRedisPassword());
    }
}
