package me.mikusugar.louvain;

import me.mikusugar.louvain.utils.DoubleArrayDisk;
import me.mikusugar.louvain.utils.IntArrayDisk;
import me.mikusugar.louvain.utils.LongArrayDisk;
import org.apache.lucene.util.RamUsageEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 15:54
 */
public class Edge implements AutoCloseable
{
    private final DoubleArrayDisk edgeWeights;

    private final IntArrayDisk edgeInfos;

    private final LongArrayDisk edgeInfos1;

    private static final Logger logger = LoggerFactory.getLogger(Edge.class);

    public Edge(long size)
    {
        int memoryBatchCount = 50_0000;
        int batchSize = 1000;
        logger.info("edge is applying for memory.");
        this.edgeInfos = new IntArrayDisk(size * 2, memoryBatchCount, batchSize, Constant.TEMP_DIR);
        logger.info("memory has been applied:{}", RamUsageEstimator.humanReadableUnits(edgeInfos.getMemoryUsage()));
        this.edgeWeights = new DoubleArrayDisk(size, memoryBatchCount, batchSize, Constant.TEMP_DIR);
        logger.info("memory has been applied:{}",
                RamUsageEstimator.humanReadableUnits(edgeInfos.getMemoryUsage() + edgeWeights.getMemoryUsage()));
        this.edgeInfos1 = new LongArrayDisk(size, memoryBatchCount, batchSize, Constant.TEMP_DIR);
        logger.info("memory usage:{}", RamUsageEstimator.humanReadableUnits(
                edgeInfos.getMemoryUsage() + edgeWeights.getMemoryUsage() + edgeInfos1.getMemoryUsage()));
    }

    @Override
    public void close()
    {
        edgeWeights.close();
        edgeInfos.close();
        edgeInfos1.close();
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
        return this.edgeInfos.get(idx * 2);
    }

    public int getRight(long idx)
    {
        return this.edgeInfos.get(idx * 2 + 1);
    }

    public long getNext(long idx)
    {
        return this.edgeInfos1.get(idx);
    }

    public double getWeight(long idx)
    {
        return this.edgeWeights.get(idx);
    }
}
