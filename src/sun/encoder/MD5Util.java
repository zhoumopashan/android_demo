package sun.encoder;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static String getMD5(String input) {
        String md5 = null;
        int digestbyte;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                digestbyte = messageDigest[i] & 0xff;
                /** if byte is less than 0x10, add padding */
                if (digestbyte < 0x10)
                    sb.append("0");
                
                sb.append(Integer.toString(digestbyte, 16));
            }
            md5 = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return md5;
    }
    
    /** */
    public static String getFileMD5(String path) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(path);
        
        byte[] databytes = new byte[4096];
        int readbytes;
        while ((readbytes = fis.read(databytes)) != -1) {
            md.update(databytes, 0, readbytes);
        }
        byte[] mdbytes = md.digest();
        int digestbyte;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            digestbyte = mdbytes[i] & 0xff;
            /** if byte is less than 0x10, add padding */
            if (digestbyte < 0x10)
                sb.append("0");
            sb.append(Integer.toHexString(digestbyte));
        }
        
        fis.close();
        
        return sb.toString();
    }
}
