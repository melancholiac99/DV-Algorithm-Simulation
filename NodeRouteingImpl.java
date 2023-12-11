import config.PathCost;
import config.TableItem;
import utils.TransportLayer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

import static config.Constant.*;

/**
 * 这个类完全是为了实现网络节点间定时交换距离矢量信息的模拟
 * @author zyt
 */
public class NodeRouteingImpl implements NodeRouteing {

    private ScheduledExecutorService scheduler;
    private NetworkNode networkNode;
    private TransportLayer transportLayer;
    NodeRouteingImpl(int interval, NetworkNode networkNode1, TransportLayer transportLayer1){
       this.networkNode = networkNode1;
       this.transportLayer = transportLayer1;
       this.scheduler = Executors.newScheduledThreadPool(1);
       this.scheduler.scheduleAtFixedRate(()->{
           DistanceVectorMessage dvMessage = constructDistanceVectorMessage(networkNode,PING_MSG);
           try {
               this.floodDistanceVectorMsg(transportLayer,networkNode,dvMessage);
           } catch (IOException e) {
               throw new RuntimeException(e);
           }

       },0,interval,TimeUnit.MILLISECONDS);
       //todo: 检查哪个邻居节点和自己的连接断开了

    }
    public void shutdown(){
        this.scheduler.shutdown();
    }


    @Override
    public DistanceVectorMessage constructDistanceVectorMessage(NetworkNode networkNode, int msgType) {
        DistanceVectorMessage dvMessage = new DistanceVectorMessage();
        switch (msgType){
            case PING_MSG,PING_REPLY_MSG:
                dvMessage.setMsgType(msgType);
                dvMessage.setFromNodeId(networkNode.getNodeId());
                break;
            case PATH_DISTANCE_MSG:
                dvMessage.setFromNodeId(networkNode.getNodeId());
                dvMessage.setMsgType(msgType);
                PathCost tmpPathCost= new PathCost();
                for (Map.Entry<String, TableItem> entry : networkNode.getRouteTable().entrySet()) {
                    tmpPathCost.cost = entry.getValue().cost;
                    tmpPathCost.toNodeId = entry.getKey();
                    dvMessage.insertPathCost(tmpPathCost);
                }
                break;
            default:
                System.out.println("消息类型代码只为0，1, 2，请重新输入:");
                break;
        }
        return dvMessage;
    }

    @Override
    public int floodDistanceVectorMsg(TransportLayer transportLayer, NetworkNode networkNode, DistanceVectorMessage dvMessage) throws IOException {
        String encodedMsgStr = dvMessage.encode();
        //遍历邻居节点表，向邻居们发送消息。
        for (Map.Entry<String,Integer> entry:
             networkNode.getNeighbor().entrySet()) {
            //entry.getValue代表该节点是否为邻居，如果不是则跳过本次循环。
            if(entry.getValue() != 1) {continue;}
            //如果这个邻居节点就是自身，则跳过本次循环。
            else if (entry.getKey().equals(networkNode.getNodeId())) {continue;}
            // 如果没连上这个邻居节点，则跳过本次循环。
           // else if (networkNode.getIsNeighConnected(entry.getKey()) == 0){continue;}
            else {
                String destNodeId = entry.getKey();
                int destPort = networkNode.getNodeAddr().get(destNodeId);
                transportLayer.sendMsg(DEST_IP_ADDR,destPort,encodedMsgStr.getBytes());
            }
        }
        return 1;
    }

    @Override
    public int sendDistanceVectorMsg(String toNodeId, TransportLayer transportLayer, NetworkNode networkNode, DistanceVectorMessage dvMessage) throws IOException {
        String encodedStr = dvMessage.encode();
        int destPort = networkNode.getNodeAddr().get(toNodeId);
        transportLayer.sendMsg(DEST_IP_ADDR,destPort,encodedStr.getBytes());
        return 1;
    }
}
