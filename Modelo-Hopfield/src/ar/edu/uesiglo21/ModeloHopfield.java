package ar.edu.uesiglo21;

import java.util.Arrays;

/**
 * En esta clase, se implementa el modelo neuronal de Hopfield, utilizando la regla de Hebb.
 * Permite entrenar una red para almacenar patrones y posteriormente recuperarlos, incluso dañados o incompletos.
 * <br><br>
 * Utiliza representación matricial para mostrar visualmente el patrón por consola.
 */
public class ModeloHopfield {
    /** Matriz de pesos sinápticos entre neuronas. */
    private int[][] pesos;

    /** Cantidad total de neuronas (dimensión de la red). */
    private int tamano;

    /**
     * Constructor que inicializa la red neuronal con un tamaño específico.
     *
     * @param tamano Cantidad total de neuronas (pixeles).
     */
    public ModeloHopfield(int tamano) {
        this.tamano = tamano;
        pesos = new int[tamano][tamano];
    }

    /**
     * Entrena la red neuronal con los patrones dados utilizando la regla de aprendizaje de Hebb.
     * Los patrones deben estar en formato bipolar (1, -1).
     *
     * @param patrones Matriz con los patrones a almacenar en la red.
     */
    public void entrenar(int[][] patrones) {
        System.out.println("\n--- Entrenando la red neuronal ---");
        for (int idx = 0; idx < patrones.length; idx++) {
            System.out.println("Entrenando con el patrón " + (idx + 1));
            for (int i = 0; i < tamano; i++) {
                for (int j = 0; j < tamano; j++) {
                    if (i == j) {
                        pesos[i][j] = 0;    //la diagonal es siempre cero
                    } else {
                        pesos[i][j] += patrones[idx][i] * patrones[idx][j];
                    }
                }
            }
        }
        System.out.println("Entrenamiento finalizado.");
    }

    /**
     * Intenta recuperar un patrón almacenado a partir de una entrada dañada o incompleta.
     * Además, indica cuando se ha alcanzado un patrón estable.
     *
     * @param patron Patron inicial dañado o incompleto.
     * @param iteraciones Número de iteraciones máximas para estabilizar el patrón.
     * @return Patrón recuperado luego del proceso iterativo.
     */
    public int[] rellamar(int[] patron, int iteraciones) {
        int[] resultado = Arrays.copyOf(patron, tamano);
        System.out.println("\n--- Iniciando recuperación del patrón ---");
        for (int iteracion = 0; iteracion < iteraciones; iteracion++) {
            System.out.println("\nIteración " + (iteracion + 1) + ":");
            int[] nuevoResultado = new int[tamano];
            for (int i = 0; i < tamano; i++) {
                int suma = 0;
                for (int j = 0; j < tamano; j++) {
                    suma += pesos[i][j] * resultado[j];
                }
                nuevoResultado[i] = suma >= 0 ? 1 : -1;
            }

            // Verificación de estabilidad (punto fijo)
            if (Arrays.equals(nuevoResultado, resultado)) {
                System.out.println("☺ Patrón estable alcanzado en iteración " + (iteracion + 1));
                printPatron(nuevoResultado, (int)Math.sqrt(tamano));
                break;
            }

            resultado = nuevoResultado;
            printPatron(resultado, (int)Math.sqrt(tamano));
        }
        System.out.println("--- Recuperación completada ---");
        return resultado;
    }


    /**
     * Muestra visualmente un patrón como imagen matricial en la consola.
     * Utiliza "●" (círculo negro U+25CF) para píxeles activados y "○" (círculo blanco U+25CB) para píxeles desactivados.
     * Además, incorpora una matriz de fondo para una mejor visualización.
     *
     * @param patron Patrón a mostrar.
     * @param ancho Ancho de la imagen matricial.
     */
    public static void printPatron(int[] patron, int ancho) {
        String bordeSuperior = "┌" + "───┬".repeat(ancho - 1) + "───┐";
        String bordeIntermedio = "├" + "───┼".repeat(ancho - 1) + "───┤";
        String bordeInferior = "└" + "───┴".repeat(ancho - 1) + "───┘";

        System.out.println(bordeSuperior);
        for (int i = 0; i < patron.length; i++) {
            System.out.print("│ " + (patron[i] == 1 ? "●" : "○") + " ");
            if ((i + 1) % ancho == 0) {
                System.out.println("│");
                if (i < patron.length - 1) {
                    System.out.println(bordeIntermedio);
                }
            }
        }
        System.out.println(bordeInferior);
    }


    /**
     * Método main que ejecuta un ejemplo de entrenamiento y recuperación de patrones.
     *
     * @param args Argumentos estándar de ejecución (no utilizados en este ejemplo).
     */
    public static void main(String[] args) {
        int tamano = 9; // Matriz de 3x3 píxeles

        int[][] patrones = {
                { 1, -1,  1,
                        -1,  1, -1,
                        1, -1,  1 },

                {-1,  1, -1,
                        1, -1,  1,
                        -1,  1, -1 }
        };

        ModeloHopfield modelo = new ModeloHopfield(tamano);
        modelo.entrenar(patrones);

        // Patrón inicial (sucio o dañado)
        int[] patronSucio = {
                1, -1,  1,
                -1, -1, -1,
                1, -1,  1 };

        System.out.println("\nPatrón dañado inicial:");
        printPatron(patronSucio, 3);

        int[] patronRecuperado = modelo.rellamar(patronSucio, 5);

        System.out.println("\nPatrón final recuperado / reconstruido:");
        printPatron(patronRecuperado, 3);
    }
}
