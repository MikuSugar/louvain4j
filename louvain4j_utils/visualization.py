import networkx as nx
import matplotlib.pyplot as plt
import sys

edges_file = sys.argv[1]
community_file = sys.argv[2]
# example
# edges_file = "../src/test/resources/club.txt"
# community_file = "../src/test/resources/club.txt_out.txt"


print(f"edge_file:{edges_file}")
print(f"community_file:{community_file}")

# 创建一个空的无向图
G = nx.Graph()

# 从边文件中添加边
with open(edges_file, 'r') as edge_file:
    for line in edge_file:
        strs = line.strip().split()
        u = strs[0]
        v = strs[1]
        G.add_edge(u, v)

# 从点文件中添加节点和社区信息
c_dist = {}
community_map = {}
with open(community_file, 'r') as node_file:
    next(node_file)
    for line in node_file:
        info = line.strip().split(",")
        node_id = info[0]
        community_id = info[1]
        cid = int(community_id)
        if cid not in c_dist.keys():
            c_dist[cid] = len(c_dist)
        community_map[node_id] = c_dist[cid]

# 创建节点着色的映射
node_colors = [community_map[node] for node in G.nodes()]

# 绘制图形，根据节点着色
pos = nx.spring_layout(G)
nx.draw(G, pos, node_color=node_colors, cmap=plt.get_cmap('viridis'), with_labels=True)
plt.show()
