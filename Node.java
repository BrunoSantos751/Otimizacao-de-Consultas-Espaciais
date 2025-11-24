// Node.java
import java.util.ArrayList;
import java.util.List;

public class Node {
    public final List<Entry> entries = new ArrayList<>();
    public Node parent = null;
    public boolean isLeaf;
    public Rectangle mbr = null;

    public Node(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public void recalcMBR() {
        if (entries.isEmpty()) {
            this.mbr = null;
            return;
        }
        Rectangle r = entries.get(0).mbr.copy();
        for (int i = 1; i < entries.size(); i++) {
            r.expandToInclude(entries.get(i).mbr);
        }
        this.mbr = r;
    }
}
