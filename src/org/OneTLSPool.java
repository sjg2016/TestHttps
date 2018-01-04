package org;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * https单向认证，不需要客户端创建密钥，客户端需要有服务端的公钥
 * @author sunjiang
 *
 */
public class OneTLSPool {
    public static CloseableHttpClient httpclient;
    // 获得池化得HttpClient
    static {
        // 设置truststore
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts
                    .custom()
                    .loadTrustMaterial(
                            new File("C:\\apache-tomcat-7.0.62\\jks\\ssl\\truststore.jks"),
                            "client".toCharArray(),
                            new TrustSelfSignedStrategy()).build();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 客户端支持TLSV1，TLSV2,TLSV3这三个版本
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext, new String[] { "TLSv1", "TLSv2", "TLSv3" }, null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());// 客户端验证服务器身份的策略

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);
        // 个性化设置某个url的连接
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost("www.y.com",
                80)), 20);    
        //设置代理
//        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(new HttpHost("web-proxy.sgp.hpecorp.net", 8080));        
//		httpclient = HttpClients.custom().setRoutePlanner(routePlanner).setConnectionManager(connManager)
//                .build();
		//不设置代理
		httpclient = HttpClients.custom().setConnectionManager(connManager)
                .build();

    }

  /**
     * 单向验证且服务端的证书可信
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public static void oneWayAuthorizationAccepted(String httpsURL) throws ClientProtocolException, IOException {
        // Execution context can be customized locally.
        HttpClientContext context = HttpClientContext.create();
        HttpGet httpget = new HttpGet(httpsURL);
        // 设置请求的配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(5000).setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000).build();
        httpget.setConfig(requestConfig);
        

        System.out.println("executing request " + httpget.getURI());
        CloseableHttpResponse response = httpclient.execute(httpget, context);
        try {
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            System.out.println(EntityUtils.toString(response.getEntity()));
            System.out.println("----------------------------------------");

            // Once the request has been executed the local context can
            // be used to examine updated state and various objects affected
            // by the request execution.

            // Last executed request
            context.getRequest();
            // Execution route
            context.getHttpRoute();
            // Target auth state
            context.getTargetAuthState();
            // Proxy auth state
            context.getTargetAuthState();
            // Cookie origin
            context.getCookieOrigin();
            // Cookie spec used
            context.getCookieSpec();
            // User security token
            context.getUserToken();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] a) throws KeyManagementException,
            NoSuchAlgorithmException, KeyStoreException, CertificateException,
            IOException {
        oneWayAuthorizationAccepted("https://localhost:8443/");
//        oneWayAuthorizationAccepted("https://www.baidu.com");
//    	oneWayAuthorizationAccepted("https://www.alipay.com");
    }
}