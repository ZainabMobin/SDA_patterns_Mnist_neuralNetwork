package neuralNetwork;
import data.AdapterHandler;
import data.ReaderInterface;

public class Main
{

    public static void main( String[] args )
    {
        double LEARNING_RATE = 0.01;

        int[] hidden = { 128, 64 };
        //csv only
        // String testing_path = ".\\data\\mnist\\test\\mnist_test.csv";
        // String training_path = ".\\data\\mnist\\train\\mnist_train.csv";
        
        // //binary compressed gzip only
        // String testing_path = ".\\data\\mnist\\test\\mnist_binary_test";
        // String training_path = ".\\data\\mnist\\train\\mnist_binary_train";
        
        //both csv and binary
        String testing_path = ".\\data\\mnist\\test";
        String training_path = ".\\data\\mnist\\train";


        ReaderInterface adapter_handler = new AdapterHandler();  // Create adapter handler
        NeuralNetwork nn = new NeuralNetwork( hidden, LEARNING_RATE, adapter_handler );  // Inject handler into NN (object type implements the same reference type, set to be an attribute of Neural Network classs)
        
        System.err.println("Training");
        nn.train( training_path );
        
        System.err.println("Testing");
        nn.test( testing_path );
    }

}
