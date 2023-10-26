package me.mikusugar.louvain;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 16:17
 */
public class TestLouvain
{
    private static final Logger logger = LoggerFactory.getLogger(TestLouvain.class);

    public static String getResourcePath()
    {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        return resourceDirectory.toFile().getAbsolutePath();
    }

    @Test
    public void testP2P31() throws IOException
    {
        final String input = getResourcePath() + "/" + "p2p-31.csv";
        Louvain louvain = LouvainAlgorithm.createLouvain(input);
        logger.info("pre-calculation modularity：" + LouvainAlgorithm.calcModularity(louvain));
        LouvainAlgorithm.learnLouvain(louvain);
        // Do something with the results
        LouvainAlgorithm.saveLouvain(louvain, input + "_out.txt");
        final double modularity = LouvainAlgorithm.calcModularity(louvain);
        logger.info("post-calculated modularity：" + modularity);
        assert modularity >= 0.3 && modularity <= 0.7;
        LouvainAlgorithm.clear(louvain);
    }

    @Test
    public void testClub() throws IOException
    {
        final String input = getResourcePath() + "/" + "club.txt";
        Louvain louvain = LouvainAlgorithm.createLouvain(input);

        logger.info("pre-calculation modularity：" + LouvainAlgorithm.calcModularity(louvain));
        LouvainAlgorithm.learnLouvain(louvain);
        // Do something with the results
        LouvainAlgorithm.saveLouvain(louvain, input + "_out.txt");
        final double modularity = LouvainAlgorithm.calcModularity(louvain);
        logger.info("post-calculated modularity：" + modularity);
        assert modularity >= 0.3 && modularity <= 0.7;
        LouvainAlgorithm.clear(louvain);
    }

    /**
     * 用于转换成 <a href="https://github.com/liuzhiqiangruc/dml/blob/master/cls/louvain.c">liuzhiqiangruc-dml-louvain.c</a> 程序可以执行的格式
     */
    @Test
    public void trans() throws IOException
    {
        final String input = getResourcePath() + "/" + "p2p-31.csv";
        String out = input + ".txt";
        try (
                BufferedReader reader = new BufferedReader(new FileReader(input));
                BufferedWriter writer = new BufferedWriter(new FileWriter(out))
        )
        {
            while (reader.ready())
            {
                final String[] strs = reader.readLine().split("[\\s　]+");
                writer.write(strs[0] + "\t");
                writer.write(strs[1] + "\t");
                writer.write(strs[2] + System.lineSeparator());
            }
        }

    }
}
