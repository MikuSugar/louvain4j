package me.mikusugar.louvain.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import org.apache.lucene.util.RamUsageEstimator;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.UUID;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/26 11:16
 */
public class LongArrayDisk implements AutoCloseable
{
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

    private final HTreeMap<Integer, long[]> diskMap;

    private final Int2ObjectLinkedOpenHashMap<long[]> memoryMap;

    public LongArrayDisk(long size, int memoryBatchCount, int batchSize, String tempDir)
    {
        this.length = size;
        this.memoryBatchCount = memoryBatchCount;
        this.batchSize = batchSize;

        this.db = DBMaker.fileDB(tempDir + "/" + UUID.randomUUID()).fileMmapEnableIfSupported().fileDeleteAfterClose()
                .make();
        this.diskMap = db.hashMap(UUID.randomUUID().toString(), Serializer.INTEGER, Serializer.LONG_ARRAY)
                .createOrOpen();
        this.memoryMap = new Int2ObjectLinkedOpenHashMap<>();

        //init
        int batches = (int)(size / batchSize);
        for (int i = 0; i < batches; i++)
        {
            final long[] value = new long[batchSize];
            diskMap.put(i, value);
            if (memoryMap.size() <= memoryBatchCount)
            {
                memoryMap.put(i, value);
            }
        }
        if (size % batchSize != 0)
        {
            diskMap.put(batches, new long[(int)((size + batchSize) % batchSize)]);
        }
    }

    public long get(long idx)
    {
        checkIdx(idx);

        final int key = (int)(idx / batchSize);
        final int index = (int)((idx + batchSize) % batchSize);
        long[] array = memoryMap.getAndMoveToLast(key);
        if (array == null)
        {
            array = diskMap.get(key);
            if (array == null)
            {
                throw new IllegalArgumentException(idx + " not found!");
            }
            memoryMap.putAndMoveToLast(key, array);
            if (memoryMap.size() > memoryBatchCount)
            {
                final int firstIntKey = memoryMap.firstIntKey();
                final long[] firstValue = memoryMap.removeFirst();
                diskMap.put(firstIntKey, firstValue);
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

    public void set(long idx, long value)
    {
        checkIdx(idx);
        final int key = (int)(idx / batchSize);
        final int index = (int)((idx + batchSize) % batchSize);
        long[] array = memoryMap.getAndMoveToLast(key);
        if (array == null)
        {
            array = diskMap.get(key);
            if (array == null)
            {
                throw new IllegalArgumentException(idx + " not found!");
            }
            array[index] = value;
            memoryMap.putAndMoveToLast(key, array);
            if (memoryMap.size() > memoryBatchCount)
            {
                final int firstIntKey = memoryMap.firstIntKey();
                final long[] firstValue = memoryMap.removeFirst();
                diskMap.put(firstIntKey, firstValue);
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
        diskMap.close();
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
                LongArrayDisk array = new LongArrayDisk(size, memoryBatchCount, batchSize,
                        "/Users/mikusugar/Downloads")
        )
        {
            for (int i = 0; i < array.length; i++)
            {
                array.set(i, i);
            }
            for (int i = 0; i < array.length; i++)
            {
                if (array.get(i) != i)
                {
                    throw new IllegalArgumentException();
                }
            }
        }

    }

}
