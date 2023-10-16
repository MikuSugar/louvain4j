package me.mikusugar.louvain;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 15:51
 */
public class Node
{
    /**
     * 社区内的节点个数
     */
    public int count;

    /**
     * 节点归属社区的代表节点ID
     */
    public int clsid;

    /**
     * 步骤1迭代中下一个属于同一个临时社区的节点
     */
    public int next;

    /**
     * 步骤1迭代中上一个属于同一个临时社区的节点
     */
    public int prev;

    /**
     * 属于同一个社区的，除代表节点外的第一个节点，该节点有步骤2 社区折叠的时候生成
     */
    public int first;

    /**
     * 节点邻居链表的第一个指针，该链表下的所有left，都是本节点自己
     */
    public int eindex;

    /**
     * 稳定社区内部节点之间的互相连接权重之和
     */
    public double kin;

    /**
     * 稳定社区外部，指向自己社区的权重之和
     */
    public double kout;

    /**
     * 临时社区内部节点之间的互相连接权重之和
     */
    public double clskin;

    /**
     * 稳定社区所有内外部指向自己的连接权重之和
     */
    public double clstot;
}
