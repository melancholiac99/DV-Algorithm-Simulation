package utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 86130
 */
public class UserCmd {
    public static int readUserCmd(byte[] cmdBuf, int bufLen){
        InputStream inputStream = System.in;
        byte[] buffer = new byte[bufLen];
 //       byte[] cmdBuf = new byte[256];

        try {
            // 等待200微秒以检测用户输入
            if (inputStream.available() > 0) {
                int bytesRead = inputStream.read(buffer);
                // 将读取的字节复制到提供的缓冲区中,
                System.arraycopy(buffer, 0, cmdBuf, 0, bytesRead);
                return 1;
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

    }

   }

