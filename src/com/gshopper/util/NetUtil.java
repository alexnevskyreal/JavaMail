package com.gshopper.util;

import java.io.IOException;  
import java.net.InetAddress;  
import java.net.Socket;  
import java.net.UnknownHostException;  
 
/**
 * 网络工具
 * @author chenguyan
 * @date 2017年5月4日
 */
public class NetUtil {  
      
    /**
     * ip端口是否占用
     * @author chenguyan
     * @date 2017年5月4日  
     * @param host
     * @param port
     * @return
     * @exception 忽略捕获的socket异常
     * @throws UnknownHostException
     */
    public static boolean isPortUsing(String host, int port) throws UnknownHostException {  
        boolean flag = false;  
        InetAddress theAddress = InetAddress.getByName(host);  
        try {  
            @SuppressWarnings({ "unused", "resource" })
			Socket socket = new Socket(theAddress, port);  
            flag = true;  
        } catch (IOException e) {  
        }  
        return flag;  
    } 
    
}
