package cristiano.webapp.analysis.prediction.svm;

import java.util.ArrayList;  
import java.util.List;    
import libsvm.svm;
import libsvm.svm_model;  
import libsvm.svm_node;  
import libsvm.svm_parameter;  
import libsvm.svm_problem;  

public class SVM {

	public int daysForPredict;
	public double run(List<Double> price)
	{		
		//define two training data sets a and b, and create the labels of them  
		List<Double> trainLabel = new ArrayList<Double>();  
		List<svm_node[]> trainNodeSet = new ArrayList<svm_node[]>();
		daysForPredict = price.size()-9;
		getData(trainNodeSet, trainLabel, price);
		int dataRange=trainNodeSet.get(0).length; 
		//the vector table of the training data set
		svm_node[][] trainDatas = new svm_node[trainNodeSet.size()][dataRange]; 	
		for (int i = 0; i < trainDatas.length; i++) 
		{  
			for (int j = 0; j < dataRange; j++) 
			{  
				trainDatas[i][j] = trainNodeSet.get(i)[j];  
			}  
		}  
		//the labels of a and b  
		double[] trainLabels = new double[trainLabel.size()]; 
		for (int i = 0; i < trainLabels.length; i++) 
		{  
			trainLabels[i] = trainLabel.get(i);  
		}   		 
		//define the svm_problem object
		svm_problem problem = new svm_problem();  
		problem.l = trainNodeSet.size(); //the number of vectors 
		problem.x = trainDatas; //the vector table of training data set   
		problem.y = trainLabels; //the relevant label array 
		//define the svm_parameter object  
		svm_parameter param = new svm_parameter();  
		param.svm_type = svm_parameter.EPSILON_SVR;  
		param.kernel_type = svm_parameter.LINEAR;  
		param.cache_size = 100;  
		param.eps = 0.00001;  
		param.C = 1.9;  
		//train the svm classification model by using svm.svm_train()  
		svm_model model = svm.svm_train(problem, param);  
		//get the test data set
		List<Double> testLabel = new ArrayList<Double>();  
		List<svm_node[]> testNodeSet = new ArrayList<svm_node[]>();  


		//getData(testNodeSet, testLabel, price.subList(price.size()-daysForPredict,price.size()-1));
		svm_node[] vector = new svm_node[daysForPredict];
		for (int i = price.size()-daysForPredict; i <= price.size()-1; i++)
		{
			svm_node node = new svm_node();
			node.index = i + 1;
			node.value = price.get(i);
			vector[i-(price.size()-daysForPredict)] = node;	//vector[0] ~ vector[3]
		}
		testNodeSet.add(vector);



		//the vector table of training data set
		svm_node[][] testDatas = new svm_node[testNodeSet.size()][dataRange]; 
		for (int i = 0; i < testDatas.length; i++) 
		{  
			for (int j = 0; j < dataRange; j++) 
			{  
				testDatas[i][j] = testNodeSet.get(i)[j];  
			}  
		} 
		//relevant labels of a and b
//		double[] testLabels = new double[testLabel.size()];
//		for (int i = 0; i < testLabels.length; i++)
//		{
//			testLabels[i] = testLabel.get(i);
//		}
		System.out.println();
		//predict the labels of test data set 
		double err = 0.0;
		double predictValue = 0.0;
		for (int i = 0; i < testDatas.length; i++) 
		{  
			//double trueValue = testLabels[i];
			//System.out.print("True value: " + trueValue + "  ");
			predictValue = svm.svm_predict(model, testDatas[i]);
			System.out.print("Predict value: " + predictValue + "");  
			//err += Math.abs(predictValue-trueValue);
			//System.out.println();
		} 
//		err = err/testDatas.length;
//		System.out.println("Error: " + err);
		return predictValue;
	}

	//define function getData
	public void getData(List<svm_node[]> nodeSet, List<Double> label, List<Double> linear)
	{  

			    svm_node[] vector = new svm_node[daysForPredict];
		int i = 0, j = 0;
		for(j = 0; j <= linear.size()-1-daysForPredict; j++)
		{
			for (i = 0; i < daysForPredict; i++)
			{
				svm_node node = new svm_node();
				node.index = j + i + 1;
				node.value = linear.get(j + i);
				vector[i] = node;	//vector[0] ~ vector[3]
			}
			nodeSet.add(vector);

		}
		for(int k = daysForPredict; k < linear.size(); k++)
		{
			double lablevalue = linear.get(k);
			label.add(lablevalue);
		}

	}
}
