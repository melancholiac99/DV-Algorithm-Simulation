import config.PathCost;
import utils.TransportLayer;

import java.io.IOException;

import static config.Constant.PATH_DISTANCE_MSG;

/**
 * 距离矢量算法的实现
 * @author zyt
 */
public final class  DistanceVectorAlgorithm {
    public static void DVAlgorithm(NetworkNode networkNode, DistanceVectorMessage dvMessage, NodeRouteingImpl nodeRouteing,TransportLayer transportLayer) throws IOException {

        for (PathCost element : dvMessage.getToNodes()) {
            //如果目的节点正好为当前节点，则跳过本次循环
            if(element.toNodeId.equals(networkNode.getNodeId())){continue;}
            int newPathCost = element.cost + networkNode.getNeighborCost(dvMessage.getFromNodeId());
            //如果当前节点的邻居路由表没有到达目的节点的路由信息，则直接在表中添加项。
            if(!networkNode.getRouteTable().containsKey(element.toNodeId)){
                networkNode.insertRouteTable(element.toNodeId, dvMessage.getFromNodeId(), newPathCost);
                networkNode.displayRoutingTable();
                //向邻居节点发送更新后的路由信息。
                DistanceVectorMessage updateMsg = nodeRouteing.constructDistanceVectorMessage(networkNode,PATH_DISTANCE_MSG);
                nodeRouteing.floodDistanceVectorMsg(transportLayer,networkNode,updateMsg);
            }
            else{//用BF算法计算最新的距离
                int oldPathCost = networkNode.getRouteTable().get(element.toNodeId).cost;
                if(newPathCost < oldPathCost){
                    networkNode.insertRouteTable(element.toNodeId, dvMessage.getFromNodeId(), newPathCost);
                    networkNode.displayRoutingTable();
                    //向邻居节点发送更新后的路由信息。
                    DistanceVectorMessage updateMsg = nodeRouteing.constructDistanceVectorMessage(networkNode,PATH_DISTANCE_MSG);
                    nodeRouteing.floodDistanceVectorMsg(transportLayer,networkNode,updateMsg);
                }
            }
        }
    }
}
