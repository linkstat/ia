package ar.edu.uesiglo21;

import java.util.Arrays;
import java.util.Scanner;
import Jama.Matrix;

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
     * Los patrones deben estar en el formato: (1, -1).
     *
     * @param patrones Matriz con los patrones a almacenar en la red.
     */
    public void entrenarHebb(int[][] patrones) {
        System.out.println("\n☻☻☻  Entrenando la red neuronal con Hebb...  ☻☻☻");
        for (int idx = 0; idx < patrones.length; idx++) {
            int[] p = patrones[idx];
            System.out.println("Entrenando con el patrón " + (idx + 1));
            for (int i = 0; i < tamano; i++) {
                for (int j = 0; j < tamano; j++) {
                    if (i != j) {
                        pesos[i][j] += p[i] * p[j];
                    }
                }
            }
        }
        System.out.println("√ ¡Finalizado! Red neuronal entrenada con Hebb.");
    }


    /**
     * Entrena la red neuronal de Hopfield utilizando la regla de la pseudoinversa de Moore-Penrose.
     * Este método permite almacenar patrones incluso si no son ortogonales entre sí, aumentando la capacidad de memoria.
     * Con un solo patrón no muestra ventajas sobre Hebb, por eso resulta de utilidad cuando se usa con dos o más
     * patrones linealmente independientes.
     *
     * @param patrones Arreglo de patrones a almacenar, donde cada patrón es un vector bipolar (1, -1).
     */
    public void entrenarPseudoinversa(int[][] patrones) {
        System.out.println("\n☺☺☺  Entrenando la red neuronal con pseudoinversa...  ☺☺☺");
        int q = patrones.length;
        int n = patrones[0].length;

        // Esta verificación es necesaria, porque cuando hay solo un patrón, la red tiende a un resultado saturado (todos 1)
        if (q < 2) {
            System.out.println("⚠ Se recomienda al menos dos patrones para usar pseudoinversa. Usando regla de Hebb en su lugar.");
            entrenarHebb(patrones);
            return;
        }

        // Construir matriz U (cada columna es un patrón)
        double[][] uData = new double[n][q];
        for (int p = 0; p < q; p++) {
            for (int i = 0; i < n; i++) {
                uData[i][p] = patrones[p][i];
            }
        }

        Matrix U = new Matrix(uData);                     // U : [n x q]
        Matrix UT = U.transpose();                        // U^T : [q x n]

        // Calcular pseudoinversa:   U† = (U^T * U)^-1 * U^T
        Matrix UTU = UT.times(U);               // [q x q]
        Matrix UTUinv = null;
        try {
            UTUinv = UTU.inverse();             // [q x q]
        } catch (RuntimeException e) {
            System.out.println("× No se pudo invertir U^T * U. Esto pasa cuando no hay independencia lineal entre los patrones");
            return;
        }
        Matrix Udag = UTUinv.times(UT);         // [q x n]

        // Calcular matriz de pesos:  W = U * U†   ( [n x q] x [q x n] = [n x n] )
        Matrix W = U.times(Udag);               // [n x n]

        // Anular diagonal
        for (int i = 0; i < W.getRowDimension(); i++) {
            W.set(i, i, 0);
        }

        /*
        //DEBUG: Matriz sin redondear
        System.out.println("→ Matriz de pesos W (sin redondear):");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.printf("%7.3f ", W.get(i, j));
            }
            System.out.println();
        }
        */

        // Guardar matriz de pesos en int[][]
        pesos = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double w = W.get(i, j);
                pesos[i][j] = (w > 0.01) ? 1 : (w < -0.01) ? -1 : 0;
                //pesos[i][j] = (int)Math.round(W.get(i, j)); //este método de reondeo es problemático. Lo dejo comentado para no volver a usarlo
            }
        }
        System.out.println("√ ¡Finalizado! Red neuronal entrenada con pseudoinversa.");
    }

    /**
     * Intenta recuperar un patrón almacenado a partir de una entrada dañada o incompleta.
     * Además, indica cuando se ha alcanzado un patrón estable.
     *
     * @param patron Patron inicial dañado o incompleto.
     * @param iteraciones Número de iteraciones máximas para estabilizar el patrón.
     * @return Patrón recuperado luego del proceso iterativo.
     */
    public int[] rellamarSincronico(int[] patron, int iteraciones) {
        int[] resultado = Arrays.copyOf(patron, tamano);
        System.out.println("\n♦♦  Iniciando recuperación del patrón...  ♦♦");
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
                System.out.println("Patrón estable alcanzado en iteración " + (iteracion + 1) +"♣♣♣♣");
                printPatron(nuevoResultado, (int)Math.sqrt(tamano));
                break;
            }

            resultado = nuevoResultado;
            printPatron(resultado, (int)Math.sqrt(tamano));
        }
        System.out.println("\n♦♦  ¡Reconstrucción completada!  ♦♦");
        return resultado;
    }


    /**
     * Recupera un patrón con actualización secuencial (asincrónica).
     * Permite ver claramente cómo evoluciona el patrón neurona por neurona.
     * @param patron Patrón dañado.
     * @param iteraciones Iteraciones máximas.
     * @return Patrón recuperado.
     */
    public int[] rellamarAsincronico(int[] patron, int iteraciones, int ancho) {
        int[] resultado = Arrays.copyOf(patron, tamano);

        System.out.println("\n--- Iniciando recuperación (actualización secuencial) ---");
        printPatron(resultado, ancho);

        for (int iteracion = 0; iteracion < iteraciones; iteracion++) {
            System.out.println("\nIteración " + (iteracion + 1) + " (neurona por neurona):");

            boolean cambios = false;

            for (int i = 0; i < tamano; i++) {
                int sum = 0;
                for (int j = 0; j < tamano; j++) {
                    sum += pesos[i][j] * resultado[j];
                }

                int valorAnterior = resultado[i];
                resultado[i] = sum >= 0 ? 1 : -1;

                // Mostrar si la neurona cambia
                if (valorAnterior != resultado[i]) {
                    cambios = true;
                    System.out.println("\n☼ Cambio en la neurona " + i + ":");
                    System.out.println("Antes:");
                    resultado[i] = valorAnterior;
                    printPatron(resultado, ancho);
                    resultado[i] = sum >= 0 ? 1 : -1;
                    System.out.println("Después:");
                    printPatron(resultado, ancho);
                }
            }

            if (!cambios) {
                System.out.println("\n√ Patrón estable alcanzado en iteración " + (iteracion + 1));
                break;
            }
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
    public static void printPatronEnCuadricula(int[] patron, int ancho) {
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
     * Muestra visualmente un patrón como imagen matricial en la consola.
     * Utiliza "●" (círculo negro U+25CF) para píxeles activados y "○" (círculo blanco U+25CB) para píxeles desactivados.
     * No dibuja la matriz (como un recuadro). La idea es que ocupe menos espacio
     *
     * @param patron Patrón a mostrar.
     * @param ancho Ancho de la imagen matricial.
     */
    public static void printPatron(int[] patron, int ancho) {
        for (int i = 0; i < patron.length; i++) {
            System.out.print(patron[i] == 1 ? "█" : "∙");
            if ((i + 1) % ancho == 0) System.out.println();
        }
    }

    /**
     * Verifica la similitud entre dos patrones y muestra advertencia si son poco ortogonales.
     * @param patron1 Primer patrón de memoria.
     * @param patron2 Segundo patrón de memoria.
     * @param umbral Umbral de advertencia (recomendado: 0.5)
     */
    public static void verificarSimilitudPatrones(int[] patron1, int[] patron2, double umbral) {
        if (patron1 == null || patron2 == null || patron1.length != patron2.length) {
            System.out.println("No se pueden comparar patrones: alguno es nulo o de diferente longitud.");
            return;
        }
        int suma = 0;
        for (int i = 0; i < patron1.length; i++) {
            suma += patron1[i] * patron2[i];
        }
        double similitud = (double) suma / patron1.length;

        System.out.printf("→ Similitud entre patrones: %.2f\n", similitud);

        if (Math.abs(similitud) > umbral) {
            System.out.println("⚠ ADVERTENCIA: Los patrones agregados son demasiado similares.");
            System.out.println("  Un valor alto (positivo o negativo) significa patrones poco independientes y potencialmente problemáticos para la red de Hopfield.");
            System.out.println("  Esto puede provocar confusión en la red de Hopfield.");
        } else {
            System.out.println("✓ Los patrones son suficientemente diferentes. Va a funcionar...");
        }
    }


    /**
     * Método main que ejecuta un ejemplo de entrenamiento y recuperación de patrones.
     * El ejemplo, es el patron dado en la situación práctica planteada
     * @param args Argumentos estándar de ejecución (no utilizados en este ejemplo).
     */
    public static void main(String[] args) {
        // matriz 10x10
        int ancho = 10;
        int tamano = ancho * ancho; // total: 100 píxeles

        // Patrón ideal (sería el motor en su posición ideal, acorde al ejemplo del caso de estudio)
        int[] patronLimpio = {
                -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                -1,-1,-1, 1, 1,-1,-1,-1,-1,-1,
                -1,-1, 1, 1, 1, 1,-1,-1,-1,-1,
                -1, 1, 1,-1,-1, 1, 1,-1,-1,-1,
                -1, 1, 1,-1,-1, 1, 1,-1,-1,-1,
                -1,-1, 1, 1, 1, 1,-1,-1,-1,-1,
                -1,-1,-1, 1, 1,-1,-1,-1,-1,-1,
                 1,-1,-1,-1,-1,-1,-1,-1,-1,-1,  // ref: elemento fijo está en pos. 80
                 1, 1,-1,-1,-1,-1,-1,-1,-1,-1   // ref: elementos fijos en pos. 90 y 91
        };
        // Elemento fijo de referencia (esquina inferior izquierda). Lo defino aquí, para asegurarme de que realmente es
        // un elemento fijo de referencia (por ejemplo, en caso de que al definir patronLimpio esas pos. no estén en 1).
        patronLimpio[80] = 1;
        patronLimpio[90] = 1;
        patronLimpio[91] = 1;

        //int[][] patrones = { patronLimpio };7

        // Segundo patrón: una variante desplazada y con otros detalles (necesario para probar pseudoinversa)
        int[] patronLimpio2 = {
                 1, 1, 1,-1, 1, 1, 1,-1, 1, 1,
                -1,-1, 1, 1, 1,-1, 1, 1, 1,-1,
                 1, 1, 1,-1, 1, 1, 1,-1,-1, 1,
                 1, 1,-1,-1,-1,-1, 1, 1, 1, 1,
                 1,-1,-1, 1, 1,-1,-1, 1,-1, 1,
                 1,-1,-1, 1, 1,-1,-1, 1, 1, 1,
                 1, 1,-1,-1,-1,-1, 1, 1,-1, 1,
                 1,-1,-1,-1,-1, 1,-1, 1,-1, 1,
                 1, 1, 1, 1,-1,-1, 1, 1, 1, 1,  // ref: elemento fijo está en pos. 80
                 1, 1, 1, 1, 1, 1, 1, 1,-1, 1   // ref: elementos fijos en pos. 90 y 91
        };
        patronLimpio2[80] = 1;
        patronLimpio2[90] = 1;
        patronLimpio2[91] = 1;

        // Patrón inicial dañado (nuevamente, siguiendo el ejemplo dado en las consignas del TP)
        // Dado que el patron dañado es básicamente el patrón limpio, pero desplazado unas 3 posiciones a la derecha y
        // con algo de suciedad en otras partes, vamos a generar nuestro patronSucio a partir del limpio (para no tener
        // que escribirlo manualmente, que también sería válido a los fines de probar el prototipo)
        int[] patronSucio = new int[patronLimpio.length];
        System.arraycopy(patronLimpio, 0, patronSucio, 3, patronLimpio.length - 3); //desplazamiento de 3 lugares

        // Mantenemos las posiciones fijas (80, 90, 91)
        patronSucio[80] = patronLimpio[80];
        patronSucio[90] = patronLimpio[90];
        patronSucio[91] = patronLimpio[91];

        // Definimos posiciones con ruido (acorde a ejemplo de las consignas del TP3)
        int[] posicionesConSuciedad = {
                0, 7,
                11, 14,
                31,
                61,
                82, 86, 89,
                93};

        // Aplicamos ruido a las posiciones específicas
        for (int pos : posicionesConSuciedad) {
            patronSucio[pos] = (patronSucio[pos] == 1) ? -1 : 1;
        }


        // Modelos Hopfield (Hebb y Pseudoinversa)
        //ModeloHopfield modeloHebb = new ModeloHopfield(tamano);
        //ModeloHopfield modeloPseudo = new ModeloHopfield(tamano);

        Scanner sc = new Scanner(System.in);
        boolean usarDosPatrones = false;
        boolean salir = false;
        while (!salir) {
            System.out.println("\nPrototipo de implementación del modelo neuronal de Hopfield\n");
            System.out.println("=================");
            System.out.println("MENÚ DE OPCIONES:");
            System.out.println("=================");
            System.out.println("1. Entrenar con Hebb y mostrar patrón recuperado.");
            System.out.println("2. Entrenar con Hebb y mostrar patrón paso a paso (asíncrono).");
            System.out.println("3. Entrenar con Pseudoinversa y mostrar patrón recuperado.");
            System.out.println("4. Entrenar con Pseudoinversa y mostrar patrón paso a paso (asíncrono).");
            System.out.printf("5. Mostrar %s.\n", !usarDosPatrones ? "patrón limpio" : "patrones limpios");
            System.out.println("6. Mostrar patrón dañado (hardcodeado).");
            System.out.printf("7. %s un segundo patrón de memoria.\n", usarDosPatrones ? "Quitar" : "Agregar");
            System.out.println("\n0. Salir.");
            System.out.println("=================");
            System.out.print("¿Su opción? [0-6]: ");
            int opcion = sc.hasNextInt() ? sc.nextInt() : -1;
            sc.nextLine();

            int[][] patrones = usarDosPatrones ? new int[][]{patronLimpio, patronLimpio2} : new int[][]{patronLimpio};
            ModeloHopfield modeloHebb = new ModeloHopfield(tamano);
            ModeloHopfield modeloPseudo = new ModeloHopfield(tamano);

            switch (opcion) {
                case 1:
                    modeloHebb.entrenarHebb(patrones);
                    int[] recHebb = modeloHebb.rellamarSincronico(patronSucio, 10);
                    System.out.println("→ Patrón recuperado (Hebb):");
                    ModeloHopfield.printPatron(recHebb, ancho);
                    break;
                case 2:
                    modeloHebb.entrenarHebb(patrones);
                    int[] recHebbPaso = modeloHebb.rellamarAsincronico(patronSucio, 10, ancho);
                    System.out.println("→ Patrón recuperado paso a paso (Hebb):");
                    ModeloHopfield.printPatron(recHebbPaso, ancho);
                    break;
                case 3:
                    modeloPseudo.entrenarPseudoinversa(patrones);
                    int[] recPseudo = modeloPseudo.rellamarSincronico(patronSucio, 10);
                    System.out.println("→ Patrón recuperado (Pseudoinversa):");
                    ModeloHopfield.printPatron(recPseudo, ancho);
                    break;
                case 4:
                    modeloPseudo.entrenarPseudoinversa(patrones);
                    int[] recPseudoPaso = modeloPseudo.rellamarAsincronico(patronSucio, 10, ancho);
                    System.out.println("→ Patrón recuperado paso a paso (Pseudoinversa):");
                    ModeloHopfield.printPatron(recPseudoPaso, ancho);
                    break;
                case 5:
                    System.out.println("\n→ Patrón limpio:");
                    ModeloHopfield.printPatron(patronLimpio, ancho);
                    if (usarDosPatrones) {
                        System.out.println("\n→ Segundo patrón limpio:");
                        ModeloHopfield.printPatron(patronLimpio2, ancho);
                    }
                    break;
                case 6:
                    System.out.println("→ Patrón dañado (entrada):");
                    ModeloHopfield.printPatron(patronSucio, ancho);
                    break;
                case 7:
                    usarDosPatrones = !usarDosPatrones;
                    System.out.printf("%s el segundo patrón de memoria. Ahora se %s %d %s.\n",
                            usarDosPatrones ? "Agregado" : "Quitado",
                            usarDosPatrones ? "usan" : "usa",
                            usarDosPatrones ? 2 : 1,
                            usarDosPatrones ? "patrones" : "patrón");
                    if (usarDosPatrones) {
                        verificarSimilitudPatrones(patronLimpio, patronLimpio2, 0.6);
                    }
                    break;
                case 0:
                    System.out.println("\n\nDe aquí hasta reconocer rostros no paramos ;-)!!!\nSaludos profes!!!");
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida. De introducir un entero  entre 0 y 7.");
            }
        }
        sc.close();
    }

}
