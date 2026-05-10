package layers;

import java.util.Arrays;
import java.util.Random;

public abstract class Layer 
{
    private double[] input;        // x      1 x m input vector from prev layer
    private double[][] weights;    // W      m x n weight matrix
    private double[] bias;         // b      1 x n bias vector
    protected double[] preActOutput; // z = xW + b  -> 1 x n
    protected double[] actOutput;    // a = act(z)  -> 1 x n
    protected MatrixOperations matrix_operations; // instance of singleton MatrixOperations 

    private static Random rand;

    protected final int inputSize;
    protected final int outputSize;

    public Layer(double[] input, int outputSize)
    {
        rand = new Random();
        matrix_operations = MatrixOperations.getInstance();

        preActOutput = new double[outputSize]; // 1 x n
        actOutput = new double[outputSize];

        this.inputSize = input.length;
        this.outputSize = outputSize;

        this.input = input; // memory for input allocated in respective layer

        initWeights();
        initBiases();
    }

    public void calculateOutput() // z = xW + b -> 1 x n
    {
        
        matrix_operations.VecMatXply(input, weights, preActOutput); // (1 x m) x (m x n) = (1 x n)

        for (int i = 0; i < outputSize; i++)
        
            preActOutput[i] += bias[i];

         

        activation(preActOutput, actOutput);  // apply activation a = act(z)  -> 1 x n
    }

    protected abstract void activation(double[] preActOutput, double[] actOutput);
    public abstract void resetGradients();

    public void updateWeights(double[][] dL_dW, double LEARNING_RATE)
    {
        for (int i = 0; i < inputSize; i++)   // Gradient Decent
        
            for (int j = 0; j < outputSize; j++)
            
                weights[i][j] -= LEARNING_RATE * dL_dW[i][j];  // Wij : Wij - learning rate x dL/dWij 
            
    }

    public void updateBiases(double[] dL_db, double LEARNING_RATE)
    {
        for (int i = 0; i < outputSize; i++)   // Gradient Decent
        
            bias[i] -= LEARNING_RATE * dL_db[i];  // bi : bi - learning rate x dL/dbi

    }



    private void initWeights()
    {
        weights = new double[inputSize][outputSize]; // m x n

        double stddev = Math.sqrt(2.0 / inputSize);  // He initialization for ReLU
        

        for ( int i = 0; i < inputSize; i++ )
        
            for (int j = 0; j < outputSize; j++)
            
            weights[i][j] = rand.nextGaussian() * stddev;
            
        
    }

    private void initBiases()
    {
        bias = new double[outputSize];  // 1 x m

        Arrays.fill(bias, 0.0);
    }

    public void setInput(double[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input array cannot be null");
        }
        this.input = Arrays.copyOf(input, input.length);
    }

    // public void setInput(double[] input) {
    //     this.input = Arrays.copyOf(input, input.length);
    // }

    public double[] getInput() { return input; }

    public double[] getOutput() { return actOutput; }

    public double[][] getWeights() { return weights; }
}