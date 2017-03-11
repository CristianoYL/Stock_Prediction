package cristiano.webapp.analysis.prediction.ann;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;
import java.io.*;

public class Layer {
    private double nodeInput; //Input of a neural node

    public double Inputs[]; //Vector of input signal from previous layer to the current layer

    public Node nodes[]; //Vector of nodes in current layer

    public Layer (int NumberOfNodes, int NumberOfInputs) {
        nodes = new Node[NumberOfNodes];
        for(int i = 0; i < NumberOfNodes; i++){
            nodes[i] = new Node(NumberOfInputs);
        }
        Inputs = new double[NumberOfInputs];
    }

    public void activationForward(){
        for(int i = 0; i < nodes.length; i++){
            nodeInput = nodes[i].Bias;
            for(int j = 0; j < nodes[i].Weights.length; j++){
                nodeInput = nodeInput + Inputs[j]*nodes[i].Weights[j];  //the number of output equals to the number of neurons in  previous layer
            }
            nodes[i].Output = Sigmoid(nodeInput);
        }
    }

    //Sigmoid function of neural node
    private double Sigmoid (double nodeInput){
        return 1/(1 + Math.exp(-nodeInput));
    }

    //Return the output from all nodes in this layer
    public double[] layerOutput(){  //put all the output in a single vector
        double vector[];
        vector = new double[nodes.length];
        for(int i = 0; i < nodes.length; i++){
            vector[i] = nodes[i].Output;
        }
        return vector;
    }

    public Node[] get_nodes() { return nodes;}
};
