package protocol;

import client.IRoutingProtocol;
import client.LinkLayer;
import client.Packet;

import java.util.HashMap;
import java.util.Map;

public class DistanceVectorProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;

    private HashMap<Integer, RoutingEntry> forwardingTable;

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
            for (Packet packet : packets)
                sendEmptyPacket(packet.getSourceAddress());
        } else {
            updateKnownHostsList(packets);
            updateForwardingTableFromReceivedPackets(packets);
            sendTableToKnownNeighbours(forwardingTable);
        }
    }

    private void sendTableToKnownNeighbours(HashMap<Integer, RoutingEntry> forwardingTable) {

    }

    private void sendEmptyPacket(int destination) {

    }

    private void updateForwardingTableFromReceivedPackets(Packet[] packets) {

    }

    private void updateKnownHostsList(Packet[] packets) {

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
