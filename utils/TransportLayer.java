package utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * 模拟网络UDP通信
 * @author zyt
 * @date 23/11/30
 */
public class TransportLayer {
    private int srcPort;
    private DatagramChannel channel;
    private Selector selector;
    private ByteBuffer sendBuffer;
    private ByteBuffer receiveBuffer;

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public void setChannel(DatagramChannel channel) {
        this.channel = channel;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public Buffer getSendBuffer() {
        return sendBuffer;
    }

    public void setSendBuffer(ByteBuffer sendBuffer) {
        this.sendBuffer = sendBuffer;
    }

    public Buffer getReceiveBuffer() {
        return receiveBuffer;
    }

    public void setReceiveBuffer(ByteBuffer receiveBuffer) {
        this.receiveBuffer = receiveBuffer;
    }


    public TransportLayer(String srcIP, int srcPort) throws IOException {
        this.srcPort = srcPort;
        this.channel = DatagramChannel.open();
        /*设置为非阻塞模式*/
        this.channel.configureBlocking(false);
        //监听目的端口
        channel.socket().bind(new InetSocketAddress(srcIP,srcPort));
        this.selector = Selector.open();
        //将channel注册到选择器上,服务端使用selector选择一个channel链接
        //OP_READ 代表对读操作感兴趣
        channel.register(selector, SelectionKey.OP_READ);
        // 初始化缓冲区大小
        this.sendBuffer = ByteBuffer.allocate(64);
        this.receiveBuffer = ByteBuffer.allocate(64);
    }

    /**
     * 接收UDP消息, 缓存到字节数组recvBuf中
     * @param timeout 超时时间
     * @return 存放收到的UDP消息的byte数组转换的String
     * @throws IOException
     */
     public String getMsg( int timeout) throws IOException {
        // ArrayList<Byte> recvBuf = new ArrayList<Byte>() ;
         byte[] recvBuf = new byte[64];
         int readyChannels = selector.select(timeout);
         if(readyChannels == 0){
             throw new SocketTimeoutException("等待超时");

         }
         /*获取就绪的selectKeys集合，并通过迭代器遍历*/
         Set<SelectionKey> selectionKeys = selector.selectedKeys();
         Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

         while(keyIterator.hasNext()){
             //从迭代器选择一个channel
             SelectionKey key = keyIterator.next();
             //将刚刚选择的channel移除
             keyIterator.remove();
             if(key.isReadable()) {
                 SocketAddress remoteAddr = channel.receive(receiveBuffer);
                 if (remoteAddr != null) {
                     receiveBuffer.flip();
                     //注意get()的第一个参数是byte, 不是它的包装类Byte...
                     receiveBuffer.get(recvBuf, 0, receiveBuffer.remaining());
                     //清除缓冲区数据
                     receiveBuffer.clear();
                 } else {
                     // Todo:此时channel里没有可用的数据报，应进行等待操作

                 }
             }
         }
                    return new String(recvBuf);
     }

    /**
     * 向目的IP的端口定向发送UDP消息
     * @param destIP 目的IP
     * @param destPort 目的端口
     * @param sendBuf 发送的信息
     * @return 信息长度
     */
     public int sendMsg(String destIP, int destPort, byte[] sendBuf) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(destIP,destPort);
        sendBuffer.put(sendBuf);
        //转换缓冲区状态
        sendBuffer.flip();
        //这个send方法是原子的，无需考虑线程安全问题。
        int bytesSent = channel.send(this.sendBuffer,inetSocketAddress);
        if(bytesSent == 0){
            System.out.println("输出缓冲区没有空间。");
        }
        sendBuffer.clear();
         return bytesSent;
     }
     public void closeChannel() throws IOException {
         this.channel.close();
     }
}
