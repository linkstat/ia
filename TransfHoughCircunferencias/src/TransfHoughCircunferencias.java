import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TransfHoughCircunferencias {

    private final int[][] accumulator;
    private final int width, height, radius;

    private List<double[]> points = new ArrayList<>();

    public TransfHoughCircunferencias(List<double[]> points, int width, int height, int radius) {
        this.points = points;
        this.width = width;
        this.height = height;
        this.radius = radius;
        accumulator = new int[width][height];
    }

    public void performTransform() {
        for (double[] p : points) {
            double x = p[0];
            double y = p[1];

            for (int theta = 0; theta < 360; theta++) {
                int a = (int) Math.round(x - radius * Math.cos(Math.toRadians(theta)));
                int b = (int) Math.round(y - radius * Math.sin(Math.toRadians(theta)));

                if (a >= 0 && a < width && b >= 0 && b < height) {
                    accumulator[a][b]++;
                }
            }
        }
    }

    public int[] getMaxAccumulator() {
        int max = 0;
        int aMaxFound = 0;
        int bMaxFound = 0;

        for (int a = 0; a < width; a++) {
            for (int b = 0; b < height; b++) {
                if (accumulator[a][b] > max) {
                    max = accumulator[a][b];
                    aMaxFound = a;
                    bMaxFound = b;
                }
            }
        }

        return new int[]{aMaxFound, bMaxFound, max};
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<double[]> points = new ArrayList<>();

        System.out.print("\nIngrese el radio conocido para la circunferencia: ");
        int radius = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Ingrese pares de coordenadas (x ; y).\nPuede dejar de introducir valores en cualquier momento, introduciendo la letra 'q'.");

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
            System.out.println("No se ingresaron coordenadas.");
            scanner.close();
            return;
        }

        System.out.println("\nValores ingresados:");
        for (int i = 0; i < points.size(); i++) {
            double[] point = points.get(i);
            System.out.printf("[%d] (x ; y) = (%.1f ; %.1f)\n", i + 1, point[0], point[1]);
        }
        System.out.println();

        int width = (int) points.stream().mapToDouble(p -> p[0]).max().getAsDouble() + radius + 10;
        int height = (int) points.stream().mapToDouble(p -> p[1]).max().getAsDouble() + radius + 10;

        TransfHoughCircunferencias hough = new TransfHoughCircunferencias(points, width, height, radius);
        hough.performTransform();

        int[] circleParams = hough.getMaxAccumulator();

        if (circleParams[2] > 1) {
            System.out.print("Centro detectado de la circunferencia: ");
            System.out.println("(a ; b) = (" + circleParams[0] + " ; " + circleParams[1] + ")");
            System.out.println("Puntos alineados con este centro: " + circleParams[2]);
        } else {
            System.out.println("No se detectó claramente una circunferencia.");
        }

        scanner.close();
    }
}