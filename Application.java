import utils.TransportLayer;
import utils.UserCmd;

import java.io.IOException;

import static config.Constant.*;

/**
 * @author 86130
 */
public class Application {
    public static void main(String[] args) {
        try {
            byte[] userCmd = new byte[MAX_CMD_LEN];
            NetworkNode networkNode = new NetworkNode();
            networkNode.setNodeId(args[0]);
            System.out.println("该节点id为" + networkNode.getNodeId());
            networkNode.initNodeAddr(args[1]);
            networkNode.displayNodeAddr();
            networkNode.initNeighborCost(args[2]);
            //byte[] receivedBuf = new byte[256];
            // int byteReceived = 0;


            int selfPort = networkNode.getNodeAddr().get(args[0]);

            TransportLayer transportLayer = new TransportLayer(SRC_IP_ADDR, selfPort);

            NodeRouteingImpl nodeRouteing = new NodeRouteingImpl(INTERVAL, networkNode, transportLayer);

            while (true) {
                if (UserCmd.readUserCmd(userCmd, MAX_CMD_LEN) == 1) {
                    String str = new String(userCmd);
                    if (str.equals("exit")) {
                        System.out.println("退出程序");
                        System.exit(0);
                    } else if (str.equals("disp_rt")) {
                        System.out.println("展示路由表");
                        networkNode.displayRoutingTable();
                    }
                    //todo:添加其他指令，比如删除某节点。
                }
                String receivedStr = transportLayer.getMsg(0);
                DistanceVectorMessage dvMessage = new DistanceVectorMessage();
                dvMessage.decode(receivedStr);
                String fromNodeId = dvMessage.getFromNodeId();
                // 我们假定网络节点状态不会发生变化
                switch (dvMessage.getMsgType()) {
                    case PING_MSG:
                     //   networkNode.setNeighConnected(fromNodeId);
                        DistanceVectorMessage replyMsg = nodeRouteing.constructDistanceVectorMessage(networkNode, PING_REPLY_MSG);
                        nodeRouteing.sendDistanceVectorMsg(fromNodeId, transportLayer, networkNode, replyMsg);
                        //处理未连接的节点，此时我简单的将未连接和不存在路径归为一类
                        if (networkNode.getIsNeighConnected(fromNodeId) == 0) {
                            int cost = networkNode.getNeighborCost().get(fromNodeId);
                            if (!networkNode.getRouteTable().containsKey(fromNodeId)) {
                                networkNode.insertRouteTable(fromNodeId, fromNodeId, cost);
                                networkNode.displayRoutingTable();
                            } else {
                                if (cost != networkNode.getRouteTable().get(fromNodeId).cost) {
                                    //todo:处理当到达邻居节点的距离发生变化的情况
                                }

                            }
                            // 如果未连接，则说明路由信息可能发生变化, 发送距离路径信息
                            DistanceVectorMessage pathMsg = nodeRouteing.constructDistanceVectorMessage(networkNode, PATH_DISTANCE_MSG);
                            nodeRouteing.floodDistanceVectorMsg(transportLayer, networkNode, pathMsg);

                        }
                        // 将该节点设置成已连接邻居
                        networkNode.setNeighConnected(fromNodeId);
                        networkNode.displayRoutingTable();
                        break;

                    case PING_REPLY_MSG:
                        if (networkNode.getIsNeighConnected(fromNodeId) == 0) {
                            int cost = networkNode.getNeighborCost().get(fromNodeId);
                            if (!networkNode.getRouteTable().containsKey(fromNodeId)) {
                                networkNode.insertRouteTable(fromNodeId, fromNodeId, cost);
                                networkNode.displayRoutingTable();
                            } else {
                                //todo:处理当到达邻居节点的距离发生变化的情况
                                if (cost != networkNode.getRouteTable().get(fromNodeId).cost) {

                                }
                            }
                            // 如果未连接，则说明路由信息可能发生变化, 发送距离路径信息
                            DistanceVectorMessage pathMsg = nodeRouteing.constructDistanceVectorMessage(networkNode, PATH_DISTANCE_MSG);
                            nodeRouteing.floodDistanceVectorMsg(transportLayer, networkNode, pathMsg);
                        }
                        // 将该节点设置成已连接邻居
                        networkNode.setNeighConnected(fromNodeId);
                        break;
                    case PATH_DISTANCE_MSG:
                        if (networkNode.getIsNeighConnected(fromNodeId) == 0) {
                            int cost = networkNode.getNeighborCost().get(fromNodeId);
                            if (!networkNode.getRouteTable().containsKey(fromNodeId)) {
                                networkNode.insertRouteTable(fromNodeId, fromNodeId, cost);
                                networkNode.displayRoutingTable();
                            } else {
                                //todo:处理当到达邻居节点的距离发生变化的情况
                                if (cost != networkNode.getRouteTable().get(fromNodeId).cost) {

                                }

                            }
                            // 如果未连接，则说明路由信息可能发生变化, 发送距离路径信息
                            DistanceVectorMessage pathMsg = nodeRouteing.constructDistanceVectorMessage(networkNode, PATH_DISTANCE_MSG);
                            nodeRouteing.floodDistanceVectorMsg(transportLayer, networkNode, pathMsg);
                            networkNode.setNeighConnected(fromNodeId);
                        }
                        DistanceVectorAlgorithm.DVAlgorithm(networkNode, dvMessage, nodeRouteing, transportLayer);
                        break;

                    default:
                        System.out.println("参数错误");
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
