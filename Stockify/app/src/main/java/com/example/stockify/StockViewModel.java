package com.example.stockify;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StockViewModel extends ViewModel {
    private final MutableLiveData<Double> stockPrice = new MutableLiveData<>();
    private final MutableLiveData<String> companyName = new MutableLiveData<>();
    private final MutableLiveData<String> percentageChange = new MutableLiveData<>();
    private final MutableLiveData<String> logoUrl = new MutableLiveData<>();
    private final MutableLiveData<String> webUrl = new MutableLiveData<>();
    private final MutableLiveData<String> exchange = new MutableLiveData<>();
    private final MutableLiveData<Double> highPrice = new MutableLiveData<>();
    private final MutableLiveData<Double> lowPrice = new MutableLiveData<>();
    private final MutableLiveData<Double> openPrice = new MutableLiveData<>();
    private final MutableLiveData<Double> previousClosePrice = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> stockSymbol = new MutableLiveData<>();
    private final StockApiService stockApiService;

    private static final String API_KEY = "cruh171r01qvnd3ndoa0cruh171r01qvnd3ndoag";

    public StockViewModel() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://finnhub.io/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        stockApiService = retrofit.create(StockApiService.class);
    }

    // Getters for LiveData
    public LiveData<Double> getStockPrice() {
        return stockPrice;
    }

    public LiveData<String> getCompanyName() {
        return companyName;
    }

    public LiveData<String> getPercentageChange() {
        return percentageChange;
    }

    public LiveData<String> getLogoUrl() {
        return logoUrl;
    }

    public LiveData<String> getWebUrl() {
        return webUrl;
    }

    public LiveData<String> getExchange() {
        return exchange;
    }

    public LiveData<Double> getHighPrice() {
        return highPrice;
    }

    public LiveData<Double> getLowPrice() {
        return lowPrice;
    }

    public LiveData<Double> getOpenPrice() {
        return openPrice;
    }

    public LiveData<Double> getPreviousClosePrice() {
        return previousClosePrice;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getStockSymbol() {
        return stockSymbol;
    }

    public void fetchStockPrice(String symbol) {
        isLoading.setValue(true); // Set loading state to true

        stockApiService.getStockQuote(symbol, API_KEY).enqueue(new Callback<StockQuoteResponse>() {
            @Override
            public void onResponse(Call<StockQuoteResponse> call, Response<StockQuoteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StockQuoteResponse quote = response.body();
                    double price = quote.getCurrentPrice();
                    if (price > 0) {
                        double change = quote.getPercentageChange();
                        stockPrice.setValue(price);
                        percentageChange.setValue("% Change: " + change);
                        highPrice.setValue(quote.getHighPrice());
                        lowPrice.setValue(quote.getLowPrice());
                        openPrice.setValue(quote.getOpenPrice());
                        previousClosePrice.setValue(quote.getPreviousClosePrice());
                        stockSymbol.setValue(symbol);
                        fetchCompanyProfile(symbol); // Fetch company profile if stock data is valid
                    } else {
                        // Trigger error message for invalid stock symbol
                        errorMessage.setValue("The symbol you entered is invalid. Please try again.");
                        isLoading.setValue(false); // Stop loading if error
                    }
                } else {
                    // Trigger error for invalid symbol or empty response
                    errorMessage.setValue("The symbol you entered is invalid. Please try again.");
                    isLoading.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<StockQuoteResponse> call, Throwable t) {
                // Trigger network error message once
                errorMessage.setValue("Please check your internet connection and try again.");
                isLoading.setValue(false);
                t.printStackTrace();
            }
        });
    }

    private void fetchCompanyProfile(String symbol) {
        // Fetch company profile only if stock data is valid
        stockApiService.getCompanyProfile(symbol, API_KEY).enqueue(new Callback<CompanyProfileResponse>() {
            @Override
            public void onResponse(Call<CompanyProfileResponse> call, Response<CompanyProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getName() != null) {
                    CompanyProfileResponse profile = response.body();
                    companyName.setValue("Name: " + profile.getName());
                    logoUrl.setValue(profile.getLogo());
                    webUrl.setValue(profile.getWebUrl());
                    exchange.setValue(profile.getExchange());
                }
                isLoading.setValue(false); // Stop loading after company profile is fetched
            }

            @Override
            public void onFailure(Call<CompanyProfileResponse> call, Throwable t) {
                isLoading.setValue(false);
            }
        });
    }
}
