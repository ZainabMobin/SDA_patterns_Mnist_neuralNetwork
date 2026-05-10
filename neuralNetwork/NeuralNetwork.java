package neuralNetwork;

import java.util.List;

import data.ReaderInterface;
import data.Image;
import layers.HiddenLayer;
import layers.Layer;
import layers.OutputLayer;



// NEURAL NETWORK - Uses AdapterHandler for file reading
// interface reference used by client
// Fully decoupled from file format (CSV, JSON, Binary)
public class NeuralNetwork
{
    private Layer[] layers;  // array of all layers

    private List< Image > images; // training / testing images
    private double[] currInput;
    private double[] currOutput;
    private ReaderInterface reader;  // interface reference type for dynamic injection of handler at runtime

    private final int inputSize = 784;     // size of mnist image
    private final int[] hiddenLayers;      // size of each hidden
    private final int size;
    private final int OutputSize = 10;     // predicting 0-9 digits

    private final double LEARNING_RATE;



    public NeuralNetwork( int[] hidden, double LEARNING_RATE, ReaderInterface reader)
    {
        this.hiddenLayers = hidden;
        size = hiddenLayers.length + 1;  // hidden layers + 1 output layer

        this.LEARNING_RATE = LEARNING_RATE;
        this.reader = reader;
        
        currInput = new double[inputSize];

        initLayers();
    }

    public void train(String path)
    {
        images = this.reader.readData(path); //adapter handler parses path, automatically detects file types and returns processed images (recursive processing done in case of foolderpath])
    
        for (int i = 0; i < images.size(); i++) 
        {
            if (i % 5000 == 0) // print progress every 5000 samples, saves terminal space 
                System.out.println("Training sample: " + i);

            int label = images.get(i).getLabel();   // True label (0-9)
            currInput = images.get(i).getData();    // Input to the network
    
            double[] trueOutput = new double[OutputSize]; // hot coded vector
            trueOutput[label] = 1;
            
            forwardPass();

            backpropogation(trueOutput);
            
            updateWeightsAndBiases();
        }
        
    }

    public void test(String path) 
    {
        images = this.reader.readData(path); //adapter handler parses path, automatically detects file types and returns processed images (recursive processing done in case of foolderpath])

        int total = images.size();
        int correct = 0;
        
        for (int i = 0; i < images.size(); i++) 
        {
            if (i % 5000 == 0) // print progress every 5000 samples, saves terminal space 
                System.out.println("Testing sample: " + i);

            int label = images.get(i).getLabel();   // True label (0-9)
            currInput = images.get(i).getData();    // Input to the network
    
            
            forwardPass();
            
            // Find predicted label (index of max output probability)
            int predicted = 0;
            double maxProb = currOutput[0];
            for (int j = 1; j < OutputSize; j++) {
                if (currOutput[j] > maxProb) {
                    maxProb = currOutput[j];
                    predicted = j;
                }
            }
    
            // Check if prediction matches true label
            if (label == predicted)
                correct++;
        }
    
        // Calculate and print accuracy
        double accuracy = (correct * 100.0) / total;
        System.out.println("Total test samples: " + total);
        System.out.println("Correct predictions: " + correct);
        System.out.println("Accuracy: " + accuracy + "%");
    }

    private void forwardPass() {
        // Propagate input through the network
        layers[0].setInput(currInput);  // Set input for first layer
        
        for (int i = 0; i < layers.length; i++) {
            layers[i].calculateOutput();
            if (i < layers.length - 1) {
                // Pass this layer's output to next layer's input
                layers[i + 1].setInput(layers[i].getOutput());
            }
        }
    }

    private void backpropogation(double[] trueOutput)
    {
        for (Layer l : layers) l.resetGradients();

        OutputLayer o = (OutputLayer) layers[size - 1]; // start with output layer
        o.calculateLocalGradient(trueOutput);


        double[] upstreamGradient = o.getUpstreamGradient();

        for ( int i = size - 2; i >= 0; i--)  // backprop through all layers till input layer
        {
            HiddenLayer h = (HiddenLayer) layers[i];
            h.calculateLocalGradient(upstreamGradient);
            upstreamGradient = h.getUpstreamGradient();
        }
    }

    private void updateWeightsAndBiases()
    {
        OutputLayer o = (OutputLayer) layers[size - 1]; // start with output layer
     
        o.updateWeights(o.getdL_dW(), LEARNING_RATE);
        o.updateBiases(o.getdL_db(), LEARNING_RATE);


     
        for ( int i = size - 2; i >= 0; i--)  // backprop through all layers till input layer
        {
            HiddenLayer h = (HiddenLayer) layers[i];
     
            h.updateWeights(h.getdL_dW(), LEARNING_RATE);
            h.updateBiases(h.getdL_db(), LEARNING_RATE);

        }

    }

    private void initLayers()
    {
        
        layers = new Layer[ size ];

        
        layers[0] = new HiddenLayer(currInput, hiddenLayers[0]); //

        for ( int i = 1; i < size - 1; i++ )
        {
            layers[i]  = new HiddenLayer(layers[i - 1].getOutput(), hiddenLayers[i]);
        }

        layers[size - 1] = new OutputLayer(layers[size - 2].getOutput(), OutputSize);

        currOutput = layers[size - 1].getOutput();
    }

}