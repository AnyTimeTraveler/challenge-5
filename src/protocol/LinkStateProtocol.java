package protocol;

import client.IRoutingProtocol;
import client.LinkLayer;
import client.Packet;
import dijkstra.DijkstraAlgorithm;
import dijkstra.Edge;
import dijkstra.Graph;
import dijkstra.Vertex;

import java.util.*;

public class LinkStateProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;

    private ConnectionTable connectionTable;
    private int currentTick;


    public LinkStateProtocol() {
        connectionTable = new ConnectionTable();
        currentTick = 0;
    }

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
    }


    @Override
    public void tick(Packet[] packets) {
        currentTick++;
        System.out.print("pack: " + packets.length);
        connectionTable.removeAllLinksTo(linkLayer.getOwnAddress());
        for (Packet packet : packets) {
            updateDirectLinkCost(packet.getSourceAddress());
            if (packet.getRawData().length > 0) {
                connectionTable.update(packet);
            }
        }
        broadcastMyTableOrEmptyPacket();
        System.out.println();
        System.out.println(connectionTable.toString());
    }

    private void updateDirectLinkCost(int sourceAddress) {
        ConnectionTable.Connection link = connectionTable.get(sourceAddress, linkLayer.getOwnAddress());
        if (link != null) {
            link.cost = linkLayer.getLinkCost(sourceAddress);
            link.tick = currentTick;
        } else {
            connectionTable.add(sourceAddress, linkLayer.getOwnAddress(), linkLayer.getLinkCost(sourceAddress), currentTick);
        }
    }

    private void broadcastMyTableOrEmptyPacket() {
        if (connectionTable.isEmpty())
            linkLayer.transmit(new Packet(linkLayer.getOwnAddress(), 0, new byte[0]));
        else
            linkLayer.transmit(new Packet(linkLayer.getOwnAddress(), 0, connectionTable.serialize()));
    }

    public HashMap<Integer, Integer> getForwardingTable() {

        List<Vertex> vertices = new ArrayList<>();
        List<Integer> hosts = new ArrayList<>();
        for (ConnectionTable.Connection connection : connectionTable.getConnections()) {
            if (!hosts.contains(connection.hostA))
                hosts.add(connection.hostA);
            if (!hosts.contains(connection.hostB))
                hosts.add(connection.hostB);
        }
        for (Integer host : hosts) {
            vertices.add(new Vertex(host, "Host " + host));
        }

        HashMap<Integer, Vertex> vertexMap = new HashMap<>();
        for (Vertex vertex : vertices) {
            vertexMap.put(vertex.getId(), vertex);
        }

        List<Edge> edges = new ArrayList<>();
        for (ConnectionTable.Connection connection : connectionTable.getConnections()) {
            edges.add(new Edge(vertexMap.get(connection.hostA), vertexMap.get(connection.hostB), connection.cost));
        }

        Graph graph = new Graph(vertices, edges);
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        dijkstra.execute(vertexMap.get(linkLayer.getOwnAddress()));

        HashMap<Integer, Integer> ft = new HashMap<>();
        for (Map.Entry<Integer, Vertex> entry : vertexMap.entrySet()) {
            LinkedList<Vertex> path = dijkstra.getPath(entry.getValue());
            if (path != null) {
                ft.put(entry.getKey(), path.get(0).getId());
            }
        }
        return ft;
    }
}
