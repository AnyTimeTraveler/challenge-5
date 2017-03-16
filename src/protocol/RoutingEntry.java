package protocol;

/**
 * Simple object which describes a route entry in the forwarding table.
 * Can be extended to include additional data.
 */
public class RoutingEntry {
    public int nextHop;
    public int cost;

    public RoutingEntry(int nextHop, int cost) {
        this.nextHop = nextHop;
        this.cost = cost;
    }
}
