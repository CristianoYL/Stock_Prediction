package cristiano.webapp.analysis.prediction.ann;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*;
import java.io.*;


public class BackPropagation{
    //Error function variable that is calculated using the calculateOverallError() function
    private double OverallError;

    //The minimum error function defined by the user
    private double MinimumError;

    //The user-defined expected output pattern for a set of samples
    private double ExpectedOutputs[][];

    //The user-defined input pattern for a set of samples
    private double Inputs[][];

    //User defined learning rate(used for weight updates)
    private double LearningRate;

    //User defined momentum(used for weight updates)
    private double Momentum;

    //Number of layers in the network(include input, output and hidden layers)
    private int NumberOfLayers;

    //Number of training sets
    private int NumberOfSamples;

    //Current training set/sample that is used to train network
    private int CurrentSampleNumber;

    //User defined maximum number of Epochs
    private long MaxNumberOfEpochs;

    public Layer layers[];  //layers[0]---input layer; layers[1]~layers[n-2]---hidden layers; layers[n-1]---output layer
    public double ActualOutputs[][];

    public BackPropagation(int NumberOfNodes[], double InputSamples[][], double OutputSamples[][], double LearnRate, double Moment, double MinError, long MaxIter){
        NumberOfSamples = InputSamples.length;
        MinimumError = MinError; //user defined minimum error
        LearningRate = LearnRate; //user defined learnng rate
        Momentum = Moment; //If Momentum = 0, it is the standard back propagation algorithm
        NumberOfLayers = NumberOfNodes.length;
        MaxNumberOfEpochs = MaxIter; //maximum number of epoches

        //create network layers
        layers = new Layer[NumberOfLayers];

        //Assign the number of node to the input layer
        layers[0] = new Layer(NumberOfNodes[0], NumberOfNodes[0]);

        //Assign number of nodes to each layer
        for(int i = 1; i < NumberOfLayers; i++){
            layers[i] = new Layer(NumberOfNodes[i], NumberOfNodes[i - 1]);
        }

        Inputs = new double[NumberOfSamples][layers[0].nodes.length];
        ExpectedOutputs = new double[NumberOfSamples][layers[NumberOfLayers - 1].nodes.length];
        ActualOutputs = new double[NumberOfSamples][layers[NumberOfLayers - 1].nodes.length];

        //Assign input set
        for(int i = 0; i < NumberOfSamples; i++){
            for(int j = 0; j < layers[0].nodes.length;j++){
                Inputs[i][j] = InputSamples[i][j];
            }
        }

        //Assign output set
        for(int i = 0; i < NumberOfSamples; i++){
            for(int j = 0; j < layers[NumberOfLayers - 1].nodes.length; j++){
                ExpectedOutputs[i][j] = OutputSamples[i][j];
            }
        }
    }

    //Calculate the node activations
    public void activationForward(){
        //Assign the input vector from the input layer to all the node in the first hidden layer
        for(int i = 0; i < layers[0].nodes.length; i++){
            layers[0].nodes[i].Output = layers[0].Inputs[i];
        }
        layers[1].Inputs = layers[0].Inputs;
        for(int i = 1; i < NumberOfLayers; i++){
            layers[i].activationForward();

            if(i != NumberOfLayers - 1){
                layers[i+1].Inputs = layers[i].layerOutput();
            }
        }
    }

    //Back propagated the network output error through the network to update the weight values
    public void updateWeights(){
        calculateDelta();
        backPropagateError();
    }

    private void calculateDelta(){
        double Sum;
        int OutputLayer = NumberOfLayers - 1;

        //Calculate all output delta(i.e delta = (target - output)*output*(1 - output))
        for(int i = 0; i < layers[OutputLayer].nodes.length; i++){
            layers[OutputLayer].nodes[i].Delta = (ExpectedOutputs[CurrentSampleNumber][i] - layers[OutputLayer].nodes[i].Output)*layers[OutputLayer].nodes[i].Output*(1 - layers[OutputLayer].nodes[i].Output);
        }
        //Calculate delta for all nodes in the hidden layers(back propagate the errors)
        for(int i = NumberOfLayers - 2; i > 0; i--){ //ith layer
            for(int j = 0; j < layers[i].nodes.length; j++){ //jth node in the ith layer
                Sum = 0;
                for(int k = 0; k < layers[i + 1].nodes.length; k++){
                    Sum = Sum + layers[i + 1].nodes[k].Weights[j]*layers[i + 1].nodes[k].Delta;
                }
                layers[i].nodes[j].Delta = layers[i].nodes[j].Output*(1 - layers[i].nodes[j].Output)*Sum;
            }
        }
    }

    private void backPropagateError(){
        //updates weights
        for(int i = NumberOfLayers - 1; i > 0; i--){
            for(int j = 0; j < layers[i].nodes.length; j++){
                //Calculate bias weight difference to node j (Momentum = 0 is the standard back propagation)
                layers[i].nodes[j].BiasDiff = LearningRate*layers[i].nodes[j].Delta + Momentum*layers[i].nodes[j].BiasDiff;

                //Update bias weight to node j
                layers[i].nodes[j].Bias = layers[i].nodes[j].Bias + layers[i].nodes[j].BiasDiff;

                //Update weights
                for(int k = 0; k < layers[i].Inputs.length; k++){
                    //Calculate weight difference between node j and k (Momentum = 0 is the standard back propagation)
                    layers[i].nodes[j].WeightsDiff[k] = LearningRate*layers[i].nodes[j].Delta*layers[i - 1].nodes[k].Output + Momentum*layers[i].nodes[j].WeightsDiff[k];

                    //Update weight between node j and k
                    layers[i].nodes[j].Weights[k] = layers[i].nodes[j].Weights[k] + layers[i].nodes[j].WeightsDiff[k];
                }
            }
        }
    }

    //sum of least square (network training target is to minimize overall error)
    public void calculateOverallError(){
        OverallError = 0;
        for(int i = 0; i < NumberOfSamples; i++){
            for(int j = 0; j < layers[NumberOfLayers - 1].nodes.length; j++){
                OverallError = OverallError + 0.5*(Math.pow(ExpectedOutputs[i][j] - ActualOutputs[i][j], 2));
            }
        }
    }

    public void trainNetwork(){
        long k = 0;
        do{
            for(CurrentSampleNumber = 0; CurrentSampleNumber < NumberOfSamples; CurrentSampleNumber++){
                for(int i = 0; i < layers[0].nodes.length; i++){
                    layers[0].Inputs[i] = Inputs[CurrentSampleNumber][i];
                }
                activationForward();
                //Assign calculated output vector from network to ActualOutput
                for(int i = 0; i < layers[NumberOfLayers - 1].nodes.length; i++){
                    ActualOutputs[CurrentSampleNumber][i] = layers[NumberOfLayers - 1].nodes[i].Output;
                }
                updateWeights();
            }
            k++;
            calculateOverallError();
            OverallError = OverallError/NumberOfSamples;
            System.out.println("Iteration# = " + k + " Error = " + OverallError);
        }while (((OverallError) > MinimumError) && (k <  MaxNumberOfEpochs));
    }

    public Layer[] get_layers()
    {
        return layers;
    };

    public ArrayList<Double> test(double[] input){
        int winner = 0;
        Node[] output_nodes;
        for(int j = 0; j < layers[0].nodes.length; j++){
            layers[0].Inputs[j] = input[j];
        }
        activationForward();
        output_nodes = (layers[layers.length - 1]).get_nodes();
        ArrayList<Double> predictResult = new ArrayList<Double>();
        for(Node i :output_nodes)
        {
            predictResult.add(i.Output);
        }
        return predictResult;
    }
}
