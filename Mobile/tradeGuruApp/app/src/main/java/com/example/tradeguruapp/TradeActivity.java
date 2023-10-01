package com.example.tradeguruapp;

/*import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TradeActivity extends AppCompatActivity implements OnTaskComplete {

    TextView companyTextView;
    TextView countdownTextView;
    TextView moneyTextView;
    TextView quoteTextView;
    Button buyBtn;
    Button shortBtn;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");
    LocalDateTime lastRefreshed;
    String date;
    String time;
    String timeOfTrade;
    LocalDateTime dateOfTrade;
    Float buyPrice;
    Float shortPrice;
    Float newPrice;
    Float priceDifference;
    private double userMoney = 1000.0;
    ArrayList<Float> prices = new ArrayList<>();
    ArrayList<Float> times = new ArrayList<>();
    ArrayList<Entry> dataVals = new ArrayList<Entry>();
    LineChart stockLineChart;
    DecimalFormat df = new DecimalFormat("0.00");
    DecimalFormat dfStock = new DecimalFormat("0.000");
    Integer adder;
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String checker;
    Integer mentor;
    String mentorName;
    ImageView mImageView;
    String[] mentorNames = {"Bjrön Wahlroos", "Elon Musk", "Jordan Belfort", "WallStreetBets", "Warren Buffett"};
    
    private TradeDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private CountdownHandler countdownHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        companyTextView = (TextView) findViewById(R.id.companyTextView);
        countdownTextView = (TextView) findViewById(R.id.countdownTextView);
        moneyTextView = (TextView) findViewById(R.id.moneyTextView);
        quoteTextView = (TextView) findViewById(R.id.quoteTextView);
        stockLineChart = (LineChart) findViewById(R.id.stockLineChart);
        mImageView = (ImageView) findViewById(R.id.mImageView);
        buyBtn = (Button) findViewById(R.id.buyBtn);
        shortBtn = (Button) findViewById(R.id.shortBtn);
        countdownHandler = new CountdownHandler(this);
        shortBtn.setEnabled(false);
        buyBtn.setEnabled(false);
        new UpdateTask(this, this).execute();
        MentorQuotes mentorQuotes = new MentorQuotes();

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userMoney = preferences.getFloat("money", 1000.0f);
        mentor = preferences.getInt("mentor", -1);

        updateMoneyTextView();

        dbHelper = new TradeDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();
        mentorName = mentorNames[mentor];
        int mentorImageResource = getMentorImageResource(mentor);
        mImageView.setImageResource(mentorImageResource);

        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adder < 0) {
                    companyTextView.setText("Stock market has closed for today.");
                    buyBtn.setEnabled(false);
                    shortBtn.setEnabled(false);
                    checker = yesterday.toString();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("dateChecker", (String) checker);
                    editor.apply();
                } else {
                    countdownHandler.cancelCountdown();
                    buyPrice = addDataValue(adder);
                    newPrice = prices.get(adder);
                    timeOfTrade = times.get(adder).toString();
                    if (timeOfTrade.length() < 5) {
                        timeOfTrade = timeOfTrade + "0";
                    }
                    timeOfTrade = timeOfTrade + " " + yesterday;
                    timeOfTrade = timeOfTrade.replace(".", ":");
                    dateOfTrade = LocalDateTime.parse(timeOfTrade, dateTimeFormatter);
                    priceDifference = newPrice - buyPrice;
                    System.out.println("New price:" + newPrice + "\n Old price: " + buyPrice);
                    userMoney += priceDifference;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putFloat("money", (float) userMoney);
                    editor.apply();
                    insertTrade("Buy", companyTextView.getText().toString(), priceDifference, dateOfTrade);

                    String randomQuote = mentorQuotes.getRandomQuote(mentorName);
                    quoteTextView.setText(randomQuote);

                    Toast.makeText(TradeActivity.this, "Bought TSLA stock at price: " + buyPrice, Toast.LENGTH_LONG).show();
                    countdownHandler.startCountdown(new CountdownHandler.Callback() {
                        @Override
                        public void onCountdownFinished() {
                            if (priceDifference < 0) {
                                countdownTextView.setText("You lost $: " + dfStock.format(priceDifference));
                            } else {
                                countdownTextView.setText("You profited $: " + dfStock.format(priceDifference));
                            }
                            updateChart(dataVals);
                            adder = adder - 1;
                            editor.putInt("adder", adder);
                            editor.apply();
                            updateMoneyTextView();
                            System.out.println("ADDER: " + adder);
                            quoteTextView.setText("");
                        }
                    });
                }

            }
        });

        shortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adder < 0) {
                    companyTextView.setText("Stock market has closed for today.");
                    buyBtn.setEnabled(false);
                    shortBtn.setEnabled(false);
                    checker = yesterday.toString();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("dateChecker", (String) checker);
                    editor.apply();
                } else {
                    countdownHandler.cancelCountdown();
                    shortPrice = addDataValue(adder);
                    newPrice = prices.get(adder);
                    timeOfTrade = times.get(adder).toString();
                    if (timeOfTrade.length() < 5) {
                        timeOfTrade = timeOfTrade + "0";
                    }
                    timeOfTrade = timeOfTrade + " " + yesterday;
                    timeOfTrade = timeOfTrade.replace(".", ":");
                    dateOfTrade = LocalDateTime.parse(timeOfTrade, dateTimeFormatter);
                    priceDifference = shortPrice -newPrice;
                    userMoney += priceDifference;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putFloat("money", (float) userMoney);
                    editor.apply();
                    insertTrade("Short", companyTextView.getText().toString(), priceDifference, dateOfTrade);
                    String randomQuote = mentorQuotes.getRandomQuote(mentorName);
                    quoteTextView.setText(randomQuote);

                    Toast.makeText(TradeActivity.this, "Shorted TSLA stock at price: " + shortPrice, Toast.LENGTH_LONG).show();
                    countdownHandler.startCountdown(new CountdownHandler.Callback() {
                        @Override
                        public void onCountdownFinished() {
                            if (priceDifference < 0) {
                                countdownTextView.setText("You lost $: " + dfStock.format(priceDifference));
                            } else {
                                countdownTextView.setText("You profited $: " + dfStock.format(priceDifference));
                            }
                            updateChart(dataVals);
                            adder = adder - 1;
                            editor.putInt("adder", adder);
                            editor.apply();
                            updateMoneyTextView();
                            System.out.println("ADDER: " + adder);
                            quoteTextView.setText("");
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onTaskComplete(JSONObject output) {

        prices.clear();
        times.clear();

        try {
            companyTextView.setText(output.getJSONObject("Meta Data").getString("2. Symbol"));
            date = output.getJSONObject("Meta Data").getString("3. Last Refreshed");
            lastRefreshed = LocalDateTime.parse(date, formatter);
            JSONArray keys = output.getJSONObject("Time Series (1min)").names();
            for (int i = 0; i <= keys.length(); i++) {
                time = date.substring(11, 16);
                time = time.replace(":", ".");
                times.add(Float.parseFloat(time));
                prices.add(Float.parseFloat(output.getJSONObject("Time Series (1min)").getJSONObject(date).getString("4. close")));
                lastRefreshed = lastRefreshed.minusMinutes(1);
                date = lastRefreshed.toString();
                date = date + ":00";
                date = date.replace("T", " ");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        adder = preferences.getInt("adder", 0);
        checker = preferences.getString("dateChecker", "a");
        System.out.println("Yesterday: " + yesterday);
        System.out.println("checker: " + checker);

        if (checker.equals(yesterday + "")) {
            adder = -1;
            companyTextView.setText("Stock market has closed for today.");
            buyBtn.setEnabled(false);
            shortBtn.setEnabled(false);

        } else {

            if (adder == 0) {
                adder = times.size() - 11;
            } else if (adder < 0) {
                adder = times.size() - 11;
            }
            LineDataSet lineDataSet = new LineDataSet(dataValues(), "");
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet);

            lineDataSet.setLineWidth(4);
            lineDataSet.setValueTextSize(10);
            lineDataSet.setDrawValues(true);


            Legend legend = stockLineChart.getLegend();
            legend.setForm(Legend.LegendForm.EMPTY);

            LineData data = new LineData(dataSets);

            XAxis xAxis = stockLineChart.getXAxis();
            YAxis yAxisRight = stockLineChart.getAxisRight();
            yAxisRight.setEnabled(false);

            xAxis.setValueFormatter(new myXAxisValueFormatter());

            Description description = new Description();
            description.setText("");
            stockLineChart.setNoDataText("Loading... Or Maybe No Data Found?");
            stockLineChart.setDescription(description);
            stockLineChart.setDrawGridBackground(true);
            stockLineChart.setDrawBorders(true);
            stockLineChart.setData(data);
            stockLineChart.invalidate();
            System.out.println("Times size: " + times.size());
            System.out.println("Adder: " + adder);
            shortBtn.setEnabled(true);
            buyBtn.setEnabled(true);
        }
    }



    private ArrayList<Entry> dataValues() {
        if (adder == times.size() - 11) {
            Integer index = times.size() - 2;
            for (int i = 0; i < 9; i++) {
                dataVals.add(new Entry(times.get(index), prices.get(index)));
                index = index - 1;
            }
        } else {
            Integer index = adder + 9;
            for (int i = 0; i < 9; i++) {
                dataVals.add(new Entry(times.get(index), prices.get(index)));
                index --;
            }
        }
        return dataVals;
    }

    private Float addDataValue(int i) {
        if (i >= 0) {
            dataVals.add(new Entry(times.get(i), prices.get(i)));
            dataVals.remove(0);
            System.out.println("Time added: " + times.get(i) + "\nPrice added:  " +prices.get(i));
            return prices.get(i + 1);
        } else {
            Toast.makeText(TradeActivity.this, "Stock market has closed", Toast.LENGTH_LONG).show();
        } return null;
    }

    private void updateChart(ArrayList<Entry> dataVals) {
        LineDataSet lineDataSet = new LineDataSet(dataVals, "");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        LineData data = new LineData(dataSets);
        lineDataSet.setLineWidth(4);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setDrawValues(true);
        stockLineChart.setData(data);
        stockLineChart.invalidate();
    }

    private class myXAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            axis.setLabelCount(10, true);
            String buffer = value + "";
            String minutes = buffer.substring(3,4);
            String hours = buffer.substring(0,1);
            String nextHour = hours + ".00";
            Integer num = Integer.parseInt(minutes);
            if (num > 60) {
                value = Float.parseFloat(nextHour);
            }
            return "" + df.format(value);
        }
    }

    private void updateMoneyTextView() {
            moneyTextView.setText("Balance: $" + String.format("%.2f", userMoney));
    }

    private void insertTrade(String type, String companyName, float priceDifference, LocalDateTime timestamp) {
        ContentValues values = new ContentValues();
        values.put(TradeDatabaseHelper.COLUMN_TYPE, type);
        values.put(TradeDatabaseHelper.COLUMN_COMPANY_NAME, companyName);
        values.put(TradeDatabaseHelper.COLUMN_PRICE_DIFFERENCE, priceDifference);
        values.put(TradeDatabaseHelper.COLUMN_TIMESTAMP, timestamp.toString());
    
        long newRowId = database.insert(TradeDatabaseHelper.TABLE_NAME, null, values);
    
        if (newRowId != -1) {
            System.out.println("Trade record inserted with ID: " + newRowId);
        } else {
            System.out.println("Error inserting trade record");
        }
    }
    private int getMentorImageResource(Integer i) {
        if (i == 0) {
            return R.drawable.bjronvahlroos;
        } else if (i == 1) {
            return R.drawable.elonmusk;
        } else if (i == 2) {
            return R.drawable.jordanbelfort;
        } else if (i == 3) {
            return R.drawable.wallstreetbets;
        } else if (i == 4) {
            return R.drawable.warrenbuffett;
        } else {
            return R.drawable.default_mentor_image;
        }
    }
}*/


import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TradeActivity extends AppCompatActivity implements OnTaskComplete {

    TextView companyTextView;
    TextView countdownTextView;
    TextView moneyTextView;
    TextView quoteTextView;
    Button buyBtn;
    Button shortBtn;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");
    LocalDateTime lastRefreshed;
    String date;
    String time;
    String timeOfTrade;
    LocalDateTime dateOfTrade;
    Float buyPrice;
    Float shortPrice;
    Float newPrice;
    Float priceDifference;
    private double userMoney = 1000.0;
    ArrayList<Float> prices = new ArrayList<>();
    ArrayList<Float> times = new ArrayList<>();
    ArrayList<Entry> dataVals = new ArrayList<Entry>();
    LineChart stockLineChart;
    DecimalFormat df = new DecimalFormat("0.00");
    DecimalFormat dfStock = new DecimalFormat("0.000");
    Integer adder;
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String checker;
    Integer mentor;
    String mentorName;
    ImageView mImageView;
    String[] mentorNames = {"Bjrön Wahlroos", "Elon Musk", "Jordan Belfort", "WallStreetBets", "Warren Buffett"};

    private TradeDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private CountdownHandler countdownHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        initializeViews();
        setupCountdownHandler();
        setupButtonListeners();
        loadUserPreferences();
        setupDatabase();
    }

    private void initializeViews() {
        companyTextView = findViewById(R.id.companyTextView);
        countdownTextView = findViewById(R.id.countdownTextView);
        moneyTextView = findViewById(R.id.moneyTextView);
        quoteTextView = findViewById(R.id.quoteTextView);
        stockLineChart = findViewById(R.id.stockLineChart);
        mImageView = findViewById(R.id.mImageView);
        buyBtn = findViewById(R.id.buyBtn);
        shortBtn = findViewById(R.id.shortBtn);
    }

    private void setupCountdownHandler() {
        countdownHandler = new CountdownHandler(this);
        shortBtn.setEnabled(false);
        buyBtn.setEnabled(false);
    }

    private void setupButtonListeners() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adder < 0) {
                    companyTextView.setText("Stock market has closed for today.");
                    buyBtn.setEnabled(false);
                    shortBtn.setEnabled(false);
                    checker = yesterday.toString();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("dateChecker", (String) checker);
                    editor.apply();
                } else {
                    countdownHandler.cancelCountdown();
                    buyPrice = addDataValue(adder);
                    newPrice = prices.get(adder);
                    timeOfTrade = times.get(adder).toString();
                    if (timeOfTrade.length() < 5) {
                        timeOfTrade = timeOfTrade + "0";
                    }
                    timeOfTrade = timeOfTrade + " " + yesterday;
                    timeOfTrade = timeOfTrade.replace(".", ":");
                    dateOfTrade = LocalDateTime.parse(timeOfTrade, dateTimeFormatter);
                    priceDifference = newPrice - buyPrice;
                    System.out.println("New price:" + newPrice + "\n Old price: " + buyPrice);
                    userMoney += priceDifference;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putFloat("money", (float) userMoney);
                    editor.apply();
                    insertTrade("Buy", companyTextView.getText().toString(), priceDifference, dateOfTrade);
                    updateUIAfterBuy();
                    adder = adder - 1;
                    editor.putInt("adder", adder);
                    editor.apply();
                }

            }
        });

        shortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adder < 0) {
                    companyTextView.setText("Stock market has closed for today.");
                    buyBtn.setEnabled(false);
                    shortBtn.setEnabled(false);
                    checker = yesterday.toString();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("dateChecker", (String) checker);
                    editor.apply();
                } else {
                    countdownHandler.cancelCountdown();
                    shortPrice = addDataValue(adder);
                    newPrice = prices.get(adder);
                    timeOfTrade = times.get(adder).toString();
                    if (timeOfTrade.length() < 5) {
                        timeOfTrade = timeOfTrade + "0";
                    }
                    timeOfTrade = timeOfTrade + " " + yesterday;
                    timeOfTrade = timeOfTrade.replace(".", ":");
                    dateOfTrade = LocalDateTime.parse(timeOfTrade, dateTimeFormatter);
                    priceDifference = shortPrice -newPrice;
                    userMoney += priceDifference;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putFloat("money", (float) userMoney);
                    editor.apply();
                    insertTrade("Short", companyTextView.getText().toString(), priceDifference, dateOfTrade);
                    updateUIAfterShort();
                    adder = adder - 1;
                    editor.putInt("adder", adder);
                    editor.apply();
                }
            }
        });
    }

    private void loadUserPreferences() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userMoney = preferences.getFloat("money", 1000.0f);
        mentor = preferences.getInt("mentor", -1);
    }

    private void setupDatabase() {
        dbHelper = new TradeDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();
        mentorName = mentorNames[mentor];
        int mentorImageResource = getMentorImageResource(mentor);
        mImageView.setImageResource(mentorImageResource);
        new UpdateTask(this, this).execute();
        MentorQuotes mentorQuotes = new MentorQuotes();
    }

    @Override
    public void onTaskComplete(JSONObject output) {
        parseAndProcessJSONData(output);
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        adder = preferences.getInt("adder", 0);
        checker = preferences.getString("dateChecker", "a");

        /*if (checker.equals(yesterday.toString())) {
            adder = -1;
            companyTextView.setText("Stock market has closed for today.");
            buyBtn.setEnabled(false);
            shortBtn.setEnabled(false);
        } else *///{
            // Handle the case when the market is open...
            if (adder == 0 || adder < 0) {
                adder = times.size() - 11;
            }
            LineDataSet lineDataSet = new LineDataSet(dataValues(), "");
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet);

            lineDataSet.setLineWidth(4);
            lineDataSet.setValueTextSize(10);
            lineDataSet.setDrawValues(true);


            Legend legend = stockLineChart.getLegend();
            legend.setForm(Legend.LegendForm.EMPTY);

            LineData data = new LineData(dataSets);

            XAxis xAxis = stockLineChart.getXAxis();
            YAxis yAxisRight = stockLineChart.getAxisRight();
            yAxisRight.setEnabled(false);

            xAxis.setValueFormatter(new myXAxisValueFormatter());

            Description description = new Description();
            description.setText("");
            stockLineChart.setNoDataText("Loading... Or Maybe No Data Found?");
            stockLineChart.setDescription(description);
            stockLineChart.setDrawGridBackground(true);
            stockLineChart.setDrawBorders(true);
            stockLineChart.setData(data);
            stockLineChart.invalidate();
            System.out.println("Times size: " + times.size());
            System.out.println("Adder: " + adder);
            shortBtn.setEnabled(true);
            buyBtn.setEnabled(true);
        //}
    }

    private void parseAndProcessJSONData(JSONObject output) {
        prices.clear();
        times.clear();

        try {
            companyTextView.setText(output.getJSONObject("Meta Data").getString("2. Symbol"));
            date = output.getJSONObject("Meta Data").getString("3. Last Refreshed");
            lastRefreshed = LocalDateTime.parse(date, formatter);
            JSONArray keys = output.getJSONObject("Time Series (1min)").names();
            for (int i = 0; i <= keys.length(); i++) {
                time = date.substring(11, 16);
                time = time.replace(":", ".");
                times.add(Float.parseFloat(time));
                prices.add(Float.parseFloat(output.getJSONObject("Time Series (1min)").getJSONObject(date).getString("4. close")));
                lastRefreshed = lastRefreshed.minusMinutes(1);
                date = lastRefreshed.toString();
                date = date + ":00";
                date = date.replace("T", " ");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Entry> dataValues() {
        if (adder == times.size() - 11) {
            Integer index = times.size() - 2;
            for (int i = 0; i < 9; i++) {
                dataVals.add(new Entry(times.get(index), prices.get(index)));
                index = index - 1;
            }
        } else {
            Integer index = adder + 9;
            for (int i = 0; i < 9; i++) {
                dataVals.add(new Entry(times.get(index), prices.get(index)));
                index --;
            }
        }
        return dataVals;
    }

    private Float addDataValue(int i) {
        if (i >= 0) {
            dataVals.add(new Entry(times.get(i), prices.get(i)));
            dataVals.remove(0);
            System.out.println("Time added: " + times.get(i) + "\nPrice added:  " +prices.get(i));
            return prices.get(i + 1);
        } else {
            Toast.makeText(TradeActivity.this, "Stock market has closed", Toast.LENGTH_LONG).show();
        } return null;
    }

    private void updateChart(ArrayList<Entry> dataVals) {
        LineDataSet lineDataSet = new LineDataSet(dataVals, "");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        LineData data = new LineData(dataSets);
        lineDataSet.setLineWidth(4);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setDrawValues(true);
        stockLineChart.setData(data);
        stockLineChart.invalidate();
    }

    private class myXAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            axis.setLabelCount(10, true);
            String buffer = value + "";
            String minutes = buffer.substring(3,4);
            String hours = buffer.substring(0,1);
            String nextHour = hours + ".00";
            Integer num = Integer.parseInt(minutes);
            if (num > 60) {
                value = Float.parseFloat(nextHour);
            }
            return "" + df.format(value);
        }
    }

    private void updateMoneyTextView() {
        moneyTextView.setText("Balance: $" + String.format("%.2f", userMoney));
    }
    private void updateUIAfterBuy() {
        String randomQuote = MentorQuotes.getRandomQuote(mentorName);
        quoteTextView.setText(randomQuote);

        Toast.makeText(TradeActivity.this, "Bought TSLA stock at price: " + buyPrice, Toast.LENGTH_LONG).show();
        countdownHandler.startCountdown(new CountdownHandler.Callback() {
            @Override
            public void onCountdownFinished() {
                if (priceDifference < 0) {
                    countdownTextView.setText("You lost $: " + dfStock.format(priceDifference));
                } else {
                    countdownTextView.setText("You profited $: " + dfStock.format(priceDifference));
                }
                updateChart(dataVals);
                updateMoneyTextView();
                System.out.println("ADDER: " + adder);
                quoteTextView.setText("");
            }
        });

    }

    private void updateUIAfterShort() {
        String randomQuote = MentorQuotes.getRandomQuote(mentorName);
        quoteTextView.setText(randomQuote);
        Toast.makeText(TradeActivity.this, "Shorted TSLA stock at price: " + shortPrice, Toast.LENGTH_LONG).show();
        countdownHandler.startCountdown(new CountdownHandler.Callback() {
            @Override
            public void onCountdownFinished() {
                if (priceDifference < 0) {
                    countdownTextView.setText("You lost $: " + dfStock.format(priceDifference));
                } else {
                    countdownTextView.setText("You profited $: " + dfStock.format(priceDifference));
                }
                updateChart(dataVals);
                updateMoneyTextView();
                System.out.println("ADDER: " + adder);
                quoteTextView.setText("");
            }
        });
    }


    private void insertTrade(String type, String companyName, float priceDifference, LocalDateTime timestamp) {
        ContentValues values = new ContentValues();
        values.put(TradeDatabaseHelper.COLUMN_TYPE, type);
        values.put(TradeDatabaseHelper.COLUMN_COMPANY_NAME, companyName);
        values.put(TradeDatabaseHelper.COLUMN_PRICE_DIFFERENCE, priceDifference);
        values.put(TradeDatabaseHelper.COLUMN_TIMESTAMP, timestamp.toString());

        long newRowId = database.insert(TradeDatabaseHelper.TABLE_NAME, null, values);

        if (newRowId != -1) {
            System.out.println("Trade record inserted with ID: " + newRowId);
        } else {
            System.out.println("Error inserting trade record");
        }
    }


    private int getMentorImageResource(Integer i) {
        if (i == 0) {
            return R.drawable.bjronvahlroos;
        } else if (i == 1) {
            return R.drawable.elonmusk;
        } else if (i == 2) {
            return R.drawable.jordanbelfort;
        } else if (i == 3) {
            return R.drawable.wallstreetbets;
        } else if (i == 4) {
            return R.drawable.warrenbuffett;
        } else {
            return R.drawable.default_mentor_image;
        }
    }

}
