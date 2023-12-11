import utils.TransportLayer;

import java.io.IOException;

/**
 * 这个接口描述了在路由矢量算法中，网络各个节点传递路由消息的行为
 * @author zyt
 */
public interface NodeRouteing {
    /**
     * 构造一个DV-Message
     * @param networkNode
     * @param msgType
     * @return 成功返回值为1
     */
    public DistanceVectorMessage constructDistanceVectorMessage(NetworkNode networkNode,int msgType);

    /**
     * 批量发送DV-Message
     * @param transportLayer
     * @param networkNode
     * @param dvMessage
     * @return
     */
    public int floodDistanceVectorMsg(TransportLayer transportLayer, NetworkNode networkNode, DistanceVectorMessage dvMessage) throws IOException;

    /**
     * 定向发送DV-Message
     * @param toNodeId 目的节点id
     * @param transportLayer
     * @param networkNode
     * @param dvMessage
     * @return
     */
    public int sendDistanceVectorMsg(String toNodeId,TransportLayer transportLayer, NetworkNode networkNode,DistanceVectorMessage dvMessage) throws IOException;



}
