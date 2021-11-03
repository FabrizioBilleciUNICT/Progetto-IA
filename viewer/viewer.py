from pyvis.network import Network
import csv
import math
import random

indices = ["0", "1", "2", "3", "4", "5"]
colors_ = lambda n: list(map(lambda i: "#" + "%06x" % random.randint(0, 0xFFFFFF), range(n)))

list_colors = colors_(70)
map_colors = dict()

for index in indices:
    net = Network()
    # nodes = set()

    with open('../output/' + index + '_output_nodes.csv', mode='r') as csv_file:
        csv_reader = csv.DictReader(csv_file)
        for row in csv_reader:

            if row["partition"] not in map_colors:
                map_colors[row["partition"]] = list_colors.pop()

            net.add_node(row["id"], label=row["label"]+" : "+row["weight"], size=math.log10(int(row["weight"]))+1, color=map_colors[row["partition"]])

    with open('../output/' + index + '_output_edges.csv', mode='r') as csv_file:
        csv_reader = csv.DictReader(csv_file)
        for row in csv_reader:
            # if row["source"] in nodes and row["target"] in nodes:
            net.add_edge(row["source"], row["target"], weight=row["weight"], title=row["weight"])

    net.show_buttons(filter_=['physics'])
    net.show(index + '_graph.html')
