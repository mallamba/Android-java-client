package com.example.pir;

public class XOR {


    /**
     * This method decrypt a byte array with a XoR key. Decryption is a simple xor decryption.
     * @param image a byte array representing encrypted image
     * @param XOR_KEY the xor key in a String-format
     * @return byte[] a byte array is returned to be converted into an image
     */
    public static byte[] byteArrayEncrypt(byte[] image, String XOR_KEY) {
        int message_length = image.length;
        int key_length = XOR_KEY.length();

        byte[] temp = new byte[message_length];
        for (int i = 0; i < message_length; i++) {
            temp[i] = (byte) (  (image[i]) ^ (  (XOR_KEY.charAt(i % key_length) - 48)  )  );
        }

        return temp;


    }


}
