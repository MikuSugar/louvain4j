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
        String input = "your_input_file.txt";
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
