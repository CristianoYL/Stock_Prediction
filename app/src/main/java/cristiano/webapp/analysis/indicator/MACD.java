package cristiano.webapp.analysis.indicator;

import java.util.List;

/**
 * Created by Administrator on 2016/4/28.
 *
 *  * EMA（12）= 前一日EMA（12）×11/13＋今日收盘价×2/13
 * EMA（26）= 前一日EMA（26）×25/27＋今日收盘价×2/27
 * DIFF=今日EMA（12）- 今日EMA（26）
 * DEA（MACD）= 前一日DEA×8/10＋今日DIF×2/10
 * BAR=2×(DIFF－DEA)
 *
 * Example:
 *
 * 20091218日：收盘价55.01
 * 新股上市，DIFF=0, DEA=0, MACD=0，
 *
 * 20091219日: 收盘价53.7
 * EMA（12）=55.01+(53.7-55.01)×2/13=54.8085
 * EMA（26）=55.01+(53.7-55.01)×2/27=54.913
 * DIFF=EMA（12）- EMA（26）= 54.8085 - 54.913 = -0.1045  （-0.104？）
 * DEA=0+(-0.1045)X2/10=-0.0209
 * BAR=2*((-0.1045)-(-0.0209))=-0.1672
 *
 * 其买卖原则为：
 * 1.DIF、DEA均为正，DIF向上突破DEA，买入信号参考。
 * 2.DIF、DEA均为负，DIF向下跌破DEA，卖出信号参考。
 * 3.DIF线与K线发生背离，行情可能出现反转信号。
 * 4.DIF、DEA的值从正数变成负数，或者从负数变成正数并不是交易信号，因为它们落后于市场。
 *
 */
public class MACD {
    public double[] EMA12;
    public double[] EMA26;
    public double[] DIFF;
    public double[] DEA;
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
        EMA12 = new double[numberOfDays];
        EMA26 = new double[numberOfDays];
        DIFF = new double[numberOfDays];
        DEA = new double[numberOfDays];
        EMA12[0] = 0.0;
        EMA26[0] = 0.0;
        DIFF[0] = 0.0;
        DEA[0] = 0.0;



        calEMA12();
        calEMA26();
        calDIFF();
        calDEA();
        if(DIFF[numberOfDays-1] > 0 && DEA[numberOfDays - 1] > 0)
        {
            if(DIFF[numberOfDays - 1] > DEA[numberOfDays - 1])
                return 1;
        }

        if(DIFF[numberOfDays - 1] < 0 && DEA[numberOfDays - 1] < 0 )
        {
            if(DIFF[numberOfDays - 1] < DEA[numberOfDays - 1])
                return -1;
        }

        return 0;   //return 0 持有, return 1 买入, return -1 卖出
    }

    public void calEMA12()
    {
        EMA12[1] = price.get(0) + (price.get(1) - price.get(0)) * 2 / 13;

        //EMA（12）= 前一日EMA（12）×11/13＋今日收盘价×2/13
        for(int i = 2; i<numberOfDays; i++)
        {
            EMA12[i] = EMA12[i-1] * 11/13 + price.get(i) * 2/13;
        }
    }

    public void calEMA26()
    {
        EMA26[1] = price.get(0) + (price.get(1) - price.get(0)) * 2 / 27;

        //EMA（26）= 前一日EMA（26）×25/27＋今日收盘价×2/27
        for(int i = 2; i<numberOfDays; i++)
        {
            EMA26[i] = EMA26[i-1] * 25/27 + price.get(i) * 2/27;
        }
    }

    public void calDIFF()
    {
        for(int i = 0; i<numberOfDays; i++)
        {
            DIFF[i] = EMA12[i] - EMA26[i];
        }
    }

    public void calDEA()
    {
        DEA[1] = 0 + DIFF[1] * 2/10;
        for(int i = 2; i<numberOfDays ; i++)
        {
            DEA[i] = DEA[i-1] * 8/10 + DIFF[i] * 2/10;
        }
    }
}
