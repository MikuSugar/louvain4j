package me.mikusugar.louvain;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.mikusugar.louvain.utils.ProgressTracker;
import org.apache.lucene.util.RamUsageEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author mikusugar
 * @version 1.0, 2023/10/16 15:58
 */
public class LouvainAlgorithm
{
    private static final Logger logger = LoggerFactory.getLogger(LouvainAlgorithm.class);

    private static void mallocLouvain(Louvain lv)
    {
        lv.cindex = new int[lv.clen];
        Arrays.fill(lv.cindex, -1);
        lv.nodes = new Node[lv.clen];
        for (int i = 0; i < lv.clen; i++)
        {
            lv.nodes[i] = new Node();
            lv.nodes[i].eindex = -1;
        }
        lv.edges = new Edge[lv.elen];
    }

    private static void initNode(Louvain lv, int i, double weight)
    {
        if (lv.cindex[i] == -1)
        {
            lv.cindex[i] = i;
            lv.nodes[i].count = 1;
            lv.nodes[i].kin = 0;
            lv.nodes[i].clskin = 0;
            lv.nodes[i].clsid = i;
            lv.nodes[i].first = -1;
            lv.nodes[i].prev = -1;
            lv.nodes[i].next = -1;
        }
        lv.nodes[i].kout += weight;
        lv.nodes[i].clstot += weight;
    }

    private static void linkEdge(Louvain lv, int l, int r, int ei, double weight)
    {
        lv.edges[ei] = new Edge();
        lv.edges[ei].left = l;
        lv.edges[ei].right = r;
        lv.edges[ei].weight = weight;
        lv.edges[ei].next = lv.nodes[l].eindex;
        lv.nodes[l].eindex = ei;
    }

    public static Louvain createLouvain(String input) throws IOException
    {
        Int2IntMap hs = new Int2IntOpenHashMap();
        long fileCount;
        try (Stream<String> s = Files.lines(Paths.get(input)))
        {
            fileCount = s.count();
            logger.info("file line count:{}", fileCount);
        }
        Louvain lv = new Louvain();
        lv.input = input;
        lv.fileCount = fileCount;
        int l = 0, ei = 0;

        int cnt = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(input)))
        {
            ProgressTracker preReadTracker = new ProgressTracker(fileCount);
            preReadTracker.start();
            String line;
            while ((line = reader.readLine()) != null)
            {
                cnt++;
                if (cnt % 10_0000 == 0)
                {
                    preReadTracker.setCurrent(cnt);
                    logger.info("pre-read progress:{},etc:{}", preReadTracker.getHumanFriendlyProgress(),
                            preReadTracker.getHumanFriendlyEtcTime());
                }
                if (line.startsWith("#"))
                {
                    continue;
                }
                String[] tokens = line.trim().split("[\\s　]+");
                final int v1 = Integer.parseInt(tokens[0]);
                final int v2 = Integer.parseInt(tokens[1]);
                if (!hs.containsKey(v1))
                {
                    hs.put(v1, hs.size());
                }
                if (!hs.containsKey(v2))
                {
                    hs.put(v2, hs.size());
                }
                l++;
            }
            logger.info("pre-read ok!,take time:{}", preReadTracker.getHumanFriendlyElapsedTime());
            lv.clen = hs.size();
            lv.elen = l * 2;
            lv.nlen = lv.clen;
            lv.olen = lv.elen;
            mallocLouvain(lv);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(input)))
        {
            String line;
            cnt = 0;
            ProgressTracker initTracker = new ProgressTracker(fileCount);
            initTracker.start();
            while ((line = reader.readLine()) != null)
            {
                cnt++;
                if (cnt % 10_0000 == 0)
                {
                    initTracker.setCurrent(cnt);
                    logger.info("init progress:{},etc:{}", initTracker.getHumanFriendlyProgress(),
                            initTracker.getHumanFriendlyEtcTime());
                }
                if (line.startsWith("#"))
                {
                    continue;
                }
                String[] tokens = line.trim().split("[\\s　]+");
                int left = hs.get(Integer.parseInt(tokens[0]));
                int right = hs.get(Integer.parseInt(tokens[1]));
                double weight = tokens.length == 3 ? Double.parseDouble(tokens[2]) : 1d;
                lv.sumw += weight;
                initNode(lv, left, weight);
                initNode(lv, right, weight);
                linkEdge(lv, left, right, ei, weight);
                ei++;
                linkEdge(lv, right, left, ei, weight);
                ei++;
            }
            logger.info("init success. take time:{}", initTracker.getHumanFriendlyElapsedTime());
            logger.info("memory usage nodes:{},edges:{}", RamUsageEstimator.humanSizeOf(lv.nodes),
                    RamUsageEstimator.humanSizeOf(lv.edges));
            logger.info("total memory usage:{}", RamUsageEstimator.humanSizeOf(lv));
            return lv;
        }
    }

    private static void addNodeToComm(Louvain lv, int id, int cid, double weight)
    {
        lv.nodes[id].clsid = cid;
        lv.nodes[id].next = lv.nodes[cid].next;
        lv.nodes[cid].next = id;
        lv.nodes[id].prev = cid;
        if (lv.nodes[id].next != -1)
        {
            lv.nodes[lv.nodes[id].next].prev = id;
        }
        lv.nodes[cid].count += lv.nodes[id].count;
        lv.nodes[cid].clstot += lv.nodes[id].clstot;
        lv.nodes[cid].clskin += lv.nodes[id].kin + 2 * weight;
    }

    private static void removeNodeFromComm(Louvain lv, int id, double weight)
    {
        int cid = lv.nodes[id].clsid;
        int prev, next;
        if (cid != id)
        {
            prev = lv.nodes[id].prev;
            next = lv.nodes[id].next;
            lv.nodes[prev].next = next;
            if (next != -1)
            {
                lv.nodes[next].prev = prev;
            }
            lv.nodes[cid].count -= lv.nodes[id].count;
            lv.nodes[cid].clstot -= lv.nodes[id].clstot;
            lv.nodes[cid].clskin -= lv.nodes[id].kin + 2 * weight;
        }
        else
        {
            next = lv.nodes[id].next;
            cid = next;
            if (next != -1)
            {
                lv.nodes[next].prev = -1;
                lv.nodes[next].clsid = next;
                while (-1 != (next = lv.nodes[next].next))
                {
                    lv.nodes[cid].count += lv.nodes[next].count;
                    lv.nodes[next].clsid = cid;
                }
                lv.nodes[cid].clstot = lv.nodes[id].clstot - lv.nodes[id].kin - lv.nodes[id].kout;
                lv.nodes[cid].clskin = lv.nodes[id].clskin - lv.nodes[id].kin - 2 * weight;
                lv.nodes[id].count -= lv.nodes[cid].count;
                lv.nodes[id].clskin = lv.nodes[id].kin;
                lv.nodes[id].clstot -= lv.nodes[cid].clstot;
            }
        }
    }

    private static int firstStage(Louvain lv)
    {
        int cct, idc, maxId, stageTwo;
        int[] ids;
        double[] weight;

        ids = new int[lv.nlen];
        weight = new double[lv.nlen];
        Arrays.fill(ids, -1);
        stageTwo = 0;
        do
        {
            cct = 0;
            for (int i = 0; i < lv.clen; i++)
            {
                int ci = lv.cindex[i];
                double kv = lv.nodes[ci].kin + lv.nodes[ci].kout;
                int cid = lv.nodes[ci].clsid;
                int ei = lv.nodes[ci].eindex;
                idc = 0;
                while (ei != -1)
                {
                    int wi = lv.edges[ei].right;
                    double wei = lv.edges[ei].weight;
                    int wci = lv.nodes[wi].clsid;
                    weight[wci] += wei;
                    ids[idc++] = wci;
                    ei = lv.edges[ei].next;
                }
                double maxInWei = 0;
                double cwei = 0;
                double maxDeltaQ = 0;
                maxId = -1;
                for (int j = 0; j < idc; j++)
                {
                    if (weight[ids[j]] > 0.0)
                    {
                        double deltaQ;
                        if (cid == ids[j])
                        {
                            deltaQ = weight[ids[j]] - kv * (lv.nodes[ids[j]].clstot - kv) / lv.sumw;
                            cwei = weight[ids[j]];
                        }
                        else
                        {
                            deltaQ = weight[ids[j]] - kv * lv.nodes[ids[j]].clstot / lv.sumw;
                        }
                        if (deltaQ > maxDeltaQ)
                        {
                            maxDeltaQ = deltaQ;
                            maxId = ids[j];
                            maxInWei = weight[ids[j]];
                        }
                        weight[ids[j]] = 0.0;
                    }
                }
                if (maxDeltaQ > 0.0 && maxId != cid)
                {
                    if (maxId == -1)
                    {
                        logger.error("This cannot be, something must be wrong");
                        return 0;
                    }
                    removeNodeFromComm(lv, ci, cwei);
                    addNodeToComm(lv, ci, maxId, maxInWei);
                    cct += 1;
                    stageTwo = 1;
                }
            }
            logger.info("One iteration inner first stage, changed nodes: " + cct);
        } while (cct * 1d / lv.clen >= 0.001);
        return stageTwo;
    }

    private static void secondStage(Louvain lv)
    {
        int tclen = 0;
        int telen = 0;
        for (int i = 0; i < lv.clen; i++)
        {
            int ci = lv.cindex[i];
            if (lv.nodes[ci].clsid == ci)
            {
                lv.cindex[tclen++] = ci;
                int next = lv.nodes[ci].next;
                int first = lv.nodes[ci].first;
                if (first != -1)
                {
                    while (lv.nodes[first].next != -1)
                    {
                        first = lv.nodes[first].next;
                    }
                    lv.nodes[first].next = next;
                }
                else
                {
                    lv.nodes[ci].first = next;
                }
                if (next != -1)
                {
                    lv.nodes[next].prev = first;
                }
                lv.nodes[ci].next = -1;
                lv.nodes[ci].prev = -1;
            }
        }
        lv.clen = tclen;
        for (int i = 0; i < lv.clen; i++)
        {
            int ci = lv.cindex[i];
            lv.nodes[ci].kin = lv.nodes[ci].clskin;
            lv.nodes[ci].kout = lv.nodes[ci].clstot - lv.nodes[ci].kin;
            lv.nodes[ci].eindex = -1;
        }
        for (int i = 0; i < lv.elen; i++)
        {
            int l = lv.edges[i].left;
            int r = lv.edges[i].right;
            double w = lv.edges[i].weight;
            int lcid = lv.nodes[l].clsid;
            int rcid = lv.nodes[r].clsid;
            if (lcid != rcid)
            {
                lv.edges[telen] = new Edge();
                lv.edges[telen].left = lcid;
                lv.edges[telen].right = rcid;
                lv.edges[telen].weight = w;
                lv.edges[telen].next = lv.nodes[lcid].eindex;
                lv.nodes[lcid].eindex = telen++;
            }
        }
        lv.elen = telen;
    }

    public static void learnLouvain(Louvain lv)
    {
        int it = 0;
        while (firstStage(lv) > 0)
        {
            secondStage(lv);
            logger.info("it: {},community count: {} after one pass,current modularity: {}", ++it, lv.clen,
                    calcModularity(lv));
        }
    }

    public static void saveLouvain(Louvain lv, String out) throws IOException
    {
        IntSet hs = new IntOpenHashSet(lv.nodes.length);
        try (
                BufferedReader reader = new BufferedReader(new FileReader(lv.input));
                BufferedWriter writer = new BufferedWriter(new FileWriter(out))
        )
        {
            ProgressTracker tracker = new ProgressTracker(lv.fileCount);
            tracker.start();
            int cnt = 0;
            writer.write("node,CommunityID,CommunityCount,kin,kout");
            writer.write(System.lineSeparator());
            while (reader.ready())
            {
                cnt++;
                if (cnt % 10_0000 == 0)
                {
                    tracker.setCurrent(cnt);
                    logger.info("write progress:{},etc:{}", tracker.getHumanFriendlyProgress(),
                            tracker.getHumanFriendlyEtcTime());
                }
                final String line = reader.readLine();
                if (line.startsWith("#"))
                {
                    continue;
                }
                final String[] strs = line.trim().split("[\\s　]+");
                final int nodeLeft = Integer.parseInt(strs[0]);
                final int nodeRight = Integer.parseInt(strs[1]);
                writeVertex(lv, hs, writer, nodeLeft);
                writeVertex(lv, hs, writer, nodeRight);
            }
            logger.info("write success,take time:{}", tracker.getHumanFriendlyElapsedTime());
        }
    }

    private static void writeVertex(Louvain lv, IntSet hs, BufferedWriter writer, int rawNode) throws IOException
    {
        if (!hs.contains(rawNode))
        {
            final int nodeLeftId = hs.size();
            final int clusterId = find(nodeLeftId, lv.nodes);
            hs.add(rawNode);
            writer.write(rawNode + "," + clusterId + "," + lv.nodes[clusterId].count);
            writer.write("," + lv.nodes[clusterId].kin);
            writer.write("," + lv.nodes[clusterId].kout);
            writer.write(System.lineSeparator());
        }
    }

    private static int find(int idx, Node[] nodes)
    {
        if (nodes[idx].clsid == idx)
        {
            return idx;
        }
        return nodes[idx].clsid = find(nodes[idx].clsid, nodes);
    }

    public static void clear(Louvain lv)
    {
        if (lv != null)
        {
            lv.cindex = null;
            lv.nodes = null;
            lv.edges = null;
        }
    }

    public static double calcModularity(Louvain lv)
    {
        double modularity = 0.0;
        for (int k = 0; k < lv.clen; k++)
        {
            int i = lv.cindex[k];
            modularity += lv.nodes[i].kin / (2 * lv.sumw) - Math.pow(lv.nodes[i].clstot / (2 * lv.sumw), 2);
        }
        return modularity;
    }

}
