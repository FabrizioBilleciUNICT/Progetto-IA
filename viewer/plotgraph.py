import networkx as nx
import matplotlib.pyplot as plt

G = nx.DiGraph()

G.add_edges_from([('A', 'B'),('C','D'),('G','D')], weight=1)
G.add_edges_from([('D','A'),('B','E'),('D','E')], weight=2)
G.add_edges_from([('B','C'),('E','F')], weight=3)
G.add_edges_from([('C','F'), ('B','G')], weight=4)

coords = {'D': [0.3, 0.6], 'B': [0.9, 0.4], 'C': [0.1, 0.5], 'A': [0.1, 0.3],
          'E': [0.7, 0.6], 'F': [0.3, 0.9], 'G': [0.7, 0.8]}

edge_labels = dict([((u,v,), d['weight']) for u,v,d in G.edges(data=True)])

plt.figure("PlotGraph", figsize=(10,5))
#pos = nx.spring_layout(G)
nx.draw_networkx_nodes(G, pos = coords, node_color = 'orange', node_size = 300)
nx.draw_networkx_labels(G, pos = coords)
nx.draw_networkx_edges(G, pos = coords, edge_color = 'black', arrows=True)
nx.draw_networkx_edge_labels(G, pos = coords, edge_labels = edge_labels)
plt.show()