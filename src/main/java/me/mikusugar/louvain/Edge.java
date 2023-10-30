package me.mikusugar.louvain;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import org.apache.lucene.util.RamUsageEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 15:54
 */
public class Edge implements AutoCloseable
{
    private final DoubleBigArrayBigList edgeWeights;

    private final IntBigArrayBigList edgeInfos;

    private final LongBigArrayBigList edgeInfos1;

    private static final Logger logger = LoggerFactory.getLogger(Edge.class);

    public Edge(long size)
    {
        logger.info("edge is applying for memory.");
        this.edgeInfos = new IntBigArrayBigList(size * 2);
        init0(size * 2);
        logger.info("memory has been applied:{}", RamUsageEstimator.humanSizeOf(edgeInfos));
        this.edgeWeights = new DoubleBigArrayBigList(size);
        init1(size);
        logger.info("memory has been applied:{}", RamUsageEstimator.humanReadableUnits(
                RamUsageEstimator.sizeOf(edgeInfos) + RamUsageEstimator.sizeOf(edgeWeights)));
        this.edgeInfos1 = new LongBigArrayBigList(size);
        init2(size);
        logger.info("memory usage:{}", RamUsageEstimator.humanReadableUnits(
                RamUsageEstimator.sizeOf(edgeInfos) + RamUsageEstimator.sizeOf(edgeWeights) + RamUsageEstimator.sizeOf(
                        edgeInfos1)));
    }

    private void init0(long size)
    {
        int batchSize = 100_0000;
        IntArrayList list = new IntArrayList(batchSize);
        for (int i = 0; i < batchSize; i++)
        {
            list.add(0);
        }
        for (int i = 0; i < size / batchSize; i++)
        {
            this.edgeInfos.addAll(list);
        }
        list.clear();
        for (int i = 0; i < (size + batchSize) % batchSize; i++)
        {
            this.edgeInfos.add(0);
        }
    }

    private void init1(long size)
    {
        int batchSize = 100_0000;
        DoubleArrayList list = new DoubleArrayList(batchSize);
        for (int i = 0; i < batchSize; i++)
        {
            list.add(0);
        }
        for (int i = 0; i < size / batchSize; i++)
        {
            this.edgeWeights.addAll(list);
        }
        list.clear();
        for (int i = 0; i < (size + batchSize) % batchSize; i++)
        {
            this.edgeWeights.add(0);
        }
    }

    private void init2(long size)
    {
        int batchSize = 100_0000;
        LongArrayList list = new LongArrayList(batchSize);
        for (int i = 0; i < batchSize; i++)
        {
            list.add(0);
        }
        for (int i = 0; i < size / batchSize; i++)
        {
            this.edgeInfos1.addAll(list);
        }
        list.clear();
        for (int i = 0; i < (size + batchSize) % batchSize; i++)
        {
            this.edgeInfos1.add(0);
        }
    }

    @Override
    public void close()
    {
        edgeWeights.clear();
        edgeInfos.clear();
        edgeInfos1.clear();
    }

    public void setLeft(long idx, int leftValue)
    {
        this.edgeInfos.set(idx * 2, leftValue);
    }

    public void setRight(long idx, int rightValue)
    {
        this.edgeInfos.set(idx * 2 + 1, rightValue);
    }

    public void setNext(long idx, long nextValue)
    {
        this.edgeInfos1.set(idx, nextValue);
    }

    public void setWeight(long idx, double weight)
    {
        this.edgeWeights.set(idx, weight);
    }

    public int getLeft(long idx)
    {
        return this.edgeInfos.getInt(idx * 2);
    }

    public int getRight(long idx)
    {
        return this.edgeInfos.getInt(idx * 2 + 1);
    }

    public long getNext(long idx)
    {
        return this.edgeInfos1.getLong(idx);
    }

    public double getWeight(long idx)
    {
        return this.edgeWeights.getDouble(idx);
    }
}
