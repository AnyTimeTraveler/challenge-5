package protocol;

import client.IRoutingProtocol;
import client.LinkLayer;
import client.Packet;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DistanceVectorProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;

    private HashMap<Integer, RoutingEntry> forwardingTable;

    public DistanceVectorProtocol() {
        forwardingTable = new HashMap<>();
    }

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
    }


    @Override
    public void tick(Packet[] packets) {
        System.out.println("tick; received " + packets.length + " packets");
        for (Packet packet : packets) {
            System.out.println(packet);
        }

        if (forwardingTable.isEmpty()) {
            broadcastEmptyPacket();
        } else {
            updateKnownHostsList(packets);
            updateForwardingTableFromReceivedPackets(packets);
            sendTableToKnownNeighbours(forwardingTable);
        }
    }

    private void sendTableToKnownNeighbours(HashMap<Integer, RoutingEntry> forwardingTable) {
        linkLayer.transmit(new Packet(linkLayer.getOwnAddress(), 0, serializeRoutingTable(forwardingTable)));
    }

    public byte[] serializeRoutingTable(HashMap<Integer, RoutingEntry> forwardingTable) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(forwardingTable);
            return byteOut.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void broadcastEmptyPacket() {
        linkLayer.transmit(new Packet(linkLayer.getOwnAddress(), 0, new byte[0]));
    }

    private void updateForwardingTableFromReceivedPackets(Packet[] packets) {
        for (Packet packet : packets) {
            getForwardingTableFromPacket(packet);
        }
    }

    public HashMap<Integer, RoutingEntry> getForwardingTableFromPacket(Packet packet) {
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(packet.getRawData());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            return (HashMap<Integer, RoutingEntry>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateKnownHostsList(Packet[] packets) {
        for (Packet packet : packets) {
            int sourceAddress = packet.getSourceAddress();
            if (!forwardingTable.containsKey(sourceAddress)) {
                forwardingTable.put(sourceAddress, new RoutingEntry(sourceAddress, linkLayer.getLinkCost(sourceAddress)));
            }
        }
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
