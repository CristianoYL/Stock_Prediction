package cristiano.webapp.analysis.prediction.ann;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class Node {
    public double Output;//Activation output of a neural node

    public double Weights[]; //Weight vector from previous nodes to the current node

    public double Bias; //Node bias

    public double WeightsDiff[]; //Weight difference between the nth and the (n-1)th iteration

    public double BiasDiff; //Bias difference between the nth and the (n-1)th iteration

    public double Delta; //Output error(need to be backpropagated)

    public Node (int NumberOfNodes)
    {
        Weights = new double[NumberOfNodes];
        WeightsDiff = new double[NumberOfNodes];
        initNodes();    // set all the weights random
    }

    //Initialize weights and bias of the node with random value between -1 to 1
    private void initNodes()
    {
        Bias = -1 + 2*Math.random();    //bias within -1 ~ 1
        BiasDiff = 0;

        for(int i = 0; i < Weights.length; i++)
        {
            Weights[i] = -1 + 2*Math.random();
            WeightsDiff[i] = 0;
        }
    }

    public double[] get_weights(){ return Weights;}
    public double get_output(){ return Output;}
};
