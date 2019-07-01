package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.junit.Test;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.RSA;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("HardCodedStringLiteral")
public class RSATest {

    private final String KEY = "f05CRI93II5ToscYyOx9k6m+qBOkOCRjOtKV0xwGPOk5/kgPMT7/p4YZfv7JfXBuNM9pXp/t4zDVjq7NqE0FIFOF" +
            "FxXVKUd8UdqY5vbSX5vTx0VyPjvbKsqv9TJt72VSv/d2zrBCX8SUA/k1OtRyQJwbG6OcxFLIiW2xBFlco0eKuIyTXAIZt9OHXEXJJ/rnYk" +
            "DX2Qhvyl5YpRz4GzcESazik/YPF4XYQIhqcg3DBuwP5IeCa4eYEOcz7ajozexQapRKVLBN3LMvjSXoe9uy6W7leNHe/uF8q5oUT1yPGM/" +
            "12KPsV5u8b78QO9+0/rH1Qzh8Ojc87h56Alcxmg8rBg==";
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