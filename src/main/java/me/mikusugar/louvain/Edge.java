package me.mikusugar.louvain;

import it.unimi.dsi.fastutil.doubles.DoubleBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 15:54
 */
public class Edge
{
    private final DoubleBigArrayBigList edgeWeights;

    private final IntBigArrayBigList edgeInfos;

    public Edge(long size)
    {
        this.edgeInfos = new IntBigArrayBigList(size * 3);
        for (int i = 0; i < size * 3; i++)
        {
            edgeInfos.add(0);
        }
        this.edgeWeights = new DoubleBigArrayBigList(size);
        for (int i = 0; i < size; i++)
        {
            edgeWeights.add(0d);
        }
    }

    public void setLeft(long idx, int leftValue)
    {
        this.edgeInfos.set(idx * 3, leftValue);
    }

    public void setRight(long idx, int rightValue)
    {
        this.edgeInfos.set(idx * 3 + 1, rightValue);
    }

    public void setNext(long idx, int nextValue)
    {
        this.edgeInfos.set(idx * 3 + 2, nextValue);
    }

    public void setWeight(long idx, double weight)
    {
        this.edgeWeights.set(idx, weight);
    }

    public int getLeft(long idx)
    {
        return this.edgeInfos.getInt(idx * 3);
    }

    public int getRight(long idx)
    {
        return this.edgeInfos.getInt(idx * 3 + 1);
    }

    public int getNext(long idx)
    {
        return this.edgeInfos.getInt(idx * 3 + 2);
    }

    public double getWeight(long idx)
    {
        return this.edgeWeights.getDouble(idx);
    }
}
