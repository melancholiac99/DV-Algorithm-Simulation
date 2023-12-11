package config;

/**
 * 邻居表表项
 * @author zyt
 */
public class TableItem {
        /**经由的邻居节点**/
        public String neighbor;
        /**到达目的节点的距离cost**/
        public int cost;

    public TableItem(String neighbor, int cost) {
        this.neighbor = neighbor;
        this.cost = cost;
    }
}
