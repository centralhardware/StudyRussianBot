package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils;

import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;

public class Auth extends Authenticator {
    @Override
    public PasswordAuthentication requestPasswordAuthenticationInstance(String host, InetAddress addr,
                                                                        int port, String protocol,
                                                                        String prompt, String scheme, URL url,
                                                                        RequestorType reqType) {
        return new PasswordAuthentication(Config.getProxyUser(),
                Config.getProxyPassword().toCharArray());
    }
}
