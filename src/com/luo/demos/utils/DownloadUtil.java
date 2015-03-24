package com.luo.demos.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadUtil {

	/**
	 * 
	 * @param sourceUrl
	 * @param desDir
	 * @param fileName
	 * @return whether the download is Success
	 */
	public static boolean downloadFile(String sourceUrl, String desDir, String fileName) {
		try {
			URL url = new URL(sourceUrl);

			// Open the connection
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5*1000);
			InputStream istream = connection.getInputStream();

			File dir = new File(desDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(dir, fileName);
			if(CommonUtils.isFileExist(file)){
				file.delete();
			}
			file.createNewFile();

			OutputStream output = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int readbytes;
			while ((readbytes = istream.read(buffer)) > 0) {
				output.write(buffer, 0, readbytes);
			}

			output.flush();
			output.close();
			istream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	   public static boolean downloadFile(String sourceUrl, OutputStream output) {
	        try {
	            URL url = new URL(sourceUrl);

	            // Open the connection
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setConnectTimeout(5*1000);
	            InputStream istream = connection.getInputStream();

	            byte[] buffer = new byte[1024];
	            int readbytes;
	            while ((readbytes = istream.read(buffer)) > 0) {
	                output.write(buffer, 0, readbytes);
	            }

	            output.flush();
	            output.close();
	            istream.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	        return true;
	    }
	
	/**
	 * downloadFileContent <br>
	 * 
	 * Download the file by giving url, and return it's content by string
	 * 
	 * @return return the String of the file's content
	 */
	public static String downloadFileContent(String sourceUrl) {
		try {
			URL url = new URL(sourceUrl);

			// Open the connection
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(3 * 1000);
			InputStream istream = connection.getInputStream();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = istream.read()) > 0) {
				baos.write(i);
			}
			return baos.toString();

		} catch (Exception e) {
			if(e != null) e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * saveContent <br>
	 * <p>
	 * save the String of content to desDir
	 * 
	 * @return return if the operation is success or not
	 */
	public static boolean saveContent(String content, File desDir) {
		// return if des is null
		if(desDir == null)	return false;
		
		// delete file if exist
		if(desDir.exists()){
			desDir.delete();
		}

		// save file
		try {
			Writer ow = new OutputStreamWriter(new FileOutputStream(desDir), "utf-8");
			ow.write(content.toString());
			ow.close();
			return true;
			
		} catch (Exception e) {
			if (e != null)
				e.printStackTrace();
		}
		return false;
	}

	/**
	 * Convert file from GBK to utf8
	 * 
	 * @param srcFileName
	 * @param destFileName
	 */
	public static void transferFile(String srcFileName, String destFileName) throws IOException {
		String line_separator = System.getProperty("line.separator");
		FileInputStream fis = new FileInputStream(srcFileName);
		StringBuffer content = new StringBuffer();
		DataInputStream in = new DataInputStream(fis);
		// "UTF-8"
		BufferedReader d = new BufferedReader(new InputStreamReader(in, "GBK"));
		String line = null;
		while ((line = d.readLine()) != null)
			content.append(line + line_separator);
		d.close();
		in.close();
		fis.close();

		Writer ow = new OutputStreamWriter(new FileOutputStream(destFileName), "utf-8");
		ow.write(content.toString());
		ow.close();
	}

	/**
	 * Return the JsonObject by given url
	 * 
	 * @param url
	 * @return
	 */
	public static JSONObject getJSONObjFromUrl(String url) {
		HttpResponse response = null;
		JSONObject content = null;
		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
			HttpConnectionParams.setSoTimeout(httpParams, 10000);
			HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
			HttpClient client = new DefaultHttpClient(httpParams);
			client.getParams().setParameter(HttpProtocolParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			client.getParams().setParameter(HttpProtocolParams.HTTP_ELEMENT_CHARSET, "UTF-8");
			client.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");
			client.getParams().setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);

			HttpGet get = new HttpGet(url);
			response = client.execute(get);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(response.getEntity(), "UTF-8");
				if(result != null && result.startsWith("\ufeff")){  
					result =  result.substring(1);  
				} 
				content = new JSONObject(result);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return content;
	}
	
	/**
	 * Return the JsonObject by given url
	 * 
	 * @param url
	 * @return
	 */
	public static JSONObject getJSONObjFromFile(File desFile) {
		
		if(desFile == null || !desFile.exists() || desFile.length() <= 0)	return null;
		
		JSONObject content = null;
		InputStream is = null;
		try {
			is = new FileInputStream(desFile);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = is.read()) != -1) {
				baos.write(i);
			}
			String result = baos.toString();
			if(result != null && result.startsWith("\ufeff")){  
				result =  result.substring(1);  
			}  
			content = new JSONObject(result);
		} catch (JSONException e) {
			if( e != null ) e.printStackTrace();
			return null;
		} catch (ClientProtocolException e) {
			if( e != null ) e.printStackTrace();
			return null;
		} catch (IOException e) {
			if( e != null ) e.printStackTrace();
			return null;
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					if( e != null ) e.printStackTrace();
				}
			}
		}
		return content;
	}
	
	/**
	 * Return the string by given filePath
	 * 
	 * @param url
	 * @return
	 */
	public static String getStringFromFile(File desFile) {
		
		if(desFile == null || !desFile.exists() || desFile.length() <= 0)	return null;
		
		InputStream is = null;
		String result = null;
		try {
			is = new FileInputStream(desFile);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = is.read()) != -1) {
				baos.write(i);
			}
			result = baos.toString();
			if(result != null && result.startsWith("\ufeff")){  
				result =  result.substring(1);  
			}  
		} catch (ClientProtocolException e) {
			if( e != null ) e.printStackTrace();
			return null;
		} catch (IOException e) {
			if( e != null ) e.printStackTrace();
			return null;
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					if( e != null ) e.printStackTrace();
				}
			}
		}
		return result;
	}

}
