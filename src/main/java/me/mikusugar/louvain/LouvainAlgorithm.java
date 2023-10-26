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
@SuppressWarnings ("DuplicatedCode")
public class LouvainAlgorithm
{
    private static final Logger logger = LoggerFactory.getLogger(LouvainAlgorithm.class);

    private static void mallocLouvain(Louvain lv)
    {
        lv.cindex = new int[lv.clen];
        Arrays.fill(lv.cindex, -1);
        lv.node = new Node(lv.clen);
        for (int i = 0; i < lv.clen; i++)
        {
            lv.node.setEindex(i, -1);
        }
        lv.edge = new Edge(lv.elen);
    }

    private static void initNode(Louvain lv, int i, double weight)
    {
        if (lv.cindex[i] == -1)
        {
            lv.cindex[i] = i;
            lv.node.setCount(i, 1);
            lv.node.setKin(i, 0);
            lv.node.setClskin(i, 0);
            lv.node.setClsid(i, i);
            lv.node.setFirst(i, -1);
            lv.node.setPrev(i, -1);
            lv.node.setNext(i, -1);
        }
        lv.node.setKout(i, lv.node.getKout(i) + weight);
        lv.node.setClstot(i, lv.node.getClstot(i) + weight);
    }

    private static void linkEdge(Louvain lv, int l, int r, long ei, double weight)
    {
        lv.edge.setLeft(ei, l);
        lv.edge.setRight(ei, r);
        lv.edge.setWeight(ei, weight);
        lv.edge.setNext(ei, lv.node.getEindex(l));
        lv.node.setEindex(l, ei);
    }

    public static Louvain createLouvain(String edgeFile) throws IOException
    {
        return createLouvain(edgeFile, null);
    }

    public static Louvain createLouvain(String edgeFile, String vertexFile) throws IOException
    {
        Int2IntMap hs = new Int2IntOpenHashMap();
        long edgeFileCount;
        logger.info("read edge file line count...");
        try (Stream<String> s = Files.lines(Paths.get(edgeFile)))
        {
            edgeFileCount = s.filter(line -> !line.startsWith("#")).count();
            logger.info("edge file line count:{}", edgeFileCount);
        }
        Louvain lv = new Louvain();
        lv.input = edgeFile;
        lv.fileCount = edgeFileCount;
        long l = 0, ei = 0;

        int cnt = 0;

        if (vertexFile == null)
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(edgeFile)))
            {
                ProgressTracker preReadTracker = new ProgressTracker(edgeFileCount);
                preReadTracker.start();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("#"))
                    {
                        continue;
                    }
                    cnt++;
                    if (cnt % 10_0000 == 0)
                    {
                        preReadTracker.setCurrent(cnt);
                        logger.info("pre-read progress:{},etc:{}", preReadTracker.getHumanFriendlyProgress(),
                                preReadTracker.getHumanFriendlyEtcTime());
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
                logger.info("id mapping memory usage:{}", RamUsageEstimator.humanSizeOf(hs));
                lv.clen = hs.size();
                lv.elen = l * 2;
                lv.nlen = lv.clen;
                lv.olen = lv.elen;
                mallocLouvain(lv);
            }
        }
        else
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(vertexFile)))
            {
                long vertexCount;
                logger.info("read vertex file line count...");
                try (Stream<String> s = Files.lines(Paths.get(vertexFile)))
                {
                    vertexCount = s.filter(line -> !line.startsWith("#")).count();
                    logger.info("vertex file line count:{}", vertexCount);
                }
                ProgressTracker preReadTracker = new ProgressTracker(vertexCount);
                preReadTracker.start();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("#"))
                    {
                        continue;
                    }
                    cnt++;
                    if (cnt % 10_0000 == 0)
                    {
                        preReadTracker.setCurrent(cnt);
                        logger.info("pre-read progress:{},etc:{}", preReadTracker.getHumanFriendlyProgress(),
                                preReadTracker.getHumanFriendlyEtcTime());
                    }
                    final int v1 = Integer.parseInt(line);
                    if (!hs.containsKey(v1))
                    {
                        hs.put(v1, hs.size());
                    }
                }
                logger.info("pre-read ok!,take time:{}", preReadTracker.getHumanFriendlyElapsedTime());
                logger.info("id mapping memory usage:{}", RamUsageEstimator.humanSizeOf(hs));
                lv.clen = hs.size();
                lv.elen = edgeFileCount * 2;
                lv.nlen = lv.clen;
                lv.olen = lv.elen;
                mallocLouvain(lv);
            }
        }
        logger.info("graph total memory usage:{}", RamUsageEstimator.humanSizeOf(lv));
        try (BufferedReader reader = new BufferedReader(new FileReader(edgeFile)))
        {
            String line;
            cnt = 0;
            ProgressTracker initTracker = new ProgressTracker(edgeFileCount);
            initTracker.start();
            while ((line = reader.readLine()) != null)
            {

                if (line.startsWith("#"))
                {
                    continue;
                }
                cnt++;
                if (cnt % 10_0000 == 0)
                {
                    initTracker.setCurrent(cnt);
                    logger.info("init progress:{},etc:{}", initTracker.getHumanFriendlyProgress(),
                            initTracker.getHumanFriendlyEtcTime());
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
            return lv;
        }
    }

    private static void addNodeToComm(Louvain lv, int id, int cid, double weight)
    {
        final Node node = lv.node;
        node.setClsid(id, cid);
        node.setNext(id, node.getNext(cid));
        node.setNext(cid, id);
        node.setPrev(id, cid);
        if (node.getNext(id) != -1)
        {
            node.setPrev(node.getNext(id), id);
        }
        node.setCount(cid, node.getCount(cid) + node.getCount(id));
        node.setClstot(cid, node.getClstot(cid) + node.getClstot(id));
        node.setClskin(cid, node.getClskin(cid) + node.getKin(id) + 2 * weight);
    }

    private static void removeNodeFromComm(Louvain lv, int id, double weight)
    {
        final Node node = lv.node;
        int cid = node.getClsid(id);
        int prev, next;
        if (cid != id)
        {
            prev = node.getPrev(id);
            next = node.getNext(id);
            node.setNext(prev, next);
            if (next != -1)
            {
                node.setPrev(next, prev);
            }
            node.setCount(cid, node.getCount(cid) - node.getCount(id));
            node.setClstot(cid, node.getClstot(cid) - node.getClstot(id));
            node.setClskin(cid, node.getClskin(cid) - (node.getKin(id) + 2 * weight));
        }
        else
        {
            next = node.getNext(id);
            cid = next;
            if (next != -1)
            {
                node.setPrev(next, -1);
                node.setClsid(next, next);
                while (-1 != (next = node.getNext(next)))
                {
                    node.setCount(cid, node.getCount(cid) + node.getCount(next));
                    node.setClsid(next, cid);
                }
                node.setClstot(cid, node.getClstot(id) - node.getKin(id) - node.getKout(id));
                node.setClskin(cid, node.getClskin(id) - node.getKin(id) - 2 * weight);
                node.setCount(id, node.getCount(id) - node.getCount(cid));
                node.setClskin(id, node.getKin(id));
                node.setClstot(id, node.getClstot(id) - node.getClstot(cid));
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
            ProgressTracker tracker = new ProgressTracker(lv.clen);
            tracker.start();
            cct = 0;
            for (int i = 0; i < lv.clen; i++)
            {
                tracker.setCurrent(i);
                if (i % 10_0000 == 0)
                {
                    logger.info("One iteration inner first stage progress: {},etc:{}",
                            tracker.getHumanFriendlyProgress(), tracker.getHumanFriendlyEtcTime());
                }
                int ci = lv.cindex[i];
                double kv = lv.node.getKin(ci) + lv.node.getKout(ci);
                int cid = lv.node.getClsid(ci);
                long ei = lv.node.getEindex(ci);
                idc = 0;
                while (ei != -1)
                {
                    int wi = lv.edge.getRight(ei);
                    double wei = lv.edge.getWeight(ei);
                    int wci = lv.node.getClsid(wi);
                    weight[wci] += wei;
                    ids[idc++] = wci;
                    ei = lv.edge.getNext(ei);
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
                            deltaQ = weight[ids[j]] - kv * (lv.node.getClstot(ids[j]) - kv) / lv.sumw;
                            cwei = weight[ids[j]];
                        }
                        else
                        {
                            deltaQ = weight[ids[j]] - kv * lv.node.getClstot(ids[j]) / lv.sumw;
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
            logger.info("One iteration inner first stage, changed nodes: {},take time:{}", cct,
                    tracker.getHumanFriendlyElapsedTime());
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
            if (lv.node.getClsid(ci) == ci)
            {
                lv.cindex[tclen++] = ci;
                int next = lv.node.getNext(ci);
                int first = lv.node.getFirst(ci);
                if (first != -1)
                {
                    while (lv.node.getNext(first) != -1)
                    {
                        first = lv.node.getNext(first);
                    }
                    lv.node.setNext(first, next);
                }
                else
                {
                    lv.node.setFirst(ci, next);
                }
                if (next != -1)
                {
                    lv.node.setPrev(next, first);
                }
                lv.node.setNext(ci, -1);
                lv.node.setPrev(ci, -1);
            }
        }
        lv.clen = tclen;
        for (int i = 0; i < lv.clen; i++)
        {
            int ci = lv.cindex[i];
            lv.node.setKin(ci, lv.node.getClskin(ci));
            lv.node.setKout(ci, lv.node.getClstot(ci) - lv.node.getKin(ci));
            lv.node.setEindex(ci, -1);
        }
        for (int i = 0; i < lv.elen; i++)
        {
            int l = lv.edge.getLeft(i);
            int r = lv.edge.getRight(i);
            double w = lv.edge.getWeight(i);
            int lcid = lv.node.getClsid(l);
            int rcid = lv.node.getClsid(r);
            if (lcid != rcid)
            {
                lv.edge.setLeft(telen, lcid);
                lv.edge.setRight(telen, rcid);
                lv.edge.setWeight(telen, w);
                lv.edge.setNext(telen, lv.node.getEindex(lcid));
                lv.node.setEindex(lcid, telen++);
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
        IntSet hs = new IntOpenHashSet(lv.node.size);
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
                final String line = reader.readLine();
                if (line.startsWith("#"))
                {
                    continue;
                }
                cnt++;
                if (cnt % 10_0000 == 0)
                {
                    tracker.setCurrent(cnt);
                    logger.info("write progress:{},etc:{}", tracker.getHumanFriendlyProgress(),
                            tracker.getHumanFriendlyEtcTime());
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
            final int clusterId = find(nodeLeftId, lv.node);
            hs.add(rawNode);
            writer.write(rawNode + "," + clusterId + "," + lv.node.getCount(clusterId));
            writer.write("," + lv.node.getKin(clusterId));
            writer.write("," + lv.node.getKout(clusterId));
            writer.write(System.lineSeparator());
        }
    }

    private static int find(int idx, Node node)
    {
        if (node.getClsid(idx) == idx)
        {
            return idx;
        }
        return find(node.getClsid(idx), node);
    }

    public static void clear(Louvain lv)
    {
        if (lv != null)
        {
            lv.cindex = null;
            lv.node = null;
            lv.edge.close();
            lv.edge = null;
        }
    }

    public static double calcModularity(Louvain lv)
    {
        double modularity = 0.0;
        for (int k = 0; k < lv.clen; k++)
        {
            int i = lv.cindex[k];
            modularity += lv.node.getKin(i) / (2 * lv.sumw) - Math.pow(lv.node.getClstot(i) / (2 * lv.sumw), 2);
        }
        return modularity;
    }

}
