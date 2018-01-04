package com;

public class TestWebService {
	/*
     * 设置证书。
     */
    static{
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier(){
                    public boolean verify(String hostname,
                            javax.net.ssl.SSLSession sslSession) {
                        //域名或ip地址
                        if (hostname.equals("localhost")) {
                            return true;
                        }
                        return false;
                    }
                });
        //第二个参数为证书的路径
        System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\sunjiang\\.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "tomcat");
    }
    
}
