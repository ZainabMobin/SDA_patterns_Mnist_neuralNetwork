package layers;

import java.util.Arrays;

public class MatrixOperations
{
    private MatrixOperations() {} // private constructor to prevent instantiation
    private static MatrixOperations instance; // singleton instance

    public static MatrixOperations getInstance(){ // method to get singleton instance
        if ( instance == null ) {
            instance = new MatrixOperations();
        }
        return instance;
    }

    // (1 x m) x (m x n) = (1 x n)
    public void VecMatXply(double[] vector, double[][] matrix, double[] output)
    {
        int m = vector.length;
        int mm = matrix.length;
        int n = matrix[0].length;

        Arrays.fill(output, 0.0);

        if (m != mm)
        {
            System.err.println("Dimentions dont add up");
            return;
        }


        for ( int j = 0; j < n; j++)
        
            for (int i = 0; i < m; i++)
            
                output[j] += vector[i] * matrix[i][j];
            
    }
    
                                    //         vec1                 vec2           output -> 1 x n
    public void vecXplyElementWise(double[] dL_da, double[] da_dz, double[] dL_dz)
    {
        if (dL_da.length != da_dz.length && dL_da.length != dL_dz.length && da_dz.length != dL_dz.length)
        {
            System.err.println("Vectors have different length");
            return;
        }

        for (int i = 0; i < dL_dz.length; i++)

            dL_dz[i] = dL_da[i] * da_dz[i];
    }

                                        // dL/dW = (dz/dW)T x dL/dz = (x)T x dL/dz
    public void vecTransposeXplyVec(double[] input, double[] dL_dz, double[][] dL_dW) // (m x n) = (m x 1) x (1 x n)
    {
        int m = input.length;
        int n = dL_dz.length;

        for (int i = 0; i < m; i++)
        
            for (int j = 0; j < n; j++)
            
                dL_dW[i][j] = input[i] * dL_dz[j];

    }
                     
                       // dL/dx = dL/dz x dz/dx , dz/dx = WT                                      
    public void vecXplyMatrixTranspose(double[] dL_dz, double[][] weights, double[] dL_dx) // (1 x n) x (m x n)T = 1 x m
    {
        int n = dL_dz.length;
        int m = weights.length;
        int nn = weights[0].length;
        
        if (n != nn)
        {
            System.err.println("Dimentions dont add up T");
            return;
        }

        Arrays.fill(dL_dx, 0.0);

        for (int i = 0; i < m; i++)

            for ( int j = 0; j < n; j++)
            
                dL_dx[i] += dL_dz[j] * weights[i][j];
    }
}