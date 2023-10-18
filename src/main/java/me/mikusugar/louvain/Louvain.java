package me.mikusugar.louvain;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 15:57
 */
public class Louvain
{
    /**
     * 社区的个数
     */
    public int clen;

    public int elen;

    public int nlen;

    public int olen;

    /**
     * 社区代表点索引
     */
    public int[] cindex;

    /**
     * 图的权重和
     */
    public double sumw;

    /**
     * 点集合
     */
    public Node[] nodes;

    public Edge[] edges;

    public String input;
}
