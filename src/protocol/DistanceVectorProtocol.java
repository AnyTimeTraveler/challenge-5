package protocol;

import client.IRoutingProtocol;
import client.LinkLayer;
import client.Packet;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceVectorProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;

    private HashMap<Integer, RoutingEntry> forwardingTable;
    private List<Integer> neighboursList;

    public DistanceVectorProtocol() {
        forwardingTable = new HashMap<>();
        neighboursList = new ArrayList<>();
    }

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
    }


    @Override
    public void tick(Packet[] packets) {
        System.out.println("tick; received " + packets.length + " packets");

        if (forwardingTable.isEmpty()) {
            broadcastEmptyPacket();
        } else {
            updateKnownNeightboursList(packets);
            updateForwardingTableFromReceivedPackets(packets);
            sendTableToKnownNeighbours();
        }
    }

    private void sendTableToKnownNeighbours() {
        //Send a personalized forwarding table to every known neighbour
        for (int neighbour : neighboursList) {
            HashMap<Integer, RoutingEntry> personalizedForwardingTable = new HashMap<>(forwardingTable);
            //Checks if the table has routes via this neighbour. This entry won't be sent.
            for (HashMap.Entry<Integer, RoutingEntry> personalizedEntry : forwardingTable.entrySet()) {
                if (personalizedEntry.getValue().nextHop == neighbour) {
                    personalizedForwardingTable.remove(personalizedEntry.getKey());
                }
            }

            //Send
            linkLayer.transmit(new Packet(linkLayer.getOwnAddress(), neighbour, serializeRoutingTable(personalizedForwardingTable)));
        }
    }

    byte[] serializeRoutingTable(HashMap<Integer, RoutingEntry> forwardingTable) {
        try {
            //Convert object to byteArray
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
        //Broadcast an empty packet to all neighbours to notify them of its existence
        linkLayer.transmit(new Packet(linkLayer.getOwnAddress(), 0, new byte[0]));
    }

    private void updateForwardingTableFromReceivedPackets(Packet[] packets) {
        for (Packet packet : packets) {
            getForwardingTableFromPacket(packet);
        }
    }

    HashMap<Integer, RoutingEntry> getForwardingTableFromPacket(Packet packet) {
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(packet.getRawData());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            //TODO Check
            return (HashMap<Integer, RoutingEntry>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateKnownNeightboursList(Packet[] packets) {
        for (Packet packet : packets) {
            if (!neighboursList.contains(packet.getSourceAddress())) {
                neighboursList.add(packet.getSourceAddress());
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
