import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

public class HttpClientLoginProxy {
public static void main(String[] args) throws Exception{
		
	
		CookieStore cookieStore = new BasicCookieStore();
	
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		
		RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
		
		SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		//设置https验证的代理,8002为代理Port
		credsProvider.setCredentials(new AuthScope("web-proxy.cn.hpecorp.net", 8080),new UsernamePasswordCredentials("", ""));

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,new String[] { "TLSv1" },null,SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCredentialsProvider(credsProvider).setDefaultCookieStore(cookieStore).setSSLSocketFactory(sslsf).build();
		try {
			
			HttpHost httpHost = new HttpHost("https://www.baidu.com", 443, "https");
			//设置代理,8002为代理Port
			HttpHost proxy = new HttpHost("web-proxy.cn.hpecorp.net", 8080);
			RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
			//Login的URL
//			HttpPost httppost = new HttpPost("");
//			httppost.setConfig(config);
//			//表单填写
//			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//			formparams.add(new BasicNameValuePair("admin", "admin"));
//			formparams.add(new BasicNameValuePair("password", "password"));
//			formparams.add(new BasicNameValuePair("destination", "/index.html"));
//			
//			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
//			httppost.setEntity(entity);

			//System.out.println("Executing request " + httppost.getRequestLine() 	+ " to " + httppost + " via " + proxy);
			 HttpGet httpGet = new HttpGet("https://www.baidu.com");    
			 httpGet.setConfig(config);
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					//status可以手动登陆后用firebug或者fiddler抓取返回
					if (status >= 200 && status < 303) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};

			//登录
			System.out.println("===========执行登录=============");
			String response = httpclient.execute(httpHost,httpGet,responseHandler,context);
            System.out.println(response);
            httpGet.releaseConnection();
            System.out.println("===========访问第二个页面===========");

            //访问登陆后的第二个页面，并打印出来，这边要注意第二个页面是Post还是Get方式提交表单，如果是post请用HttpPost
            HttpGet httpgetConn = new HttpGet("yourNextPageUrl");
            httpgetConn.setConfig(config);
            String responseConn = httpclient.execute(httpHost,httpgetConn,responseHandler);

            System.out.println(responseConn);
          
            
      
		} finally {
			
            httpclient.close();
		}

	}
}
