package dijkstra;

public class Vertex {
    final private int id;
    final private String name;


    public Vertex(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return this.id == ((Vertex) obj).id;
    }

    @Override
    public String toString() {
        return name;
    }

}