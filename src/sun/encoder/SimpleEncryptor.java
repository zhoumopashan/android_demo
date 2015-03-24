package sun.encoder;

import java.io.UnsupportedEncodingException;

public class SimpleEncryptor {
	
	/**
	 * getEncryptedData.
	 * <p>
	 * Just ^ 7 of every byte, a simple encryMethod
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getEncryptedData(String data) {
		byte[] encryptedData = null;

		if (data == null) {
			return encryptedData;
		}

		try {
			encryptedData = data.getBytes("UTF-8");
			for (int i = 0; i < encryptedData.length; i++) {
				encryptedData[i] = (byte) (encryptedData[i] ^ 7);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			encryptedData = null;
		}

		return encryptedData;
	}
}
