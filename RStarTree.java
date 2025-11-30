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
        // Otimização: atualiza MBR incrementalmente ao invés de recalcular
        leaf.expandMBR(e.mbr);

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

        // Forced Reinsert: tenta redistribuir antes de fazer split
        // Isso melhora a qualidade da estrutura da árvore
        // Aplicado apenas uma vez por nó para evitar recursão infinita
        if (node.parent != null && node.isLeaf && node.entries.size() > maxEntries) {
            int numReinsert = Math.max(1, (int) (node.entries.size() * REINSERT_PCT));
            if (numReinsert > 0 && numReinsert < node.entries.size() && node.entries.size() - numReinsert >= minEntries) {
                // Ordena entradas por distância ao centro do MBR do nó
                List<Entry> sorted = new ArrayList<>(node.entries);
                sorted.sort((e1, e2) -> {
                    double d1 = distanceToCenter(node, e1);
                    double d2 = distanceToCenter(node, e2);
                    return Double.compare(d2, d1); // Mais distantes primeiro
                });
                
                // Remove as entradas mais distantes
                List<Entry> toReinsert = new ArrayList<>();
                for (int i = 0; i < numReinsert; i++) {
                    toReinsert.add(sorted.get(i));
                }
                
                // Remove do nó
                node.entries.removeAll(toReinsert);
                node.recalcMBR();
                
                // Reinsere os pontos na árvore (apenas pontos, não nós internos)
                for (Entry e : toReinsert) {
                    if (e.point != null) {
                        // Reinsere o ponto diretamente sem passar por handleOverflow novamente
                        // para evitar recursão infinita
                        insertWithoutReinsert(e.point);
                    }
                }
                
                // Se ainda houver overflow após reinsert, faz split
                if (node.entries.size() > maxEntries) {
                    Node newNode = split(node);
                    handleNewNodeAfterSplit(node, newNode);
                    return;
                }
                return;
            }
        }

        // Se não aplicou reinsert ou ainda há overflow, faz split
        Node newNode = split(node);
        handleNewNodeAfterSplit(node, newNode);
    }
    
    // Versão de insert que não faz reinsert (para evitar recursão infinita)
    private void insertWithoutReinsert(Point p) {
        Entry e = new Entry(p);
        Node leaf = chooseLeaf(root, e);
        leaf.entries.add(e);
        leaf.expandMBR(e.mbr);

        if (leaf.entries.size() > maxEntries) {
            // Faz split diretamente sem tentar reinsert
            Node newNode = split(leaf);
            handleNewNodeAfterSplit(leaf, newNode);
        }
    }
    
    
    private void handleNewNodeAfterSplit(Node node, Node newNode) {

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
        Entry newEntry = new Entry(newNode);
        node.parent.entries.add(newEntry);
        // Otimização: atualiza MBR incrementalmente
        node.parent.expandMBR(newEntry.mbr);

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

    // Otimização: encontrar melhor ponto de split minimizando overlap
    int bestSplitIndex = findBestSplitIndex(copy, chooseByX);
    
    List<Entry> group1 = new ArrayList<>(copy.subList(0, bestSplitIndex));
    List<Entry> group2 = new ArrayList<>(copy.subList(bestSplitIndex, copy.size()));

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

// Encontra o melhor índice de split minimizando overlap e área total
private int findBestSplitIndex(List<Entry> sortedEntries, boolean byX) {
    int size = sortedEntries.size();
    int minEntries = Math.max(1, size / 4); // Garantir pelo menos 25% em cada grupo
    int maxEntries = size - minEntries;
    
    int bestIndex = size / 2;
    double bestScore = Double.POSITIVE_INFINITY;
    
    for (int i = minEntries; i <= maxEntries; i++) {
        // Calcular MBRs dos dois grupos
        Rectangle mbr1 = calculateMBR(sortedEntries.subList(0, i));
        Rectangle mbr2 = calculateMBR(sortedEntries.subList(i, size));
        
        // Score: overlap + área total (menor é melhor)
        double overlap = calculateOverlap(mbr1, mbr2);
        double totalArea = mbr1.area() + mbr2.area();
        double score = overlap + totalArea * 0.1; // Peso menor para área
        
        if (score < bestScore) {
            bestScore = score;
            bestIndex = i;
        }
    }
    
    return bestIndex;
}

private Rectangle calculateMBR(List<Entry> entries) {
    if (entries.isEmpty()) return null;
    Rectangle mbr = entries.get(0).mbr.copy();
    for (int i = 1; i < entries.size(); i++) {
        mbr.expandToInclude(entries.get(i).mbr);
    }
    return mbr;
}

private double calculateOverlap(Rectangle a, Rectangle b) {
    double overlapX = Math.max(0, Math.min(a.maxX, b.maxX) - Math.max(a.minX, b.minX));
    double overlapY = Math.max(0, Math.min(a.maxY, b.maxY) - Math.max(a.minY, b.minY));
    return overlapX * overlapY;
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
        
        // Otimização: se o MBR do nó está completamente dentro da query, adiciona todos os pontos
        boolean nodeFullyContained = contains(rect, node.mbr);
        
        if (node.isLeaf) {
            for (Entry e : node.entries) {
                if (e.point != null) {
                    if (nodeFullyContained) {
                        // Se o nó está completamente dentro, não precisa verificar cada ponto
                        out.add(e.point);
                    } else {
                        // Verificar se o ponto está realmente dentro do retângulo
                        if (e.point.x >= rect.minX && e.point.x <= rect.maxX && 
                            e.point.y >= rect.minY && e.point.y <= rect.maxY) {
                            out.add(e.point);
                        }
                    }
                }
            }
        } else {
            for (Entry e : node.entries) {
                if (nodeFullyContained || intersect(e.mbr, rect)) {
                    rangeQueryNode(e.child, rect, out);
                }
            }
        }
    }

    private boolean intersect(Rectangle a, Rectangle b) {
        return !(a.maxX < b.minX || a.maxY < b.minY || a.minX > b.maxX || a.minY > b.maxY);
    }

    // Verifica se o retângulo 'container' contém completamente o retângulo 'r'
    private boolean contains(Rectangle container, Rectangle r) {
        return r.minX >= container.minX && r.maxX <= container.maxX &&
               r.minY >= container.minY && r.maxY <= container.maxY;
    }
}
