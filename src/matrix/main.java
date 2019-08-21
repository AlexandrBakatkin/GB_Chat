package matrix;

public class main {
    public static void main(String[] args) {
        int [][] matrix = new int [6][9];
        int count = 1;
        int iter;

        if (matrix.length < matrix[0].length){
            iter = matrix.length/2 + 1;
        } else {
            iter = matrix[0].length/2 + 1;
        }

        for (int n = 1; n < iter; n++) {

            for (int i = n - 1; i < matrix[0].length - n; i++) {
                matrix[n - 1][i] = count;
                count++;
            }

            for (int j = n - 1; j < matrix.length - n; j++) {
                matrix[j][matrix[0].length - n] = count;
                count++;
            }

            for (int i = matrix[0].length; i > n + (n-1); i--) {
                matrix[matrix.length - n][i - n] = count;
                count++;
            }

            for (int j = matrix.length; j > n + (n-1); j--) {
                matrix[j - n][n - 1] = count;
                count++;
            }
        }

        if (matrix.length%2 != 0){
            matrix[iter - 1][iter - 1] = count;
        }

        printMatrix(matrix);
    }

    private static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] < 10){
                    System.out.print("[ " + matrix[i][j] + "]");
                } else {
                    System.out.print("[" + matrix[i][j] + "]");
                }
            }
            System.out.println();
        }
    }
}