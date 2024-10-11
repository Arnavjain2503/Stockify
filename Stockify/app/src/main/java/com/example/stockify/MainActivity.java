package com.example.stockify;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import android.content.Intent;
import android.net.Uri;
public class MainActivity extends AppCompatActivity {

    ImageView search, companyLogoImageView, priceImageView;
    TextView stockPriceTextView, stockPriceLabel, exchangeText, companyText, loadingText;
    EditText symbol;
    FrameLayout loadingOverlay;
    CardView animatedCardView;
    CardView additionalcardview;
    LinearLayout colorLinearLayout;

    TextView additionalExchangeTextView , highPriceTextView, openPriceTextView, lowPriceTextView, previousCloseTextView, urlTextView;

    private StockViewModel stockViewModel;
    private Runnable priceUpdateRunnable;

    private Handler priceUpdateHandler = new Handler();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isToastVisible = false;
    private Double previousPrice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        loadingOverlay = findViewById(R.id.loadingOverlay);
        stockPriceTextView = findViewById(R.id.stockPriceTextView);
        stockPriceLabel = findViewById(R.id.stockPriceLabel);
        exchangeText = findViewById(R.id.exchangeText);
        companyText = findViewById(R.id.companyTextView);
        loadingText = findViewById(R.id.LoadingText);
        symbol = findViewById(R.id.symbolName);
        search = findViewById(R.id.search);
        companyLogoImageView = findViewById(R.id.companyLogoImageView);
        animatedCardView = findViewById(R.id.animatedCardView);
        additionalcardview = findViewById(R.id.additionalcardview);
        colorLinearLayout = findViewById(R.id.colorLinearLayout);
        priceImageView = findViewById(R.id.priceImageView);

        additionalExchangeTextView = findViewById(R.id.additionalExchangeTextView);

        highPriceTextView = findViewById(R.id.highprice);
        openPriceTextView = findViewById(R.id.openprice);
        lowPriceTextView = findViewById(R.id.lowprice);
        previousCloseTextView = findViewById(R.id.previousclose);
        urlTextView = findViewById(R.id.URL);

        animatedCardView.setVisibility(View.GONE); // Hide the CardView initially
        additionalcardview.setVisibility(View.GONE); // Hide the CardView initially

        // Initialize ViewModel
        stockViewModel = new ViewModelProvider(this).get(StockViewModel.class);

        // Observe LiveData from ViewModel
        stockViewModel.getStockPrice().observe(this, stockPrice -> {
            if (stockPrice != null) {
                updateStockPrice(stockPrice);
            }
        });

        stockViewModel.getCompanyName().observe(this, companyName -> {
            if (companyName != null) {
                companyText.setText(companyName);
            }
        });

        stockViewModel.getLogoUrl().observe(this, logoUrl -> {
            if (logoUrl != null) {
                Glide.with(this)
                        .load(logoUrl)
                        .into(companyLogoImageView);
            }
        });

        stockViewModel.getHighPrice().observe(this, highPrice -> {
            if (highPrice != null) {
                highPriceTextView.setText(String.format("%.2f", highPrice));
            }
        });

        stockViewModel.getOpenPrice().observe(this, openPrice -> {
            if (openPrice != null) {
                openPriceTextView.setText(String.format("%.2f", openPrice));
            }
        });

        stockViewModel.getLowPrice().observe(this, lowPrice -> {
            if (lowPrice != null) {
                lowPriceTextView.setText(String.format("%.2f", lowPrice));
            }
        });

        stockViewModel.getPreviousClosePrice().observe(this, previousClose -> {
            if (previousClose != null) {
                previousCloseTextView.setText(String.format("%.2f", previousClose));
            }
        });

        stockViewModel.getWebUrl().observe(this, url -> {
            if (url != null) {
                urlTextView.setText(url);
            }
        });

        stockViewModel.isDataValid().observe(this, isValid -> {
            if (isValid) {
                additionalcardview.setVisibility(View.GONE);
                animatedCardView.setVisibility(View.VISIBLE);
            } else {
                additionalcardview.setVisibility(View.GONE);
                animatedCardView.setVisibility(View.GONE);
            }
        });

        stockViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && !isToastVisible) {
                isToastVisible = true;
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        stockViewModel.getExchange().observe(this, exchange -> {
            if (exchange != null) {
                additionalExchangeTextView.setText("Exchange: "+ exchange);
            }
        });

        stockViewModel.getPercentageChange().observe(this, change -> {
            if (change != null) {
                exchangeText.setText(change);
            }
        });

        stockViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                showLoadingOverlay();
            } else {
                hideLoadingOverlay();
            }
        });

        stockViewModel.getStockSymbol().observe(this, stockSymbol -> {
            if (stockSymbol != null) {
                stockPriceLabel.setText(stockSymbol);
            }
        });

        // Search button click
        search.setOnClickListener(view -> {
            String symbolString = symbol.getText().toString();
            if (!symbolString.isEmpty()) {
                colorLinearLayout.setBackgroundColor(Color.WHITE);
                previousPrice = null;
                loadingText.setText("");
                isToastVisible = false;
                stockViewModel.fetchStockPrice(symbolString);
                startPeriodicPriceUpdate(symbolString);
                priceImageView.setImageResource(R.drawable.white);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a valid symbol", Toast.LENGTH_SHORT).show();
            }
        });

        //Company URL Click
        urlTextView.setOnClickListener(view -> {
            String url = urlTextView.getText().toString();
            if (!url.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } else {
                Toast.makeText(MainActivity.this, "No URL available", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Displays a loading indicator (overlay) while fetching stock data,
    //and hides the stock data card to prevent user interaction during the fetch.
    private void showLoadingOverlay() {
        loadingOverlay.setVisibility(View.VISIBLE);
        animatedCardView.setVisibility(View.GONE);
        additionalcardview.setVisibility(View.GONE);
        search.setEnabled(false);
    }

    //Hides the loading indicator and re-displays the stock data card if valid stock data is fetched.
    private void hideLoadingOverlay() {
        loadingOverlay.setVisibility(View.GONE);

        if (stockViewModel.getStockPrice().getValue() != null) {
            animatedCardView.setVisibility(View.VISIBLE);
            additionalcardview.setVisibility(View.VISIBLE);
        }

        search.setEnabled(true);
    }

    //Updates the displayed stock price, manages UI updates for price changes,
    //and color codes (green for increase, red for decrease) with an icon to show the stock trend.
    private void updateStockPrice(double currentPrice) {
        handler.post(() -> {
            String formattedPrice = String.format("%.2f", currentPrice);
            stockPriceTextView.setText(formattedPrice);

            if (previousPrice != null) {
                animatedCardView.setVisibility(View.VISIBLE);
                double priceDifference = Math.abs(currentPrice - previousPrice);
                String priceDiffFormatted = String.format("%.2f", priceDifference);

                loadingText.setText("(" + priceDiffFormatted + ")");

                if (currentPrice > previousPrice) {
                    colorLinearLayout.setBackgroundColor(Color.GREEN);
                    priceImageView.setImageResource(R.drawable.up_image);
                } else if (currentPrice < previousPrice) {
                    colorLinearLayout.setBackgroundColor(Color.RED);
                    priceImageView.setImageResource(R.drawable.down_image);
                }

            }
            previousPrice = currentPrice;
        });
    }
    //Schedules periodic stock price updates every 60 seconds,
    //re-fetching the stock data for the provided symbol.
    private void startPeriodicPriceUpdate(String symbol) {
        if (priceUpdateRunnable != null) {
            priceUpdateHandler.removeCallbacks(priceUpdateRunnable);
        }

        priceUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                stockViewModel.fetchStockPrice(symbol);
                priceUpdateHandler.postDelayed(this, 60000);
            }
        };

        priceUpdateHandler.post(priceUpdateRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        priceUpdateHandler.removeCallbacks(priceUpdateRunnable);
    }
}
