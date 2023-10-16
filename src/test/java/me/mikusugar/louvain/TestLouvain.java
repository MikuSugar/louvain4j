package me.mikusugar.louvain;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 16:17
 */
public class TestLouvain
{
    public static String getResourcePath()
    {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        return resourceDirectory.toFile().getAbsolutePath();
    }

    @Test
    public void testP2P31() throws IOException
    {
        final String input = getResourcePath() + "/" + "p2p-31.e";
        Louvain louvain = LouvainAlgorithm.createLouvain(input);
        if (louvain != null)
        {
            LouvainAlgorithm.learnLouvain(louvain);
            // Do something with the results
            LouvainAlgorithm.saveLouvain(louvain);
            LouvainAlgorithm.clear(louvain);
        }
    }
}
