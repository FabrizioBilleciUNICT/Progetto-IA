from pyvis.network import Network
import csv
import math

indices = ["test", "0", "1", "2", "3", "4"]

for index in indices:
    net = Network()
    # nodes = set()

    with open('../output/' + index + '_output_nodes.csv', mode='r') as csv_file:
        csv_reader = csv.DictReader(csv_file)
        ll = 0
        for row in csv_reader:
            if ll < 1500:
                print(row['id'])
                # nodes.add(row["id"])
                color = "#f00" if row["partition"] == "0" else "#0ff"
                net.add_node(row["id"], label=row["label"]+" : "+row["weight"], size=math.log10(int(row["weight"]))+1, color=color)

    with open('../output/' + index + '_output_edges.csv', mode='r') as csv_file:
        csv_reader = csv.DictReader(csv_file)
        for row in csv_reader:
            # if row["source"] in nodes and row["target"] in nodes:
            net.add_edge(row["source"], row["target"], weight=row["weight"], title=row["weight"])

    net.show_buttons(filter_=['physics'])
    net.show(index + '_graph.html')
