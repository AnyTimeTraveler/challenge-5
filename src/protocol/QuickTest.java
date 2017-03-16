//package protocol;
//
//import client.Packet;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by simon on 16.03.17.
// */
//public class QuickTest {
//    public static void main(String[] args) {
//        DistanceVectorProtocol dvp = new DistanceVectorProtocol();
//
//        HashMap<Integer, RoutingEntry> testRoutingTable = new HashMap<>();
//        testRoutingTable.put(1, new RoutingEntry(3, 2));
//
//        byte[] data = dvp.serializeRoutingTable(testRoutingTable);
//
//        HashMap<Integer, RoutingEntry> newData = dvp.getForwardingTableFromPacket(new Packet(0, 0, data));
//
//        for (Map.Entry<Integer, RoutingEntry> entry : newData.entrySet()) {
//            System.out.println(entry.getKey() + " > " + entry.getValue().cost + " : " + entry.getValue().nextHop);
//        }
//
//    }
//}
