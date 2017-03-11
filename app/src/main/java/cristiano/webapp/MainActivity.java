package cristiano.webapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cristiano.webapp.analysis.indicator.KDJ;
import cristiano.webapp.analysis.indicator.MACD;
import cristiano.webapp.analysis.prediction.ann.Predictor;
import cristiano.webapp.analysis.prediction.svm.SVM;
import cristiano.webapp.chart.ChartHelper;
import cristiano.webapp.database.DatabaseHelper;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int COL_DATE=0;
    private static final int COL_TIME=1;
    private static final int COL_VALUE=2;
    private static final int DATA_COLUMN=3;
    private static final DateFormat fmt =new SimpleDateFormat("HH:mm");

    DatabaseHelper myDB;
    private GraphicalView chart;
    private String[][] data;
    private int kdjAdvice=-2,macdAdvice=-2;
    private String currentStockName;
    Button btn_load;
    List<Double> predictionBase;
    private long interval = 60000;   //the time interval between two stock value (ms)
    //TODO: dynamically set the interval
    /*
     * currently the data is dirty
     * there are replicate data so that the interval cannot be calculated correctly
     * once the database is reset
     * change the interval setting to dynamic
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Branch.getAutoInstance(getApplicationContext());
        myDB = new DatabaseHelper(getApplicationContext());
        showHomePage();
        SharedPreferences sharedPreferences = getSharedPreferences("preference", Context.MODE_PRIVATE);
        if ( sharedPreferences.getBoolean("isFirstLaunch",true) ) {
            insertData();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstLaunch",false);
            editor.commit();
        }





    }
    @Override
    public void onStart() {
        super.onStart();

        Branch branch = Branch.getInstance(getApplicationContext());
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked before showing up
                    Log.i("BranchConfigTest", "deep link data: " + referringParams.toString());
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {  // show home page content
            showHomePage();
        } else if (id == R.id.nav_favourite) {  // show favourite page content
            showStockPage();
        } else if (id == R.id.nav_about_us) {  // show about us page content
            showAboutUs();
        }  else if (id == R.id.nav_contact_us) {  // show contact us page content
            showContactUs();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void showHomePage(){
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null){
            setSupportActionBar(toolbar);
            SearchView sv = (SearchView)findViewById(R.id.sv_search_content);
            sv.setIconified(false);

            if(sv != null){
                sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        //TODO: dynamic setting interval
//                        interval=0;
                        data = getData(query.toUpperCase());
                       if(data != null){
//                           int i=0;
//                           while(interval == 0){
//                               try{
//                                   interval = fmt.parse(data[i+1][COL_TIME]).getTime()-fmt.parse(data[i][COL_TIME]).getTime();
//                                   i++;
//                               } catch (ParseException e){
//                                   e.printStackTrace();
//                               }
//                           }
                           currentStockName = query.toUpperCase();
                           showStockPage();
                           chart = ChartFactory.getTimeChartView(MainActivity.this, ChartHelper.getDateDemoDataset(data,null,false), ChartHelper.getDemoRenderer(currentStockName,false), "HH:mm");
                           LinearLayout chartLayout = (LinearLayout)findViewById(R.id.chartLayout);
                           chartLayout.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,380));
//                           chart.invalidate();
                           return true;
                       } else {
                           Toast.makeText(MainActivity.this,"No Result Found!\nPlease Check Your Input.",Toast.LENGTH_SHORT).show();
                           return false;
                       }
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Snackbar.make(view, "Please Enter the Stock Code", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                    showHomePage();
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null){
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }
    private void showStockPage(){
        setContentView(R.layout.activity_stock);
        final Button btn_ann = (Button)findViewById(R.id.btn_ann);
        btn_ann.setText(R.string.ann);
        final TextView tv_1 = (TextView)findViewById(R.id.tv_1);
        final TextView tv_2 = (TextView)findViewById(R.id.tv_2);
        final EditText et_1 = (EditText)findViewById(R.id.et_1);
        final EditText et_2 = (EditText)findViewById(R.id.et_2);
        final Button btn_advice = (Button)findViewById(R.id.btn_advice);
        final Button btn_svm = (Button)findViewById(R.id.btn_svm);





        btn_advice.setEnabled(false);
        btn_advice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                tv_1.setText(R.string.kdj);
                tv_2.setText(R.string.macd);
                switch(getKdjAdvice()){

                    case 1: //buy
                        et_1.setText(R.string.buy);
                        break;
                    case 0:
                        et_1.setText(R.string.keep);
                        break;
                    case -1:
                        et_1.setText(R.string.sell);
                        break;
                    default:
                        et_1.setText(R.string.error);
                        break;
                }
                switch(getMacdAdvice()){

                    case 1: //buy
                        et_2.setText(R.string.buy);
                        break;
                    case 0:
                        et_2.setText(R.string.keep);
                        break;
                    case -1:
                        et_2.setText(R.string.sell);
                        break;
                    default:
                        et_2.setText(R.string.error);
                        break;
                }
                btn_advice.setEnabled(false);
            }
        });
        tv_1.setText(R.string.baseCount);
        tv_2.setText(R.string.predictCount);
        btn_ann.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
               if(btn_ann.getText().toString().equals("Predict Again")){
                   tv_1.setText(R.string.baseCount);
                   tv_2.setText(R.string.predictCount);
                   et_1.setText(null);
                   et_2.setText(null);
                   btn_ann.setText(R.string.ann);
                   btn_svm.setText(R.string.svm);
               } else {

                   btn_ann.setEnabled(false);
                   LinearLayout chartLayout = (LinearLayout)findViewById(R.id.chartLayout);

                   int baseDataCount=20;
                   int predictionCount=5;
                   try{
                       baseDataCount = Integer.parseInt(et_1.getText().toString());
                       predictionCount = Integer.parseInt(et_2.getText().toString());
                       if(et_1.getText() == null || baseDataCount <= 12 || baseDataCount>data.length){
                           Toast.makeText(MainActivity.this,"The prediction base count must be greater than 12 and smaller than the current dataset!",Toast.LENGTH_LONG).show();
                           et_1.setText(null);
                           btn_ann.setEnabled(true);
                           return;
                       }
                       if(et_2.getText() == null || predictionCount <= 0 || predictionCount > 10){
                           Toast.makeText(MainActivity.this,"The prediction day count must be a positive number not greater than 10\n(Stats shows that comprehensive accuracy are provided within 5 days!",Toast.LENGTH_LONG).show();
                           et_2.setText(null);
                           btn_ann.setEnabled(true);
                           return;
                       }

                   }catch (NumberFormatException e){
                       Toast.makeText(MainActivity.this,"Please Enter Numbers.",Toast.LENGTH_SHORT).show();
                       btn_ann.setEnabled(true);
                       return;
                   }

                   if(chart != null){
                       chartLayout.removeView(chart);
                   }

                   String [][] shortData = new String[baseDataCount][DATA_COLUMN];
                   for(int i=0;i<baseDataCount;i++){
                       shortData[i][COL_TIME] = data[data.length-baseDataCount+i][COL_TIME];
                       shortData[i][COL_VALUE] = data[data.length-baseDataCount+i][COL_VALUE];
                   }

                   chart = ChartFactory.getTimeChartView(MainActivity.this, ChartHelper.getDateDemoDataset(shortData, getAnnPrediction(shortData,baseDataCount,predictionCount),true), ChartHelper.getDemoRenderer("ANN Prediction For "+currentStockName,true), "HH:mm");
                   chartLayout.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,380));
//                   chart.invalidate();
                   btn_ann.setText(R.string.again);
                   btn_svm.setText(R.string.again);
                   btn_advice.setEnabled(true);
               }
                btn_ann.setEnabled(true);


            }
        });



        //set svm button listener
        btn_svm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(btn_svm.getText().toString().equals("Predict Again")){
                    tv_1.setText(R.string.baseCount);
                    tv_2.setText(R.string.predictCount);
                    et_1.setText(null);
                    et_2.setText(null);
                    btn_ann.setText(R.string.ann);
                    btn_svm.setText(R.string.svm);
                } else {
                    btn_svm.setEnabled(false);
                    LinearLayout chartLayout = (LinearLayout)findViewById(R.id.chartLayout);

                    int baseDataCount=20;
                    try{
                        baseDataCount = Integer.parseInt(et_1.getText().toString());

                        if(et_1.getText() == null || baseDataCount <= 12 || baseDataCount>data.length){
                            Toast.makeText(MainActivity.this,"The prediction base count must be greater than 12 and smaller than the current dataset!",Toast.LENGTH_LONG).show();
                            et_1.setText(null);
                            btn_svm.setEnabled(true);
                            return;
                        }


                    }catch (NumberFormatException e){
                        Toast.makeText(MainActivity.this,"Please Enter Numbers.",Toast.LENGTH_SHORT).show();
                        btn_svm.setEnabled(true);
                        return;
                    }

                    if(chart != null){
                        chartLayout.removeView(chart);
                    }

                    String [][] shortData = new String[baseDataCount][DATA_COLUMN];
                    for(int i=0;i<baseDataCount;i++){
                        shortData[i][COL_TIME] = data[data.length-baseDataCount+i][COL_TIME];
                        shortData[i][COL_VALUE] = data[data.length-baseDataCount+i][COL_VALUE];
                    }
                    chart = ChartFactory.getTimeChartView(MainActivity.this, ChartHelper.getDateDemoDataset(shortData, getSvmPrediction(shortData,baseDataCount),true), ChartHelper.getDemoRenderer("ANN Prediction For "+currentStockName,true), "HH:mm");
                    chartLayout.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,380));

                    btn_ann.setText(R.string.again);
                    btn_svm.setText(R.string.again);
                    btn_advice.setEnabled(true);
                }
                btn_svm.setEnabled(true);


            }
        });






        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null){
            setSupportActionBar(toolbar);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Snackbar.make(view, "Please Enter the Stock Code", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                    showHomePage();
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null){
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    private void showAboutUs(){
        setContentView(R.layout.activity_about_us);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null){
            setSupportActionBar(toolbar);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                    Snackbar.make(view, "Please Enter the Stock Code", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                    showHomePage();
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null){
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }
    private void showContactUs(){
        setContentView(R.layout.activity_contact_us);
        btn_load = (Button)findViewById(R.id.btn_load);
        btn_load.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                insertData();
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null){
            setSupportActionBar(toolbar);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                    Snackbar.make(view, "Please Enter the Stock Code", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                    showHomePage();
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null){
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }
    public void insertData(){
        String[] entrie = new String[9];
        InputStream is = getResources().openRawResource(R.raw.data);
        try {
            InputStreamReader isr = new InputStreamReader(is,"gbk");
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            StringBuffer buf = new StringBuffer();
            int fileLines=0;
            while((line=br.readLine()) != null){
                ;    //convert all ( ) and space to comma, and then split with comma, so that the result only contains data
                String[] res = line.replace('(',',').replace(')',',').replace(' ',',').replace('\'',',').split(",");
                for(int i=0,j=0;i<res.length;i++){
                    if(res[i].length()!=0) {
                        entrie[j]=res[i];
                        j++;
                    }
                }
                if(myDB.insertData(myDB.STOCK_TABLE,entrie)) {
//                    Toast.makeText(MainActivity.this, "Data Inserted!",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Data Not Inserted!",Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(MainActivity.this, "Data Inserted!",Toast.LENGTH_SHORT).show();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String[][] getData(String stockName){

        Cursor cursor = myDB.getStockData(stockName);
        if(cursor.getCount() == 0){
            return null;
        } else {
            String[][] result = new String[cursor.getCount()][DATA_COLUMN];
            int count=0;

            while (cursor.moveToNext()) {
                result[count][COL_DATE] = cursor.getString(0); //date
                result[count][COL_TIME] = cursor.getString(1); //time
                result[count][COL_VALUE] = cursor.getString(2); //price
                count++;
            }
//            Toast.makeText(MainActivity.this, "Data Retrieved", Toast.LENGTH_SHORT).show();
            return result;
        }
    }
    private String[][] getAnnPrediction(String[][] shortData, int baseDataCount, int predictionCount){

        String[][] predictData = new String[predictionCount][DATA_COLUMN];
        predictionBase = new ArrayList<Double>();
        List<Double> predictionResult = new ArrayList<Double>();
        try {

            for(int i=0;i<baseDataCount;i++){
                predictionBase.add(Double.parseDouble(shortData[i][COL_VALUE]));
            }
            Predictor predictor = new Predictor();
            predictionResult = predictor.predict(predictionBase,predictionCount);

            for(int i=0;i<predictionCount;i++){
                predictData[i][COL_TIME] = fmt.format(new java.util.Date (fmt.parse(shortData[shortData.length-1][COL_TIME]).getTime()+interval*(i+1)));
                predictData[i][COL_VALUE] = predictionResult.get(i).toString();
            }

            return predictData;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
    private String[][] getSvmPrediction(String[][] shortData,int baseCount){

        predictionBase = new ArrayList<Double>();
        List<Double> predictionResult = new ArrayList<Double>();
        try {

            for (int i = 0; i < baseCount; i++) {
                predictionBase.add(Double.parseDouble(shortData[i][COL_VALUE]));
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
            return null;
        }

        SVM svm = new SVM();
        List<Double> svmData = new ArrayList<Double>();
        for(int i=0;i<shortData.length;i++){
            svmData.add(Double.parseDouble(shortData[i][COL_VALUE]));
        }
        String[][] svmResult = new String[1][DATA_COLUMN];
        try{
            svmResult[0][COL_TIME] = fmt.format(new java.util.Date (fmt.parse(shortData[shortData.length-1][COL_TIME]).getTime()+interval));
            svmResult[0][COL_VALUE] = ""+svm.run(svmData);
        }catch (ParseException e){
            e.printStackTrace();
            return null;
        }
        return svmResult;
    }
    private int getKdjAdvice(){
        KDJ kdj = new KDJ();
        kdjAdvice = kdj.strategy(predictionBase);
        return kdjAdvice;
    }
    private int getMacdAdvice(){
        MACD macd = new MACD();
        macdAdvice = macd.strategy(predictionBase);
        return macdAdvice;
    }
}
