package com.minicc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class Util {

	public static String Download(String urlString) {
		String teste = "";
		try {
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			teste = readStream(con.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.d("Result", teste);
		return teste;

	}

	private static String readStream(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static String getJSONString(final List<Currency> currencyList) {

		JSONArray currenciesJSONArray = new JSONArray();
		JSONObject currencyJSONObject;

		for (Currency currency : currencyList) {

			currencyJSONObject = new JSONObject();

			try {

				currencyJSONObject.put("code", currency.code);
				currencyJSONObject.put("country", currency.country);
				currencyJSONObject.put("symbol", currency.symbol);
				currencyJSONObject.put("name", currency.name);
				currencyJSONObject.put("active", currency.active);

			} catch (JSONException e) {
				e.printStackTrace();
			}

			currenciesJSONArray.put(currencyJSONObject);
		}

		return currenciesJSONArray.toString();
	}

	public static List<Currency> getCurrenciesList(String currenciesJSONArrayString) {
		JSONObject currencyObject;
		JSONArray currenciesJSONArray = null;
		Currency currency = null;

		List<Currency> currencies = new ArrayList<Currency>();

		try {

			currenciesJSONArray = new JSONArray(currenciesJSONArrayString);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < currenciesJSONArray.length(); i++) {

			try {

				currencyObject = (JSONObject) currenciesJSONArray.getJSONObject(i);

				currency = new Currency(currencyObject.getString("name"), currencyObject.getString("code"),
						currencyObject.getString("symbol"), currencyObject.getString("country"),
						currencyObject.getBoolean("active"));

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			currencies.add(currency);
		}

		return currencies;
	}

	public static String loadDefaultCurrenciesToString(Context context) {
		String jsonString = null;

		InputStream is = context.getResources().openRawResource(R.raw.currencies);

		try {
			byte[] b = new byte[is.available()];
			is.read(b);
			jsonString = new String(b);

		} catch (Exception e) {

		}

		return jsonString;
	}

	public static List<Currency> getPersonalCurrencyList(List<Currency> allCurrenciesList) {

		List<Currency> personalCurrencies = new ArrayList<Currency>();

		for (Currency currency : allCurrenciesList) {

			if (currency.active)
				personalCurrencies.add(currency);
		}

		return personalCurrencies;
	}

}
