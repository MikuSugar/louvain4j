package me.mikusugar.louvain;

import org.apache.lucene.util.RamUsageEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/25 15:09
 */
public class Node
{
    public final int size;

    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    public Node(int size)
    {
        logger.info("node is applying for memory.");
        this.size = size;
        this.infos1 = new int[size * 5];
        logger.info("memory has been applied:{}", RamUsageEstimator.humanSizeOf(infos1));
        this.infos2 = new double[size * 4];
        logger.info("memory has been applied:{}", RamUsageEstimator.humanReadableUnits(
                RamUsageEstimator.sizeOf(infos1) + RamUsageEstimator.sizeOf(infos2)));
        this.infos3 = new long[size];
        logger.info("memory usage:{}", RamUsageEstimator.humanReadableUnits(
                RamUsageEstimator.sizeOf(infos1) + RamUsageEstimator.sizeOf(infos2) + RamUsageEstimator.sizeOf(
                        infos3)));
    }

    private final int[] infos1;

    private final double[] infos2;

    private final long[] infos3;

    /**
     * 社区内的节点个数
     */
    public void setCount(int idx, int count)
    {
        infos1[idx * 5] = count;
    }

    /**
     * 社区内的节点个数
     */
    public int getCount(int idx)
    {
        return infos1[idx * 5];
    }

    /**
     * 节点归属社区的代表节点ID
     */
    public void setClsid(int idx, int clsid)
    {
        infos1[idx * 5 + 1] = clsid;
    }

    /**
     * 节点归属社区的代表节点ID
     */
    public int getClsid(int idx)
    {
        return infos1[idx * 5 + 1];
    }

    /**
     * 步骤1迭代中下一个属于同一个临时社区的节点
     */
    public void setNext(int idx, int next)
    {
        infos1[idx * 5 + 2] = next;
    }

    /**
     * 步骤1迭代中下一个属于同一个临时社区的节点
     */
    public int getNext(int idx)
    {
        return infos1[idx * 5 + 2];
    }

    /**
     * 步骤1迭代中上一个属于同一个临时社区的节点
     */
    public void setPrev(int idx, int prev)
    {
        infos1[idx * 5 + 3] = prev;
    }

    /**
     * 步骤1迭代中上一个属于同一个临时社区的节点
     */
    public int getPrev(int idx)
    {
        return infos1[idx * 5 + 3];
    }

    /**
     * 属于同一个社区的，除代表节点外的第一个节点，该节点有步骤2 社区折叠的时候生成
     */
    public void setFirst(int idx, int first)
    {
        infos1[idx * 5 + 4] = first;
    }

    /**
     * 属于同一个社区的，除代表节点外的第一个节点，该节点有步骤2 社区折叠的时候生成
     */
    public int getFirst(int idx)
    {
        return infos1[idx * 5 + 4];
    }

    /**
     * 节点邻居链表的第一个指针，该链表下的所有left，都是本节点自己
     */
    public void setEindex(int idx, long eindex)
    {
        infos3[idx] = eindex;
    }

    /**
     * 节点邻居链表的第一个指针，该链表下的所有left，都是本节点自己
     */
    public long getEindex(int idx)
    {
        return infos3[idx];
    }

    /**
     * 稳定社区内部节点之间的互相连接权重之和
     */
    public void setKin(int idx, double kin)
    {
        infos2[idx * 4] = kin;
    }

    /**
     * 稳定社区内部节点之间的互相连接权重之和
     */
    public double getKin(int idx)
    {
        return infos2[idx * 4];
    }

    /**
     * 稳定社区外部，指向自己社区的权重之和
     */
    public void setKout(int idx, double kout)
    {
        infos2[idx * 4 + 1] = kout;
    }

    /**
     * 稳定社区外部，指向自己社区的权重之和
     */
    public double getKout(int idx)
    {
        return infos2[idx * 4 + 1];
    }

    /**
     * 临时社区内部节点之间的互相连接权重之和
     */
    public void setClskin(int idx, double clskin)
    {
        infos2[idx * 4 + 2] = clskin;
    }

    /**
     * 临时社区内部节点之间的互相连接权重之和
     */
    public double getClskin(int idx)
    {
        return infos2[idx * 4 + 2];
    }

    /**
     * 稳定社区所有内外部指向自己的连接权重之和
     */
    public void setClstot(int idx, double clstot)
    {
        infos2[idx * 4 + 3] = clstot;
    }

    /**
     * 稳定社区所有内外部指向自己的连接权重之和
     */
    public double getClstot(int idx)
    {
        return infos2[idx * 4 + 3];
    }

}
