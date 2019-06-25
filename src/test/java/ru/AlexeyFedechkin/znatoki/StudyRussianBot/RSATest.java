package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RSATest {

    private final String KEY = "WVgDoh6bCJZ3F78/lvGEFS5WJcFX3qHdKw1yYRrYykiwiapXSrejnpebzpPv5wjNArJD/leMjh39gtfajNHN1" +
            "CoZsEJxBBH8fjw0gW6zzYfr9yHFkNimjVLmKPIpxXjPX0WsHlNe7MjiHYb65XqbGSj6ZWAexoL8p6lKPtfmkQARwY1U83t5gua3Su" +
            "5OljiegeCeJsIzwOhD2TvetTUtK7BvAdtp5XLVbU0L6S4YB4hlTRyR+9+pITqzDbJjIWq7Gd3V8pvJ9W+ppWkjbvIcwDPJ1yWGiC" +
            "KfisBbaLiMEIRYSzcCfsxCiSaaoo+adZxW5aX+oJiRWgbHA9DjdNyr0A==";
    private final RSA rsa = new RSA();

    @Test
    public void generateKey() {
        assertEquals(KEY, rsa.generateKey("central"));
    }

    @Test
    public void validateKey() {
        assertTrue(rsa.validateKey("central",KEY));
    }
}