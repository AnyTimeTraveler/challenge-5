package protocol;

import client.IRoutingProtocol;
import client.LinkLayer;
import client.Packet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExtendedDistanceVectorProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;

    private HashMap<Integer, HashMap<Integer, RoutingEntry>> receivedForwardingTables;
    private HashMap<Integer, RoutingEntry> forwardingTable;
    private Set<Integer> neighbours;
    private int ownAddress;

    public ExtendedDistanceVectorProtocol() {
        receivedForwardingTables = new HashMap<>();
        neighbours = new HashSet<>();
    }

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
        ownAddress = linkLayer.getOwnAddress();
        System.out.println("Address: " + ownAddress);
    }


    @Override
    public void tick(Packet[] packets) {
        resetAllData();
        updateKnownNeighbours(packets);
        updateReceivedForwardingTables(packets);
        updateForwardingTableFromReceivedTables();
        if (forwardingTable.size() == 1) {
            broadcastEmptyPacket();
        } else {
            sendTableToKnownNeighbours();
        }
    }

    private void resetAllData() {
        neighbours = new HashSet<>();
        receivedForwardingTables = new HashMap<>();
        forwardingTable = new HashMap<>();
        forwardingTable.put(ownAddress, new RoutingEntry(ownAddress, 0, ownAddress));
    }

    private void updateReceivedForwardingTables(Packet[] packets) {
        for (Packet packet : packets) {
            if (packet.getRawData().length == 0)
                continue;
            HashMap<Integer, RoutingEntry> receivedTable = Util.getForwardingTableFromPacket(packet);
            HashMap<Integer, RoutingEntry> filteredReceivedTable = new HashMap<>();
            for (HashMap.Entry<Integer, RoutingEntry> entry : receivedTable.entrySet()) {
                if (!(entry.getValue().nextHop == ownAddress || entry.getKey() == ownAddress || entry.getValue().cost < 0)) {
                    filteredReceivedTable.put(entry.getKey(),entry.getValue());
                }
            }
            receivedForwardingTables.put(packet.getSourceAddress(), filteredReceivedTable);
        }
    }

private void sendTableToKnownNeighbours() {
        HashMap<Integer, RoutingEntry> forwardingTableToSend = new HashMap<>(forwardingTable);
        System.out.println("Sending ");
        forwardingTableToSend.entrySet().forEach(entry -> System.out.println(entry.getKey() + ">" + entry.getValue().nextHop + "(" + entry.getValue().cost + ")"));
        //Send
        linkLayer.transmit(new Packet(linkLayer.getOwnAddress(), 0, Util.serializeRoutingTable(forwardingTableToSend)));
    }

    private void broadcastEmptyPacket() {
        //Broadcast an empty packet to all neighbours to notify them of its existence
        linkLayer.transmit(new Packet(linkLayer.getOwnAddress(), 0, new byte[0]));
    }

    private void updateForwardingTableFromReceivedTables() {
        for (HashMap.Entry<Integer, HashMap<Integer, RoutingEntry>> receivedTableEntry : receivedForwardingTables.entrySet()) {
            for (Map.Entry<Integer, RoutingEntry> entry : receivedTableEntry.getValue().entrySet()) {
                int source = receivedTableEntry.getKey();
                int costToDestination = linkLayer.getLinkCost(receivedTableEntry.getKey()) + entry.getValue().cost;
                int destination = entry.getKey();
                if (forwardingTable.containsKey(destination)) {
                    if (costToDestination >= 0 && forwardingTable.get(destination).cost > costToDestination) {
                        forwardingTable.put(destination, new RoutingEntry(source, costToDestination, destination));
                    }
                } else {
                    forwardingTable.put(destination, new RoutingEntry(source, costToDestination, destination));
                }
            }
        }
    }


    private void updateKnownNeighbours(Packet[] packets) {
        System.out.print("Received package from: ");
        for (Packet packet : packets) {
            int sourceAddress = packet.getSourceAddress();
            System.out.print(sourceAddress + ", ");
            neighbours.add(sourceAddress);
            forwardingTable.put(sourceAddress, new RoutingEntry(sourceAddress, linkLayer.getLinkCost(sourceAddress), sourceAddress));
        }
        System.out.println();
    }

    public HashMap<Integer, Integer> getForwardingTable() {
        // This code transforms your forwarding table which may contain extra information
        // to a simple one with only a next hop (value) for each destination (key).
        // The result of this method is send to the server to validate and score your protocol.

        // <Destination, NextHop>
        HashMap<Integer, Integer> ft = new HashMap<>();

        for (Map.Entry<Integer, RoutingEntry> entry : forwardingTable.entrySet()) {
            ft.put(entry.getKey(), entry.getValue().nextHop);
        }

        return ft;
    }
}
