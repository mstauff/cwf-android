package org.ldscd.callingworkflow.display;

import android.annotation.SuppressLint;
import android.app.Application;

import org.ldscd.callingworkflow.dependencies.ApplicationContextModule;
import org.ldscd.callingworkflow.dependencies.DaggerNetComponent;
import org.ldscd.callingworkflow.dependencies.NetComponent;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CWFApplication extends Application {
    private NetComponent netComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        handleSSLHandshake();

        netComponent = DaggerNetComponent.builder()
                .applicationContextModule(new ApplicationContextModule(this))
                .build();
    }

    public NetComponent getNetComponent() {
        return netComponent;
    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return hostname.contains("lds.org");
                }
            });
        } catch (Exception ignored) {
        }
    }
}
