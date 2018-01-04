package test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONObject;
public class BZX509TrustManager   implements X509TrustManager {

	public BZX509TrustManager(){}
	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static SSLSocketFactory getSSFactory() throws NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException{
		TrustManager[] tm = { new BZX509TrustManager()};
		SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
		sslContext.init(null, tm, new java.security.SecureRandom());
		SSLSocketFactory ssf = sslContext.getSocketFactory();
		return  ssf;
	}
	public static void main(String[] args){
		try {
			sendPost("https://localhost:8443","get",null);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

/**
	 * 发送https请求共用体 
	 */
	private  static JSONObject  sendPost(String url,String parame,Map<String,Object> pmap) throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException{
		// 请求结果
		JSONObject json = new JSONObject();
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		URL realUrl;
		HttpsURLConnection conn;
		//查询地址
		String queryString = url;
		//请求参数获取
		String postpar = "";
		//字符串请求参数
		if(parame!=null){
			postpar = parame;
		}
		// map格式的请求参数
		if(pmap!=null){
			StringBuffer mstr = new StringBuffer();
			for(String str:pmap.keySet()){
				String val = (String) pmap.get(str);
				try {
					val=URLEncoder.encode(val,"UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
					mstr.append(str+"="+val+"&");
			}
			// 最终参数
			postpar = mstr.toString(); 
			int lasts=postpar.lastIndexOf("&");
			postpar=postpar.substring(0, lasts);
		}
		if(postpar.toUpperCase().equals("GET")){
			queryString+="?"+postpar;
		}
		SSLSocketFactory  ssf= BZX509TrustManager.getSSFactory();
		try {
			realUrl= new URL(queryString);
			conn = (HttpsURLConnection)realUrl.openConnection();
			conn.setSSLSocketFactory(ssf);
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			if(postpar.toUpperCase().equals("POST")){
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setUseCaches(false);
				out = new PrintWriter(conn.getOutputStream());
				out.print(postpar);
				out.flush();
			}else{
				conn.connect();
			}
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(),"utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		    json = JSONObject.fromObject(result);
		}finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return json;
}
}
