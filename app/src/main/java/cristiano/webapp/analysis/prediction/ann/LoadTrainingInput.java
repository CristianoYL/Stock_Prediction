package cristiano.webapp.analysis.prediction.ann;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class LoadTrainingInput
{
    private BufferedReader bufferedReader;

    private double[][] input;

    public int sampleNumber;

//    public ArrayList<Double> linear = new ArrayList<Double>();

    public LoadTrainingInput(List<Double> linear, int inputNumber)
    {
        try
        {
//            bufferedReader = new BufferedReader(new FileReader(fileName));
//            String line;
//           for(double i : linear)
//            {
//                linear.add(i);
//            }
//            bufferedReader.close();

            double max = linear.get(0);
            double min = linear.get(0);

            for(double i : linear)
            {
                if(i < min)
                    min = i;
                if(i > max)
                    max = i;
            }
            sampleNumber = linear.size() - inputNumber; // inputNumber determines the shift window size, and window size determines the number of samples
            //sampleNumber = linear.size() - inputNumber;

            input = new double[sampleNumber][inputNumber];

            int counter = 0;
            for(int i = 0; i < sampleNumber; i ++)
            {
                double[] temp = new double[inputNumber];
                for(int j = 0; j < inputNumber; j++)
                {
                    temp[j] = (linear.get(i+j)-min)/(max-min);  //Normalized within [0,1]
                    //temp[j] = linear.get(i+j);
                }
                input[counter++] = temp;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public double[][] getInput()
    {
        return input;
    }


    // this main function is only for testing
    public static void main(String[] args)
    {
        ArrayList<Double> test = new ArrayList<Double>();
        for(int i=0;i<10;i++){
            test.add((double)i);
        }
        LoadTrainingInput loadTrainingInput = new LoadTrainingInput(test, 5);
        double[][] out = loadTrainingInput.getInput();
        for(int i = 0; i<out.length;i++)
        {
            for (int j = 0; j < out[i].length; j++)
            {
                System.out.print(out[i][j] + " ");
            }
            System.out.println();
        }
    }


}
