package cristiano.webapp.analysis.prediction.ann;
import java.util.ArrayList;
import java.util.List;

public class Predictor
{
    public List<Double> predict(List<Double> inputFile, int dayToPredict)
    {
        int FEATURE_NUMBER = 5; //number of days in one sample
        int DAYS_TO_PREDICT = dayToPredict;

        //ANN structure
        int CLASSIFICATION_NUMBER = 1;
        int NumberOfHiddenNodes1 = 30;
        double LEARNING_RATE = 0.3;
        double TARGET_ERROR = 0.001;
        long MAX_ITERATION = 1000000;
        double MOMENTUM = 0.1;

//        double startTime;
//        double endTime;

        //dimension of NumberOfNodes[] determines the number of hidden layers
        int [] NumberOfNodes = new int[]{FEATURE_NUMBER, NumberOfHiddenNodes1,CLASSIFICATION_NUMBER};

        LoadTrainingInput loadTrainingInput = new LoadTrainingInput(inputFile, FEATURE_NUMBER);
        LoadTrainingTarget loadTrainingTarget = new LoadTrainingTarget(inputFile, FEATURE_NUMBER, CLASSIFICATION_NUMBER);

        double[][] input = loadTrainingInput.getInput();
        double[][] target = loadTrainingTarget.getTarget();


        BackPropagation bp = new BackPropagation(NumberOfNodes, input, target, LEARNING_RATE, MOMENTUM, TARGET_ERROR, MAX_ITERATION);

//        startTime = System.currentTimeMillis();
        bp.trainNetwork();
//        endTime = System.currentTimeMillis();

        //get the first sample to predict
        double[] test_input = new double[FEATURE_NUMBER];

        double max = 0;
        double min = 0;


            //get Max and Min value
            max = inputFile.get(0);
            min = inputFile.get(0);
            for(double i : inputFile)
            {
                if(i < min)
                    min = i;
                if(i > max)
                    max = i;
            }

            for(int i = 0; i < FEATURE_NUMBER; i++)
            {
                test_input[i] = (inputFile.get(inputFile.size() - FEATURE_NUMBER + i)-min)/(max - min);
                //test_input[i] = (inputFile.get(inputFile.size() - FEATURE_NUMBER + i));
            }


        ArrayList<Double> ret = new ArrayList<Double>();

        for(int i = 0; i<DAYS_TO_PREDICT; i++)
        {
            double result = bp.test(test_input).get(0);
            ret.add(result * (max - min) + min);
//            System.out.println(result * (max - min) + min);


            for(int j = 1; j <= test_input.length - 1; j++)
            {
                test_input[j-1] = test_input[j];
            }
            test_input[test_input.length-1] = result;
        }
//        System.out.println("Running time: " + (endTime - startTime) + " (ms)");
        return ret;
    }


}
