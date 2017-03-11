package cristiano.webapp.chart;

import android.graphics.Color;
import android.os.Handler;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/4/8.
 */
public class ChartHelper {

    public static XYMultipleSeriesDataset getDateDemoDataset(String[][] data,String[][] predictionData,boolean showPrediction) {//初始化的数据
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries stockSeries = new TimeSeries("Stock Series");
        DateFormat fmt =new SimpleDateFormat("HH:mm");

        try {
            for(int i=0;i<data.length;i++){
                stockSeries.add(fmt.parse(data[i][1]), Double.parseDouble(data[i][2]));
            }
            dataset.addSeries(stockSeries);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(showPrediction){
            try {
                TimeSeries predictionSeries = new TimeSeries("Prediction Series");

                //add last point of history data to prediction dataset so the chart connects
                predictionSeries.add(fmt.parse(data[data.length-1][1]), Double.parseDouble(data[data.length-1][2]));
                for(int i=0;i<predictionData.length;i++){
                    predictionSeries.add(fmt.parse(predictionData[i][1]), Double.parseDouble(predictionData[i][2]));
                }
                dataset.addSeries(predictionSeries);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dataset;
    }
    public static XYMultipleSeriesRenderer getDemoRenderer(String stockName,boolean showPrediction) {

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
            renderer.setChartTitle(stockName+" Price");//标题
            renderer.setChartTitleTextSize(20);
            renderer.setXTitle("time");    //x轴说明
            renderer.setYTitle("price");
            renderer.setAxisTitleTextSize(16);
            renderer.setAxesColor(Color.BLACK);
            renderer.setLabelsTextSize(10);    //数轴刻度字体大小
            renderer.setLabelsColor(Color.BLACK);
            renderer.setLegendTextSize(15);    //曲线说明
            renderer.setXLabelsColor(Color.BLACK);
            renderer.setYLabelsColor(0,Color.BLACK);
            renderer.setShowLegend(true);
            renderer.setFitLegend(true);
            renderer.setMargins(new int[] {20, 30, 15, 2});//上左下右{ 20, 30, 100, 0 })

            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(Color.RED);
            r.setChartValuesTextSize(10);
            r.setChartValuesSpacing(0);
            r.setPointStyle(PointStyle.DIAMOND);
            r.setFillBelowLine(true);
            r.setFillBelowLineColor(Color.WHITE);
            r.setFillPoints(true);
            r.setDisplayChartValues(true);
            renderer.addSeriesRenderer(r);
            renderer.setMarginsColor(Color.WHITE);
            renderer.setPanEnabled(true,true);
            renderer.setShowGrid(true);
//        renderer.setYAxisMax(Double.parseDouble(maxValue));//纵坐标最大值
//        renderer.setYAxisMin(Double.parseDouble(minValue));//纵坐标最小值
            renderer.setInScroll(true);

        //add two renderer, red for history, blue for prediction
        if(showPrediction){
            XYSeriesRenderer preR = new XYSeriesRenderer();
            preR.setChartValuesTextSize(10);
            preR.setChartValuesSpacing(0);
            preR.setPointStyle(PointStyle.DIAMOND);
            preR.setFillBelowLine(true);
            preR.setFillBelowLineColor(Color.WHITE);
            preR.setFillPoints(true);
            preR.setDisplayChartValues(true);
            preR.setColor(Color.BLUE);
            renderer.addSeriesRenderer(preR);
            renderer.setChartTitle(stockName);
        }

        return renderer;
    }

}
