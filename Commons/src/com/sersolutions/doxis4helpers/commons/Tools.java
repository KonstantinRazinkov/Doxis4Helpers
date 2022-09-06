package com.sersolutions.doxis4helpers.commons;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Class for some specific tool-functions
 */
public class Tools {

    /**
     * Get file from local file storage or from web-server
     * @param path Any path to file
     * @return byte array of the file
     * @throws Exception
     */
    public static byte[] GetFileFromAnywhere(String path) throws Exception {
        byte[] file = null;
        try {
            file = Files.readAllBytes(Paths.get(path));
        } catch (FileNotFoundException fnfex) {
            file = DownloadFileFromWeb(path);
        }
        return file;
    }

    /**
     * Download file from any web-address (including SSL)
     * @param url path to file at web
     * @return byte array of the file
     * @throws Exception
     */
    public static byte[] DownloadFileFromWeb(String url) throws Exception {
        URL templateUrl = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection)templateUrl.openConnection();
        con.setRequestMethod( "GET" );

        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        TrustManager[] trustManagers = new TrustManager[1];
        trustManagers[0] = trustManager;
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return s.equals(sslSession.getPeerHost());
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");

        sslContext.init(null, trustManagers, null);
        con.setSSLSocketFactory(sslContext.getSocketFactory());

        con.setHostnameVerifier(hostnameVerifier);

        int responseCode = con.getResponseCode();

        int bytesRead=-1;
        byte[] fileToDownload = new byte[1024];

        BufferedInputStream bis = new BufferedInputStream(con.getInputStream());

        //BufferedInputStream bis = new BufferedInputStream(templateUrl.openStream());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((bytesRead = bis.read(fileToDownload, 0, 1024)) != -1) {
            baos.write(fileToDownload, 0, bytesRead);
        }
        fileToDownload = baos.toByteArray();

        return fileToDownload;
    }
}
