package cristiano.webapp.analysis.indicator;

import java.util.List;

/**
 * Created by Administrator on 2016/4/28.
 *
 *
 * K, D 值范围为 0~100
 * J值可能小于0 或 大于100
 * K, D, J 均在20以下:买入
 * K, D, J 均在80以上:卖出
 * K, D, J 在中间:观望
 *
 *
 *
 *
 *
 */
public class KDJ
{
    public int CYCLE = 5;   // CYCLE is a parameter for KDJ calculation

    public double[] K;
    public double[] D;
    public double[] J;
    public double[] RSV;
    public List<Double> price;
    public int numberOfDays;

    /*
    *   return:
    *   1: buy
    *   0: hold
    *   -1: sell
     */
    public int strategy(List<Double> price)
    {
        this.price = price;
        numberOfDays = price.size();
        K = new double[numberOfDays];
        D = new double[numberOfDays];
        J = new double[numberOfDays];
        RSV = new double[numberOfDays];

        // The first 8 days have default K and D value of 50
        for(int i = 0; i<CYCLE - 1; i++)
        {
            K[i] = 50;
            D[i] = 50;
        }

        // calculate RSV value from the 9th day
        for(int i = CYCLE - 1; i < numberOfDays; i++)
        {
            double max = getMax(i - CYCLE + 1, i);
            double min = getMin(i - CYCLE + 1, i);
            RSV[i] = (price.get(i) - min) / (max - min) * 100;
        }

        // calculate K and D value from the 9th day
        for(int i = CYCLE - 1; i < numberOfDays; i++)
        {
            K[i] = (K[i-1] * 2/3) + (RSV[i]*1/3);
            D[i] = (D[i-1] * 2/3) + (K[i]*1/3);
        }

        // calculate J value
        for(int i = 0; i<numberOfDays; i++)
        {
            J[i] = 3 * K[i] - 2 * D[i];
        }


        // get strategy
        if(K[numberOfDays - 1] < 20 && D[numberOfDays - 1] < 20 && J[numberOfDays - 1] < 20)
        {
            return 1;   // buy in
        }
        else if(K[numberOfDays - 1] > 80 && D[numberOfDays - 1] > 80 && J[numberOfDays - 1] > 80)
        {
            return -1; // sale out
        }
        else
            return 0; // hold

    }

    public double getMax(int low, int high)
    {
        double max = price.get(low);
        for(int i = low + 1; i<=high; i++)
        {
            if(price.get(i) > max)
            {
                max = price.get(i);
            }
        }
        return max;
    }

    public double getMin(int low, int high)
    {
        double min = price.get(low);
        for(int i = low + 1; i<=high; i++)
        {
            if(price.get(i) < min)
            {
                min = price.get(i);
            }
        }
        return min;
    }
}
