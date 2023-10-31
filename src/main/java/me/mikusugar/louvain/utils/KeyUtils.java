package me.mikusugar.louvain.utils;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/10/31 11:04
 */
public class KeyUtils
{
    public static long toRelationId(int sourceId, int targetId)
    {
        return ((long)sourceId << 32) | (targetId & 0xFFFFFFFFL);
    }

    public static int getSourceIdFromRelationId(long relationId)
    {
        return (int)(relationId >> 32);
    }

    public static int getTargetIdFromRelationId(long relationId)
    {
        return (int)relationId;
    }
}
