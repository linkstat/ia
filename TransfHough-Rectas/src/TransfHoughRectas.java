import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TransfHoughRectas {

    private final int rhoMax;
    private final int[][] accumulator;
    private final double[] sinCache;
    private final double[] cosCache;
    private final int thetaMax = 180;

    private List<double[]> points = new ArrayList<>();

    public TransfHoughRectas(List<double[]> points, int width, int height) {
        this.points = points;

        rhoMax = (int) Math.hypot(width, height);
        accumulator = new int[2 * rhoMax][thetaMax];

        sinCache = new double[thetaMax];
        cosCache = new double[thetaMax];

        for (int theta = 0; theta < thetaMax; theta++) {
            double thetaRad = Math.toRadians(theta);
            sinCache[theta] = Math.sin(thetaRad);
            cosCache[theta] = Math.cos(thetaRad);
        }
    }

    // Realiza la acumulación de votos en el espacio de Hough
    public void performTransform() {
        for (double[] p : points) {
            double x = p[0];
            double y = p[1];
            for (int theta = 0; theta < thetaMax; theta++) {
                int rho = (int) Math.round(x * cosCache[theta] + y * sinCache[theta]) + rhoMax;
                if (rho >= 0 && rho < 2 * rhoMax)
                    accumulator[rho][theta]++;
            }
        }
    }

    // Devuelve el máximo de la acumulación (parámetros de la recta)
    public int[] getMaxAccumulator() {
        int max = 0;
        int rhoMaxFound = 0;
        int thetaMaxFound = 0;

        for (int rho = 0; rho < 2 * rhoMax; rho++) {
            for (int theta = 0; theta < thetaMax; theta++) {
                if (accumulator[rho][theta] > max) {
                    max = accumulator[rho][theta];
                    rhoMaxFound = rho - rhoMax;
                    thetaMaxFound = theta;
                }
            }
        }

        return new int[]{rhoMaxFound, thetaMaxFound, max};
    }

    // Método principal de ejemplo
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<double[]> points = new ArrayList<>();

        System.out.println("Deberá ingresar coordenadas para (x ; y)...\nIngrese dos números (enteros o decimales), de a pares, separados por un espacio.\nEjemplo: 0 1.5\nPuede dejar de introducir valores en cualquier momento, introduciendo la letra 'q'.");

        int contador = 1;
        while (true) {
            System.out.print("(x[" + contador + "] ; y[" + contador + "]) = ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) {
                break;
            }

            String[] parts = input.split("\\s+");
            if (parts.length != 2) {
                System.out.println("¡Error! Deben ingresarse dos valores numéricos separados por espacio.");
                continue;
            }

            try {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                points.add(new double[]{x, y});
                contador++;
            } catch (NumberFormatException e) {
                System.out.println("¡Error! Ingresar números válidos.");
            }
        }

        if (points.isEmpty()) {
            System.out.println("No se ingresaron coordenadas de puntos.");
            scanner.close();
            return;
        }

        // Mostrar resumen de puntos ingresados
        System.out.println("\nValores ingresados:");
        for (int i = 0; i < points.size(); i++) {
            double[] point = points.get(i);
            System.out.printf("[%d] (x ; y) = (%.1f ; %.1f)\n", i+1, point[0], point[1]);
        }
        System.out.println();

        // Determinar límites aproximados para espacio Hough
        int width = (int) points.stream().mapToDouble(p -> p[0]).max().getAsDouble() + 10;
        int height = (int) points.stream().mapToDouble(p -> p[1]).max().getAsDouble() + 10;

        TransfHoughRectas hough = new TransfHoughRectas(points, width, height);
        hough.performTransform();

        int[] lineParameters = hough.getMaxAccumulator();

        if (lineParameters[2] > 1) { // al menos dos puntos formando línea
            System.out.print("Recta detectada con parámetros: ");
            System.out.println("(ρ ; θ) = (" + lineParameters[0] +" ; "+ lineParameters[1]+"°)");
            System.out.println("Puntos alineados: " + lineParameters[2]);
        } else {
            System.out.println("No se detectó una recta clara.");
        }

        scanner.close();
    }

}
