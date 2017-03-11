package cristiano.webapp.analysis.prediction.ann;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class LoadTrainingTarget
{
    private BufferedReader bufferedReader;

    public double[][] output;

    public int sampleNumber;

    public ArrayList<Double> linear;

    public LoadTrainingTarget(List<Double> linear, int FEATURE_NUMBER, int outputNum)
    {
//        linear = new ArrayList<Double>();
        try
        {
//            bufferedReader = new BufferedReader(new FileReader(fileName));
//            String line;
//            for(Double i : linear)
//            {
//                linear.add(i);
//            }
//            bufferedReader.close();

            //get Max and Min value
            double max = linear.get(0);
            double min = linear.get(0);
            for(double i :linear)
            {
                if(i < min)
                    min = i;
                if(i > max)
                    max = i;
            }

            sampleNumber = linear.size() - FEATURE_NUMBER;

            output = new double[sampleNumber][outputNum];

            int counter = 0;
            for(int i = FEATURE_NUMBER; i < linear.size(); i++)
            {
                double[] temp = new double[outputNum];
                for(int j = 0; j < outputNum; j++)
                {
                    //temp[j] = linear.get(i+j);
                    //temp[j] = 1 / (1 + Math.exp(-linear.get(i+j)));
                    temp[j] = (linear.get(i+j)-min)/(max-min);
                }
                output[counter++] = temp;
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    public double[][] getTarget()
    {
        return output;
    }

    public static void main(String[] agrs)
    {
        ArrayList<Double> test = new ArrayList<Double>();
        for(int i=0;i<10;i++){
            test.add((double)i);
        }
        LoadTrainingTarget loadTrainingTarget = new LoadTrainingTarget(test, 5, 1);
        double[][] out = loadTrainingTarget.getTarget();
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
