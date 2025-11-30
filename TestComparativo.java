import java.util.*;

public class TestComparativo {

    static Random rnd = new Random(12345);
    // Configurações Globais
    static final int REPS = 100; // Aumentado para 100 repetições
    static final int[] SIZES = { 10000, 50000, 100000, 250000, 500000, 1000000, 2000000, 5000000, 10000000 }; // Cenários de N
    static final double SPACE = 1000.0;

    public static void main(String[] args) {
        System.out.println("Cenario;N;QueryFrac;Linear_Avg(ms);Linear_Std(ms);Quad_Avg(ms);Quad_Std(ms);RTree_Avg(ms);RTree_Std(ms)");

        // Loop pelos tamanhos de N (10k, 50k, 100k)
        for (int n : SIZES) {
            rodarCenario("Uniforme", n, SPACE, false);
            rodarCenario("Clusterizado", n, SPACE, true);
        }
    }

    static void rodarCenario(String nome, int N, double space, boolean cluster) {
        // Gerar pontos
        List<Point> pts = gerarPontos(N, space, cluster);

        // Construir estruturas
        Linear linear = new Linear();
        QuadTree quad = new QuadTree(new Rectangle(0, 0, space, space));
        RStarTree rstar = new RStarTree(16); // Otimizado: maxEntries reduzido de 100 para 16

        for (Point p : pts) {
            linear.insert(p);
            quad.insert(p);
            rstar.insert(p);
        }

        double[] fracs = {0.01, 0.05, 0.2};

        for (double frac : fracs) {
            // Arrays para armazenar tempos de cada repetição
            double[] tLinear = new double[REPS];
            double[] tQuad = new double[REPS];
            double[] tRStar = new double[REPS];

            // Executar repetições
            for (int i = 0; i < REPS; i++) {
                Rectangle q = gerarQueryAleatoria(space, frac, pts, cluster);

                // Medir Linear
                long t0 = System.nanoTime();
                linear.rangeQuery(q);
                tLinear[i] = (System.nanoTime() - t0) / 1_000_000.0; // Converter para ms

                // Medir QuadTree
                t0 = System.nanoTime();
                quad.rangeQuery(q);
                tQuad[i] = (System.nanoTime() - t0) / 1_000_000.0;

                // Medir R*Tree
                t0 = System.nanoTime();
                rstar.rangeQuery(q);
                tRStar[i] = (System.nanoTime() - t0) / 1_000_000.0;
            }

            // Calcular Estatísticas
            double avgLinear = mean(tLinear);
            double stdLinear = stdDev(tLinear, avgLinear);
            
            double avgQuad = mean(tQuad);
            double stdQuad = stdDev(tQuad, avgQuad);

            double avgRStar = mean(tRStar);
            double stdRStar = stdDev(tRStar, avgRStar);

            // Output formatado CSV (ponto e vírgula para fácil importação no Excel)
            System.out.printf(Locale.US, "%s;%d;%.2f;%.4f;%.4f;%.4f;%.4f;%.4f;%.4f\n",
                nome, N, frac, 
                avgLinear, stdLinear, 
                avgQuad, stdQuad, 
                avgRStar, stdRStar
            );
        }
    }

    // Funções Auxiliares de Estatística
    static double mean(double[] data) {
        double sum = 0;
        for (double d : data) sum += d;
        return sum / data.length;
    }

    static double stdDev(double[] data, double mean) {
        double sumSq = 0;
        for (double d : data) sumSq += Math.pow(d - mean, 2);
        return Math.sqrt(sumSq / data.length);
    }

    static List<Point> gerarPontos(int N, double space, boolean cluster) {
        List<Point> list = new ArrayList<>(N);
        if (!cluster) {
            for (int i = 0; i < N; i++) {
                list.add(new Point(i, rnd.nextDouble() * space, rnd.nextDouble() * space));
            }
        } else {
            double[][] centers = {
                {space * 0.25, space * 0.25}, {space * 0.75, space * 0.25},
                {space * 0.25, space * 0.75}, {space * 0.75, space * 0.75},
                {space * 0.50, space * 0.50},
            };
            for (int i = 0; i < N; i++) {
                int c = rnd.nextInt(centers.length);
                double x = centers[c][0] + rnd.nextGaussian() * (space * 0.05);
                double y = centers[c][1] + rnd.nextGaussian() * (space * 0.05);
                list.add(new Point(i, x, y));
            }
        }
        return list;
    }

    static Rectangle gerarQueryAleatoria(double space, double frac, List<Point> pontos, boolean cluster) {
        double size = space * frac;
        if (cluster && !pontos.isEmpty()) {
            Point p = pontos.get(rnd.nextInt(pontos.size()));
            double x = Math.max(0, Math.min(p.x - size/2.0, space - size));
            double y = Math.max(0, Math.min(p.y - size/2.0, space - size));
            return new Rectangle(x, y, x + size, y + size);
        } else {
            double x = rnd.nextDouble() * (space - size);
            double y = rnd.nextDouble() * (space - size);
            return new Rectangle(x, y, x + size, y + size);
        }
    }
}