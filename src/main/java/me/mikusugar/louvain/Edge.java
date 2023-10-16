package me.mikusugar.louvain;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 15:54
 */
public class Edge
{
    //left <------ right
    
    int left;

    int right;

    /**
     * next neighbor index for node left
     */
    int next;

    /**
     * edge weight from right to left
     */
    double weight;
}
