import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import config.*;
import org.junit.Test;

import static config.Constant.*;

/**
 * dv-message 实体类，模拟路由间传输的消息。
 * @author zyt
 * @date 23/12/2
 */

public class DistanceVectorMessage {

    private List<PathCost> toNodes;
    private int msgType;
    private String fromNodeId;

    public DistanceVectorMessage() {
        this.toNodes = new ArrayList<>();
    }

    public List<PathCost> getToNodes() {
        return toNodes;
    }

    public int getMsgType() {
        return msgType;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }


    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    /**
     * 不能直接插入，而是在插入元素前重新new一个对象，防止对toNodeCost进行修改后toNodes中的元素值发生变化。
     * (其实就是List.add接口的实现是让List的元素和传入该接口待插入的对象引用指向同一块堆区内存，导致若修改作为参数的对象引用
     * 会使得List中的元素发生变化)
     * @param toNodeCost
     */
    public void insertPathCost(PathCost toNodeCost) {
        PathCost pathCost = new PathCost();
        pathCost.cost = toNodeCost.cost;
        pathCost.toNodeId = toNodeCost.toNodeId;
        this.toNodes.add(pathCost);
    }

    /**
     * 将dv-message类型其中信息编码成字符串
     * @return String
     */

    public String encode() {
        //初始化一个待编码的StringBuilder对象。
        StringBuilder encodedStrBuilder = new StringBuilder(100);
        //将msgType转换为字符串，并用其初始化临时字符串。
        StringBuilder tmpStrBuilder = new StringBuilder(100);
        tmpStrBuilder.append(this.msgType);
        //将消息类型和源节点id追加到编码字符串中，使用'#'分隔
        encodedStrBuilder.append(tmpStrBuilder.toString()).append('#').append(this.fromNodeId);
        //根据不同消息类型选择不同编码方式
        switch (this.msgType) {
            case PATH_DISTANCE_MSG:
                int count = 0;
                for (PathCost node : this.toNodes) {
                    tmpStrBuilder.setLength(0);
                    tmpStrBuilder.append(node.cost);
                    //将字符串编码成形如：#'toNodeId'/'cost'/...#
                    if(count == 0) {
                        encodedStrBuilder.append('#').append(node.toNodeId).append('/').append(tmpStrBuilder.toString());
                    }
                    else {
                        encodedStrBuilder.append('/').append(node.toNodeId).append('/').append(tmpStrBuilder.toString());
                    }
                    count ++;
                }
                //在最后添加'#'表示结束。
                encodedStrBuilder.append('#');
                break;
            case PING_MSG, PING_REPLY_MSG:
                encodedStrBuilder.append('#');
                break;
            default:
                break;
        }
        //
        return encodedStrBuilder.toString();
    }

    /**
     * 将发来的字符串解码后初始化dv-message
     * (当String底层byte数组有多余0时，使用Pattern类匹配该字符串，会在最后错误匹配出一个空串，
     * 这个应该是Pattern类的一个bug)
     * @param receivedStr
     * @return
     */
    public int decode(String receivedStr) {
        //前两次匹配，匹配到的是ping和reply消息，第三次是PathCost消息
        int count = 0;
        PathCost pathCost = new PathCost();
        String[] split0 = exField.split(receivedStr);
        for (String s : split0) {
            count++;
            if (count == 1) {
                int msgType = Integer.parseInt(s, 10);
                setMsgType(msgType);
                continue;
            }
            if (count == 2) {
                setFromNodeId(s);
                //加这一行逻辑看似多此一举，实则精妙异常，巧妙地避开了String底层byte数组有多余0导致模式串错误匹配空串的问题。
                if (msgType != PATH_DISTANCE_MSG) {
                    return 1;
                }
                continue;
            }
            //将PathCost消息分割
            if (count > 2) {
                String[] split = exPathCost.split(s);
                //分奇偶初始化pathCost的属性
                for (int i = 0; i < split.length; i++) {
                    if (i % 2 == 0) {
                        pathCost.toNodeId = split[i];
                        continue;
                    }
                    if (i % 2 == 1) {
                        pathCost.cost = Integer.parseInt(split[i]);
                        this.insertPathCost(pathCost);
                    }
                }
                  return 1;
            }
        }
        return 1;
    }

    @Test
    public void testDecode() {

        decode("2#nodeB#nodeA/1/nodeC/2#");
    }

}





