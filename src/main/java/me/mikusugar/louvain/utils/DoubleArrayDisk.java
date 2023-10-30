package me.mikusugar.louvain.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import org.apache.lucene.util.RamUsageEstimator;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/26 11:16
 */
public class DoubleArrayDisk implements AutoCloseable
{
    private final static Logger logger = LoggerFactory.getLogger(DoubleArrayDisk.class);

    /**
     * 数组长度
     */
    public final long length;

    /**
     * 内存缓存的个数
     */
    private final int memoryBatchCount;

    /**
     * 每个批次的数组大小
     */
    private final int batchSize;

    private final DB db;

    private final IndexTreeList<double[]> diskList;

    private final Int2ObjectLinkedOpenHashMap<double[]> memoryMap;

    public DoubleArrayDisk(long size, int memoryBatchCount, int batchSize, String tempDir)
    {
        this.length = size;
        this.memoryBatchCount = memoryBatchCount;
        this.batchSize = batchSize;

        this.db = DBMaker.fileDB(tempDir + "/" + UUID.randomUUID() + "db.tmp").fileDeleteAfterClose()
                .fileMmapEnableIfSupported().make();
        this.diskList = db.indexTreeList(UUID.randomUUID().toString(), Serializer.DOUBLE_ARRAY).createOrOpen();
        this.memoryMap = new Int2ObjectLinkedOpenHashMap<>();

        //init
        int batches = (int)(size / batchSize);
        ProgressTracker initTracker = new ProgressTracker(batches);
        initTracker.start();
        for (int i = 0; i < batches; i++)
        {
            final double[] value = new double[batchSize];
            diskList.add(value);
            if (memoryMap.size() <= memoryBatchCount)
            {
                memoryMap.put(i, value);
            }
            initTracker.setCurrent(i + 1);
            if ((i + 1) % 10000 == 0)
            {
                logger.info("init progress:{},etc:{}", initTracker.getHumanFriendlyProgress(),
                        initTracker.getHumanFriendlyEtcTime());
            }
        }
        if (size % batchSize != 0)
        {
            diskList.add(new double[(int)((size + batchSize) % batchSize)]);
        }
    }

    public double get(long idx)
    {
        checkIdx(idx);

        final int key = (int)(idx / batchSize);
        final int index = (int)((idx + batchSize) % batchSize);
        double[] array = memoryMap.getAndMoveToLast(key);
        if (array == null)
        {
            array = diskList.get(key);
            if (array == null)
            {
                throw new IllegalArgumentException(idx + " not found!");
            }
            memoryMap.putAndMoveToLast(key, array);
            if (memoryMap.size() > memoryBatchCount)
            {
                final int firstIntKey = memoryMap.firstIntKey();
                final double[] firstValue = memoryMap.removeFirst();
                diskList.set(firstIntKey, firstValue);
            }
        }
        return array[index];
    }

    private void checkIdx(long idx)
    {
        if (idx < 0 || idx >= length)
        {
            throw new ArrayIndexOutOfBoundsException("cur idx is " + idx + ",but array length is " + length);
        }
    }

    public void set(long idx, double value)
    {
        checkIdx(idx);
        final int key = (int)(idx / batchSize);
        final int index = (int)((idx + batchSize) % batchSize);
        double[] array = memoryMap.getAndMoveToLast(key);
        if (array == null)
        {
            array = diskList.get(key);
            if (array == null)
            {
                throw new IllegalArgumentException(idx + " not found!");
            }
            array[index] = value;
            memoryMap.putAndMoveToLast(key, array);
            if (memoryMap.size() > memoryBatchCount)
            {
                final int firstIntKey = memoryMap.firstIntKey();
                final double[] firstValue = memoryMap.removeFirst();
                diskList.set(firstIntKey, firstValue);
            }
        }
        else
        {
            array[index] = value;
        }
    }

    @Override
    public void close()
    {
        diskList.clear();
        db.close();
        memoryMap.clear();
    }

    public long getMemoryUsage()
    {
        return RamUsageEstimator.sizeOf(memoryMap);
    }

    public static void main(String[] args)
    {
        int size = 10000_0000 + 4;
        int memoryBatchCount = 101;
        int batchSize = 43234;
        try (
                DoubleArrayDisk array = new DoubleArrayDisk(size, memoryBatchCount, batchSize,
                        "/Users/mikusugar/Downloads")
        )
        {
            for (int i = 0; i < array.length; i++)
            {
                array.set(i, i);
            }
            for (int i = 0; i < array.length; i++)
            {
                if (array.get(i) != i * 1.0)
                {
                    throw new IllegalArgumentException();
                }
            }
        }

    }

}
