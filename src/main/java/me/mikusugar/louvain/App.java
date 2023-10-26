package me.mikusugar.louvain;

import java.io.IOException;

/**
 * run app
 * @author mikusugar
 * @version 1.0, 2023/10/16 16:01
 */
public class App
{
    public static void main(String[] args) throws IOException
    {
        String edgeFile = args[0];
        String vertexFile = args.length >= 2 ? args[1] : null;
        Louvain louvain = LouvainAlgorithm.createLouvain(edgeFile, vertexFile);
        LouvainAlgorithm.learnLouvain(louvain);
        // Do something with the results
        LouvainAlgorithm.saveLouvain(louvain, edgeFile + "_out.txt");
        LouvainAlgorithm.clear(louvain);
    }
}
