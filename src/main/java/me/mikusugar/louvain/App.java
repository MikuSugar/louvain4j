package me.mikusugar.louvain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * run app
 * @author mikusugar
 * @version 1.0, 2023/10/16 16:01
 */
public class App
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException
    {
        logger.info("args:{}", Arrays.toString(args));
        Louvain louvain = null;
        final String edgeFile = args[0];
        logger.info("edgeFile:{}", edgeFile);
        final String vertexFile = args.length >= 2 ? args[1] : null;
        logger.info("vertexFile:{}", vertexFile);
        final long edgeFileCount = args.length >= 3 ? Long.parseLong(args[2]) : -1;
        logger.info("edgeFileCount:{}", edgeFileCount);

        try
        {
            louvain = LouvainAlgorithm.createLouvain(edgeFile, vertexFile, edgeFileCount);
            LouvainAlgorithm.learnLouvain(louvain);
            LouvainAlgorithm.saveLouvain(louvain, edgeFile + "_out.txt");
        }
        finally
        {
            LouvainAlgorithm.clear(louvain);
        }

    }
}
