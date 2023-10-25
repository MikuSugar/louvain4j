package me.mikusugar.louvain;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 15:54
 */
public class Edge
{
    private final double[] edgeWeights;

    private final int[] edgeInfos;

    public Edge(int size)
    {
        this.edgeWeights = new double[size];
        this.edgeInfos = new int[3 * size];
    }

    public void setLeft(int idx, int leftValue)
    {
        this.edgeInfos[idx * 3] = leftValue;
    }

    public void setRight(int idx, int rightValue)
    {
        this.edgeInfos[idx * 3 + 1] = rightValue;
    }

    public void setNext(int idx, int nextValue)
    {
        this.edgeInfos[idx * 3 + 2] = nextValue;
    }

    public void setWeight(int idx, double weight)
    {
        this.edgeWeights[idx] = weight;
    }

    public int getLeft(int idx)
    {
        return this.edgeInfos[idx * 3];
    }

    public int getRight(int idx)
    {
        return this.edgeInfos[idx * 3 + 1];
    }

    public int getNext(int idx)
    {
        return this.edgeInfos[idx * 3 + 2];
    }

    public double getWeight(int idx)
    {
        return this.edgeWeights[idx];
    }
}
