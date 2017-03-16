package protocol;

import client.Packet;

import java.io.*;
import java.util.HashMap;

/**
 * Created by simon on 16.03.17.
 */
public class Util {
    public static byte[] serializeRoutingTable(HashMap<Integer, RoutingEntry> forwardingTable) {
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


    public static HashMap<Integer, RoutingEntry> getForwardingTableFromPacket(Packet packet) {
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
}
