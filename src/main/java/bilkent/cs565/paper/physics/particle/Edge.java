package bilkent.cs565.paper.physics.particle;

public class Edge {

    public final Particle a;
    public final Particle b;

    public Edge(final Particle a, final Particle b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        return a.hashCode() ^ b.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Edge) {
            return a.id == ((Edge) o).a.id && b.id == ((Edge) o).b.id ||
                b.id == ((Edge) o).a.id && a.id == ((Edge) o).b.id;
        }
        return false;
    }
}
