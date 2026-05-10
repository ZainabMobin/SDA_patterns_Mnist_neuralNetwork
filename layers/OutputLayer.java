package layers;

import java.util.Arrays;

public class OutputLayer extends Layer
{
    // Gradients
        // Local Grqdient (1 x n)      output - true output
    private double[] dL_dz;  // dL/dz = a - y   
    private double[] dL_dx;  // downstream gradient (will send to previous layer) dL/dx = dL/dz x dz/dx = dL/dz x WT  (1 x n) x (m x n)T = (1 x m)

    // Gradients to Update Weights and Biases
                                // m x n =  m x 1     1 x n
    private double[][] dL_dW;   // dL/dW = (dz/dW)T x dL/dz = (x)T x dL/dz 
    private double[] dL_db;     // dL/db = dL/dz x dz/db = dL/dz x 1     (1 x n) = (1 x n) x 1 , dz/db = 1


    public OutputLayer(double[] input, int outputSize)
    {
        super(input, outputSize);
        
        dL_dz = new double[outputSize];
        dL_dx = new double[inputSize];

        dL_dW = new double[inputSize][outputSize];
        dL_db = new double[outputSize];
    }


    public void calculateLocalGradient(double[] trueOutput) // Loss L is a scalar values, trueOutput (1 x n) hot coded vector
    {
        // Local Gradient

        for ( int i = 0; i < outputSize; i++)
        
            dL_dz[i] = actOutput[i] - trueOutput[i]; // derivative of softmax + cross entropy loss (1 x n)


        // Weight Gradient
                                        // (m x n) = (m x 1) x (1 x n)
        super.matrix_operations.vecTransposeXplyVec(this.getInput(), dL_dz, dL_dW); // dL/dW = (dz/dW)T x dL/dz = (x)T x dL/dz

        // Bias Gradient
        for ( int i = 0; i < outputSize; i++)
        
            dL_db[i] = dL_dz[i];            // dL/db = dL_dz -> 1 x n
        

        // Downstream Gradient
        super.matrix_operations.vecXplyMatrixTranspose(dL_dz, this.getWeights(), dL_dx); // dL/dx = dL/dz x dz/dx , dz/dx = WT -> 1 x m
        

    }


    @Override           // SOFTMAX ACTIVATION a = softmax(z) -> 1 x n
    protected void activation(double[] preActOutput, double[] actOutput) 
    {
        double max = Double.NEGATIVE_INFINITY;
        
        // Find maximum value for numerical stability
        for (int i = 0; i < outputSize; i++) {
            if (preActOutput[i] > max) {
                max = preActOutput[i];
            }
        }
        
        double sum = 0.0;
        
        // Compute exponentials (shifted by max to avoid overflow)
        for (int i = 0; i < outputSize; i++) {
            actOutput[i] = Math.exp(preActOutput[i] - max);
            sum += actOutput[i];
        }
        
        // Normalize to probabilities
        for (int i = 0; i < outputSize; i++) {
            actOutput[i] /= sum;
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