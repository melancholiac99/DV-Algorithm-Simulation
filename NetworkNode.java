
import config.TableItem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyt
 * @date 23/11/29
 */
public class NetworkNode {


    /**当前节点id**/
    private  String nodeId;
    /**
     * 节点表，存储这个网络的节点id与节点端口信息，
     * 要让每个节点知道网络中端口的信息，否则无法进行通信。
     **/
    private Map<String,Integer> nodeAddr;
    /** 键是邻居节点ID，值代表是否为邻居**/
    private Map<String,Integer> neighbor;
    /**与邻居节点的距离，键是邻居节点，值是到邻居节点的距离**/
    private Map<String,Integer> neighborCost;
    private Map<String,Integer> isNeighConnected;
    /**路由表，键是目的节点id，值是TableItem类**/
    private Map<String, TableItem> routeTable;


    public NetworkNode() {
        this.nodeAddr = new HashMap<>(256);
        this.neighbor = new HashMap<>(256);
        this.neighborCost = new HashMap<>(256);
        this.isNeighConnected = new HashMap<>(256);
        this.routeTable = new HashMap<>();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Map<String, Integer> getNodeAddr() {
        return nodeAddr;
    }

    public Map<String, Integer> getNeighbor() {
        return neighbor;
    }

    public Map<String, Integer> getNeighborCost() {
        return neighborCost;
    }

    /**
     * 重写getNeighborCost方法
     * @param neighborId
     * @return
     */
    public int getNeighborCost(String neighborId) {
        return this.neighborCost.get(neighborId);
    }

    public void setNeighborCost(Map<String, Integer> neighborCost) {
        this.neighborCost = neighborCost;
    }

    public int getIsNeighConnected(String neighborNodeId) {
        return this.isNeighConnected.get(neighborNodeId);
    }

    public void setNeighConnected(String neighborNodeId) {
        this.isNeighConnected.put(neighborNodeId,1);
    }

    public Map<String, TableItem> getRouteTable() {
        return routeTable;
    }

    public void insertRouteTable(String toNodeId, String neighborId, int cost ) {
        this.routeTable.put(toNodeId, new TableItem(neighborId,cost));
    }


    /**
     * 读取端口配置文件，初始化节点
     * @param nodesFileIn 文件路径
     * @return
     */
    public int initNodeAddr(String nodesFileIn) {
        try (BufferedReader neighNodesIn = new BufferedReader(new FileReader(nodesFileIn))) {
            String line;
            while ((line = neighNodesIn.readLine()) != null) {
                //将读取的文件行按照空白格进行分割
                String[] parts = line.split("\\s+", 2);
                if (parts.length == 2) {
                    String key = parts[0];
                    int value = Integer.parseInt(parts[1].trim());
                    //初始化nodeAddr属性
                    this.nodeAddr.put(key, value);
                }
            }

        } catch (IOException e) {
            System.err.println(nodesFileIn + " not open.");
            System.exit(1);
        }

        return 1;
    }


    /**
     *展示节点表的信息
     */
    public void displayNodeAddr(){
        System.out.println("该网络中节点与端口的映射是：");
        //Map.Entry的作用就是更加方便的获取Map键值对
        for (Map.Entry<String,Integer> entry: nodeAddr.entrySet()) {
            System.out.println("节点名称: " + entry.getKey() + " 端口: " + entry.getValue());
        }
    }

    /**
     * 展示邻居路由表的信息
     */
    public void displayRoutingTable() {
        System.out.println();
        System.out.println("The routing table: ");
        for (Map.Entry<String, TableItem> entry: this.routeTable.entrySet()){
            TableItem tablePair = entry.getValue();
            System.out.println("The distance from this " + nodeId + " to " + entry.getKey() +
                    " is: " + tablePair.cost + "  via " + tablePair.neighbor );
        }
    }

    /**
     * 读取网络配置文件，获取邻居节点信息
     * @param neighborCostFileIn 邻居路由表
     * @return
     */
    public int initNeighborCost(String neighborCostFileIn){
        try(BufferedReader reader = new BufferedReader(new FileReader(neighborCostFileIn))){
            String line;
            while((line = reader.readLine())!=null){
                 String []parts = line.split("\\s+",3);
                 if(parts.length == 3){
                    String fromNodeId = parts[0];
                    String toNodeId = parts[1];
                    int costToNeighbor = Integer.parseInt(parts[2].trim());
                    if(this.nodeId.equals(fromNodeId)){
                        this.neighbor.put(toNodeId,1);
                        this.neighborCost.put(toNodeId,costToNeighbor);
                        this.isNeighConnected.put(toNodeId,0);
                    }

                     if (this.nodeId.equals(toNodeId)) {
                         neighborCost.put(fromNodeId, costToNeighbor);
                         isNeighConnected.put(fromNodeId, 0);
                         this.neighbor.put(fromNodeId, 1);
                     }
                 }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println(neighborCostFileIn + " not open.");
            throw new RuntimeException(e);
        }
        return 1;
    }



}
