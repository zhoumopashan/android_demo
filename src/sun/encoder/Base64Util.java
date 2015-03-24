package sun.encoder;

import java.io.UnsupportedEncodingException;

public class Base64Util {  
    
	/**
	 * Encode a String by base64
	 */
    public static String getBase64(String str) {  
        byte[] b = null;  
        String s = null;  
        try {  
            b = str.getBytes("utf-8");  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }  
        if (b != null) {  
            s = new BASE64Encoder().encode(b);  
        }  
        return s;  
    }  
    
	/**
	 * Encode a String by base64
	 */
    public static String getBase64(byte[] msg) {  
        String s = null;  
        if (msg != null) {  
            s = new BASE64Encoder().encode(msg);  
        }  
        return s;  
    }
  
    /**
	 * Decode a String by base64
	 */
    public static String getFromBase64(String s) {  
        byte[] b = null;  
        String result = null;  
        if (s != null) {  
            BASE64Decoder decoder = new BASE64Decoder();  
            try {  
                b = decoder.decodeBuffer(s);  
                result = new String(b, "utf-8");  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return result;  
    }  
}  