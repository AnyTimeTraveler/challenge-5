package protocol;

import client.IRoutingProtocol;
import client.LinkLayer;
import client.Packet;

import java.io.*;
import java.util.*;

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
        if (new Random().nextFloat() < 0.9) {
            System.out.print("tick; ");
        } else {
            System.out.print("tock; ");
        }
        System.out.println("received " + packets.length + " packets");

        updateKnownNeighbours(packets);
        updateForwardingTableFromReceivedPackets(packets);
        if (forwardingTable.isEmpty()) {
            broadcastEmptyPacket();
        } else {
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
            if (packet.getRawData().length == 0)
                continue;
            HashMap<Integer, RoutingEntry> receivedTable = getForwardingTableFromPacket(packet);

            for (Map.Entry<Integer, RoutingEntry> entry : receivedTable.entrySet()) {
                RoutingEntry myEntry = new RoutingEntry(packet.getSourceAddress(),linkLayer.getLinkCost(packet.getSourceAddress()) + entry.getValue().cost,entry.getKey());
                if (forwardingTable.containsKey(myEntry.finalDestination)) {
                    if (forwardingTable.get(myEntry.finalDestination).cost > myEntry.cost) {
                        forwardingTable.remove(myEntry.finalDestination);
                        forwardingTable.put(myEntry.finalDestination, myEntry);
                    }
                } else {
                    forwardingTable.put(myEntry.finalDestination, myEntry);
                }
            }
        }
    }

    HashMap<Integer, RoutingEntry> getForwardingTableFromPacket(Packet packet) {
        if (packet.getRawData().length == 0)
            return null;
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(packet.getRawData());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            // no check needed
            return (HashMap<Integer, RoutingEntry>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateKnownNeighbours(Packet[] packets) {
        for (Packet packet : packets) {
            int sourceAddress = packet.getSourceAddress();
            if (!neighboursList.contains(sourceAddress)) {
                neighboursList.add(sourceAddress);
                forwardingTable.put(sourceAddress, new RoutingEntry(sourceAddress, linkLayer.getLinkCost(sourceAddress), sourceAddress));
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
