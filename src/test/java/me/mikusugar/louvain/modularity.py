import networkx as nx

# 从边文件中读取边信息
edges_file = "/Users/mikusugar/code/github/louvain4j/src/test/resources/p2p-31.csv"
community_file = "/Users/mikusugar/code/github/louvain4j/src/test/resources/p2p-31.csv_out.txt"

G = nx.Graph()

with open(edges_file, "r") as f:
    for line in f:
        source, target, weight = line.strip().split()
        G.add_edge(source, target, weight=int(weight))

# 从点文件中读取社区信息

node_to_community = {}

with open(community_file, "r") as f:
    next(f)
    for line in f:
        info = line.strip().split(",")
        node = info[0]
        community = info[1]
        node_to_community[node] = int(community)

community_partition = {}
for node, community in node_to_community.items():
    if community not in community_partition:
        community_partition[community] = set()
    community_partition[community].add(node)

# 计算模块度
modularity = nx.community.modularity(G, community_partition.values())
print("Modularity:", modularity)
