import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;  
  
/** 
 * HTTP 请求工具类 
 * 
 * @author : liii 
 * @version : 1.0.0 
 * @date : 2015/7/21 
 * @see : TODO 
 */  
public class HttpClientTest { 
	/** 
	 * 模拟请求 
	 *  
	 * @param url       资源地址 
	 * @param map   参数列表 
	 * @param encoding  编码 
	 * @return 
	 * @throws NoSuchAlgorithmException  
	 * @throws KeyManagementException  
	 * @throws IOException  
	 * @throws ClientProtocolException  
	 */  
	public static String send(String url, Map<String,String> map,String encoding) throws KeyManagementException, NoSuchAlgorithmException, ClientProtocolException, IOException {  
	    String body = "";  
	    //采用绕过验证的方式处理https请求  
	    SSLContext sslcontext = createIgnoreVerifySSL();  
	      
	       // 设置协议http和https对应的处理socket链接工厂的对象  
	       Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()  
	           .register("http", PlainConnectionSocketFactory.INSTANCE)  
	           .register("https", new SSLConnectionSocketFactory(sslcontext))  
	           .build();  
	       PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);  
	       HttpClients.custom().setConnectionManager(connManager);  
	  
	       //创建自定义的httpclient对象  
	    CloseableHttpClient client = HttpClients.custom().setConnectionManager(connManager).build();  
	//       CloseableHttpClient client = HttpClients.createDefault();  
	      
	    //创建post方式请求对象  
	    HttpPost httpPost = new HttpPost(url);  
	      
	    //装填参数  
	    List<NameValuePair> nvps = new ArrayList<NameValuePair>();  
	    if(map!=null){  
	        for (Entry<String, String> entry : map.entrySet()) {  
	            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));  
	        }  
	    }  
	    //设置参数到请求对象中  
	    httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));  
	  
	    System.out.println("请求地址："+url);  
	    System.out.println("请求参数："+nvps.toString());  
	      
	    //设置header信息  
	    //指定报文头【Content-type】、【User-Agent】  
//	    httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");  
//	    httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
        httpPost.addHeader("Content-Type", "application/json");
	    //执行请求操作，并拿到结果（同步阻塞）  
	    CloseableHttpResponse response = client.execute(httpPost);  
	    //获取结果实体  
	    HttpEntity entity = response.getEntity();  
	    if (entity != null) {  
	        //按指定编码转换结果实体为String类型  
	        body = EntityUtils.toString(entity, encoding);  
	    }  
	    EntityUtils.consume(entity);  
	    //释放链接  
	    response.close();  
	       return body;  
	}  
    public static String doGet(String url,String charset){
        HttpClient client = null;
        String result = null;
        try{
    	    //采用绕过验证的方式处理https请求  
    	    SSLContext sslcontext = createIgnoreVerifySSL();  
    	      
    	       // 设置协议http和https对应的处理socket链接工厂的对象  
    	       Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()  
    	           .register("http", PlainConnectionSocketFactory.INSTANCE)  
    	           .register("https", new SSLConnectionSocketFactory(sslcontext,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))  
    	           .build();  
    	       PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);  
    	       HttpClients.custom().setConnectionManager(connManager);  
    	  
    	       //创建自定义的httpclient对象  
    	    client = HttpClients.custom().setConnectionManager(connManager).build();  
            HttpGet httpGet = new HttpGet(url);  
            HttpResponse response = client.execute(httpGet);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,charset);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }
	/** 
	 * 绕过验证 
	 *   
	 * @return 
	 * @throws NoSuchAlgorithmException  
	 * @throws KeyManagementException  
	 */  
	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {  
//	    SSLContext sc = SSLContext.getInstance("SSLv3");  
	    SSLContext sc = SSLContext.getInstance("TLS");  
	  
	    // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法  
	    X509TrustManager trustManager = new X509TrustManager() {  
	        @Override  
	        public void checkClientTrusted(  
	                java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
	                String paramString) throws CertificateException {  
	        }  
	  
	        @Override  
	        public void checkServerTrusted(  
	                java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
	                String paramString) throws CertificateException {  
	        }  
	  
	        @Override  
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
	            return null;  
	        }  
	    };  
//        SSLSocketFactory ssf = new SSLSocketFactory(ctx,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//        ClientConnectionManager ccm = this.getConnectionManager();
//        SchemeRegistry sr = ccm.getSchemeRegistry();
//        sr.register(new Scheme("https", 8443, ssf));
        
	    sc.init(null, new TrustManager[] { trustManager }, null);  
	    return sc;  
	}  
	public static void main(String[] args) throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, HttpHostConnectException {  
	    String url = "https://localhost:8443/examples/jsp/jsp2/simpletag/hello.jsp";  
	    String body = doGet(url, "utf-8");  
	    System.out.println("交易响应结果：");  
	    System.out.println(body);  
	    System.out.println("-----------------------------------");  
	} 
}