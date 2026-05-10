package layers;

import java.util.Arrays;

public class HiddenLayer extends Layer
{
    // Gradients
    private double[] dL_da;  // upstream gradient (will receive from next layer) 1 x n vector
    private double[] dL_dz;  // local gradient dL/dz = dL/da x (da/dz)T (1 x n) x (n x 1) = n x n (here->1 x n, we work implicitly, element wise xply)  
    private double[] dL_dx;  // downstream gradient (will send to previous layer) dL/dx = dL/dz x dz/dx = dL/dz x WT  (1 x n) x (m x n)T = (1 x m)

    // Gradients to Update Weights and Biases
                                // m x n =  m x 1     1 x n
    private double[][] dL_dW;   // dL/dW = (dz/dW)T x dL/dz = (x)T x dL/dz 
    private double[] dL_db;     // dL/db = dL/dz x dz/db = dL/dz x 1     (1 x n) = (1 x n) x 1 , dz/db = 1


    public HiddenLayer(double[] input, int outputSize)
    {
        super(input, outputSize);
        
        dL_dz = new double[outputSize];
        dL_dx = new double[inputSize];

        dL_dW = new double[inputSize][outputSize];
        dL_db = new double[outputSize];
    }


                               // dL/da is upstream gradient
    public void calculateLocalGradient(double[] upstreamGradient)
    {
        this.dL_da = upstreamGradient;  // 1 x n

        double[] da_dz = new double[outputSize]; // intermediate derivitive, needed to calc dL/dz
        ReLuDerivitive(preActOutput, da_dz);   // da_dz = d/dz (a(z)) = a'(z)  -> 1 x n


        // Local Gradient
        super.matrix_operations.vecXplyElementWise(dL_da, da_dz, dL_dz);  // dL/dz = dL/da x da/dz (element-wise xply) -> 1 x n 
                                                                            

        // Weight Gradient
                                        // (m x n) = (m x 1) x (1 x n)
        super.matrix_operations.vecTransposeXplyVec(this.getInput(), dL_dz, dL_dW); // dL/dW = (dz/dW)T x dL/dz = (x)T x dL/dz

        // Bias Gradient
        for ( int i = 0; i < outputSize; i++)
        
            dL_db[i] = dL_dz[i];            // dL/db = dL_dz -> 1 x n
        

        // Downstream Gradient
        super.matrix_operations.vecXplyMatrixTranspose(dL_dz, this.getWeights(), dL_dx); // dL/dx = dL/dz x dz/dx , dz/dx = WT -> 1 x m
        
        
    }

    @Override     // ReLu Activation  a = relu(z)   -> 1 x n
    protected void activation(double[] preActOutput, double[] actOutput)
    {
        for ( int i = 0; i < outputSize; i++)
        {
            if (preActOutput[i] <= 0)

                actOutput[i] = 0;
            else

                actOutput[i] = preActOutput[i];
        }
    }


    private void ReLuDerivitive(double[] preActOutput, double[] da_dz) 
    {
        
        for ( int i = 0; i < outputSize; i++)
        {
            if (preActOutput[i] <= 0)

                da_dz[i] = 0;
            else

                da_dz[i] = 1;
        }
    }

    @Override
    public void resetGradients() {
    Arrays.fill(dL_dz, 0.0);
    Arrays.fill(dL_dx, 0.0);
    for (int i = 0; i < inputSize; i++) {
        Arrays.fill(dL_dW[i], 0.0);
    }
    Arrays.fill(dL_db, 0.0);
}

    public double[] getUpstreamGradient() { return dL_dx; }

    public double[][] getdL_dW() { return dL_dW; }

    public double[] getdL_db() { return dL_db; }
}