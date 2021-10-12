from pyvis.network import Network
import csv
import math

net = Network()
nodes = set()
index = "0"

with open('../output/' + index + '_output_nodes.csv', mode='r') as csv_file:
    csv_reader = csv.DictReader(csv_file)
    header = True
    ll = 0
    for row in csv_reader:
        ll += 1
        if header:
            header = False
        elif ll < 1500:
            nodes.add(row["id"])
            color = "#f00" if row["partition"] == "0" else "#0ff"
            net.add_node(row["id"], label=row["label"], size=math.log10(int(row["weight"]))+1, color=color)

with open('../output/' + index + '_output_edges.csv', mode='r') as csv_file:
    csv_reader = csv.DictReader(csv_file)
    header = True
    for row in csv_reader:
        if header:
            header = False
        else:
            if row["source"] in nodes and row["target"] in nodes:
                net.add_edge(row["source"], row["target"], weight=row["weight"])

# net.enable_physics(True)
'''
var options = {
  "physics": {
    "barnesHut": {
      "gravitationalConstant": -4355518,
      "centralGravity": 9.75,
      "springLength": 285,
      "springConstant": 0
    },
    "maxVelocity": 85,
    "minVelocity": 0.85,
    "timestep": 0.13
  }
}
'''
net.show_buttons(filter_=['physics'])
net.show(index + '_graph.html')
