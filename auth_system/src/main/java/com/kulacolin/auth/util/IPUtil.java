package com.kulacolin.auth.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class IPUtil {
    public static String getPublicIP() {
        try {
            URI uri = URI.create("https://api.ipify.org");
            URL url = uri.toURL(); 
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }
}
