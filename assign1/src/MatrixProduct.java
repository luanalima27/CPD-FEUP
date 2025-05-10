import java.util.Scanner;

public class MatrixProduct {

    // Multiplicação default
    public static void OnMul(int n) {
        double[][] A = new double[n][n];
        double[][] B = new double[n][n];
        double[][] C = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = 1.0;
                B[i][j] = i + 1;
            }
        }
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double temp = 0.0;
                for (int k = 0; k < n; k++) {
                    temp += A[i][k] * B[k][j];
                }
                C[i][j] = temp;
            }
        }
        
        long endTime = System.nanoTime();
        double seconds = (endTime - startTime) / 1e9;
        System.out.printf("Time (Standard Multiplication): %.3f seconds%n", seconds);
        
	// display 10 elements of the result matrix tto verify correctness
        System.out.print("Result matrix (first 10 elements of row 0): ");
        for (int j = 0; j < Math.min(10, n); j++) {
            System.out.print(C[0][j] + " ");
        }
        System.out.println();
    }


    // Multiplicação em linha
    public static void OnMultLine(int n) {
        double[][] A = new double[n][n];
        double[][] B = new double[n][n];
        double[][] C = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = 1.0;
                B[i][j] = i + 1;
            }
        }
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                for (int j = 0; j < n; j++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        
        long endTime = System.nanoTime();
        double seconds = (endTime - startTime) / 1e9;
        System.out.printf("Time (Line Multiplication): %.3f seconds%n", seconds);
        
        System.out.print("Result matrix (first 10 elements of row 0): ");
        for (int j = 0; j < Math.min(10, n); j++) {
            System.out.print(C[0][j] + " ");
        }
        System.out.println();
    }

    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int op;
        
        do {
            System.out.println("\nMenu:");
            System.out.println("1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("0. Quit");
            System.out.print("Selection?: ");
            op = sc.nextInt();
            
            if (op == 0) break;
            
            System.out.print("Dimensions: lins=cols ? ");
            int n = sc.nextInt();
            
            switch(op) {
                case 1:
                    OnMul(n);
                    break;
                case 2:
                    OnMultLine(n);
                    break;
                default:
                    System.out.println("Invalid option!");
            }
            
        } while (op != 0);
        
        sc.close();
    }
}