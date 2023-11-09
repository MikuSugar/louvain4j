import networkx as nx
import sys

# 从边文件中读取边信息
edges_file = sys.argv[1]
community_file = sys.argv[2]
# example
# edges_file = "../src/test/resources/p2p-31.csv"
# community_file = "../src/test/resources/p2p-31.csv_out.txt"


print(f"edge_file:{edges_file}")
print(f"community_file:{community_file}")

G = nx.Graph()

with open(edges_file, "r") as f:
    for line in f:
        info = line.strip().split()
        source = info[0]
        target = info[1]
        weight = 1
        if len(info) >= 3:
            weight = info[2]
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
