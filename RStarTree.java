// RStarTreeOptimized.java
import java.util.*;

public class RStarTree {
    public Node root;
    private final int maxEntries;
    private final int minEntries;
    private static final double REINSERT_PCT = 0.3;

    public RStarTree(int maxEntries) {
        if (maxEntries < 4) maxEntries = 4;
        this.maxEntries = maxEntries;
        this.minEntries = Math.max(2, maxEntries / 2);
        this.root = new Node(true); // start with leaf
    }

    public void insert(Point p) {
        Entry e = new Entry(p);
        Node leaf = chooseLeaf(root, e);
        leaf.entries.add(e);
        e.child = null;
        e.mbr = new Rectangle(p);
        leaf.recalcMBR();

        if (leaf.entries.size() > maxEntries) {
            handleOverflow(leaf);
        }
    }

    private Node chooseLeaf(Node node, Entry entry) {
        if (node.isLeaf) return node;

        Node best = null;
        double bestEnlargement = Double.POSITIVE_INFINITY;
        double bestArea = Double.POSITIVE_INFINITY;

        for (Entry childEntry : node.entries) {
            double enr = childEntry.mbr.enlargement(entry.mbr);
            double area = childEntry.mbr.area();
            if (enr < bestEnlargement || (enr == bestEnlargement && area < bestArea)) {
                best = childEntry.child;
                bestEnlargement = enr;
                bestArea = area;
            }
        }
        return chooseLeaf(best, entry);
    }

    private void handleOverflow(Node node) {
        if (node.entries.size() <= maxEntries) return;

        // Faz o split do nó
        Node newNode = split(node);

        if (node.parent == null) {
            // Se era a raiz, o split cria uma nova raiz e não há mais overflow para propagar
            Node newRoot = new Node(false);
            Entry en1 = new Entry(node);
            Entry en2 = new Entry(newNode);
            newRoot.entries.add(en1);
            newRoot.entries.add(en2);
            node.parent = newRoot;
            newNode.parent = newRoot;
            newRoot.recalcMBR();
            this.root = newRoot;
            return; // Não precisa chamar handleOverflow na raiz recém-criada
        }

        // Se não era raiz, adiciona newNode ao parent e verifica overflow do parent
        newNode.parent = node.parent;
        node.parent.entries.add(new Entry(newNode));
        node.parent.recalcMBR();

        if (node.parent.entries.size() > maxEntries) {
            handleOverflow(node.parent); // Propaga apenas se necessário
        }
    }

    private double distanceToCenter(Node node, Entry e) {
        double cx = (node.mbr.minX + node.mbr.maxX) / 2;
        double cy = (node.mbr.minY + node.mbr.maxY) / 2;
        double px = e.mbr.minX + (e.mbr.maxX - e.mbr.minX) / 2;
        double py = e.mbr.minY + (e.mbr.maxY - e.mbr.minY) / 2;
        return Math.hypot(cx - px, cy - py);
    }

private Node split(Node node) {
    // Escolhe eixo de split
    boolean chooseByX = chooseSplitAxis(node);

    // Ordena cópia das entries
    List<Entry> copy = new ArrayList<>(node.entries);
    copy.sort(chooseByX ? Comparator.comparingDouble(e -> e.mbr.minX)
                        : Comparator.comparingDouble(e -> e.mbr.minY));

    int splitIndex = copy.size() / 2;
    List<Entry> group1 = new ArrayList<>(copy.subList(0, splitIndex));
    List<Entry> group2 = new ArrayList<>(copy.subList(splitIndex, copy.size()));

    // Atribui entries ao nó original e ao novo nó
    node.entries.clear();
    node.entries.addAll(group1);
    for (Entry e : node.entries) {
        if (e.child != null) e.child.parent = node;
    }

    Node newNode = new Node(node.isLeaf);
    newNode.entries.addAll(group2);
    for (Entry e : newNode.entries) {
        if (e.child != null) e.child.parent = newNode;
    }

    node.recalcMBR();
    newNode.recalcMBR();

    return newNode;
}

    private boolean chooseSplitAxis(Node node) {
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Entry e : node.entries) {
            minX = Math.min(minX, e.mbr.minX); maxX = Math.max(maxX, e.mbr.maxX);
            minY = Math.min(minY, e.mbr.minY); maxY = Math.max(maxY, e.mbr.maxY);
        }
        return (maxX - minX) >= (maxY - minY);
    }

    public List<Point> rangeQuery(Rectangle rect) {
        List<Point> out = new ArrayList<>();
        rangeQueryNode(root, rect, out);
        return out;
    }

    private void rangeQueryNode(Node node, Rectangle rect, List<Point> out) {
        if (node.mbr == null || !intersect(node.mbr, rect)) return;
        if (node.isLeaf) {
            for (Entry e : node.entries) {
                if (e.point != null && intersect(e.mbr, rect)) out.add(e.point);
            }
        } else {
            for (Entry e : node.entries) rangeQueryNode(e.child, rect, out);
        }
    }

    private boolean intersect(Rectangle a, Rectangle b) {
        return !(a.maxX < b.minX || a.maxY < b.minY || a.minX > b.maxX || a.minY > b.maxY);
    }
}
