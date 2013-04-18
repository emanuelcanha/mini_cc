package com.minicc;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ConverterActivity extends SherlockActivity {
	private EditText amount;
	private Spinner fromSpinner, toSpinner;
	private final String SHARED_PREFS_FILE = "currencies";
	private SharedPreferences prefs;
	private Editor editor;
	private MyAdapter adapter;
	private List<Currency> personalCurrencies;
	private ProgressBar progressBar;
	private TextView resultText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		fromSpinner = (Spinner) findViewById(R.id.spinner_from);
		toSpinner = (Spinner) findViewById(R.id.spinner_to);
		resultText = (TextView) findViewById(R.id.resultText);
		personalCurrencies = new ArrayList<Currency>();
		amount = (EditText) findViewById(R.id.edittext_convert_amount);
		amount.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				
				if (actionId == EditorInfo.IME_ACTION_DONE)
					Convert();
					
				return true;
			}
		});

		prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
		editor = prefs.edit();

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

	}

	@Override
	protected void onResume() {
		super.onResume();

		String currenciesJSON = prefs.getString("currencies", null);

		if (currenciesJSON != null) {

			personalCurrencies = Util.getCurrenciesList(currenciesJSON);
			setSpinners();

		} else

			loadDefaultCurrencies();
	}

	private void setSpinners() {

		adapter = new MyAdapter(personalCurrencies);

		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);

		int fromSelected = prefs.getInt("spinnerFromSelection", 0);
		int toSelected = prefs.getInt("spinnerToSelection", 0);
		int personalCurrenciesNum = personalCurrencies.size() - 1;

		if (fromSelected <= personalCurrenciesNum)
			fromSpinner.setSelection(fromSelected, true);

		if (toSelected <= personalCurrenciesNum)
			toSpinner.setSelection(toSelected, true);
	}

	private void loadDefaultCurrencies() {

		String allCurrenciesJSON = Util.loadDefaultCurrenciesToString(this);

		List<Currency> allCurrencies = Util.getCurrenciesList(allCurrenciesJSON);

		// Gets arraylist with the default selected currencies and saves as JSON
		personalCurrencies = Util.getPersonalCurrencyList(allCurrencies);

		setSpinners();

		editor.putString("allCurrencies", allCurrenciesJSON);
		editor.putString("currencies", Util.getJSONString(personalCurrencies));
		editor.commit();
	}

	@Override
	protected void onPause() {
		super.onPause();

		prefs.edit();

		if (fromSpinner.getAdapter() != null && toSpinner.getAdapter() != null) {

			editor.putInt("spinnerFromSelection", fromSpinner.getSelectedItemPosition());
			editor.putInt("spinnerToSelection", toSpinner.getSelectedItemPosition());

			editor.commit();
		}
	};
	
	public void ConvertClicked(View v) {
		Convert();
	}

	public void Convert() {
		Currency c;

		if (adapter.getCount() == 0)
			return;

		else if (!Util.isNetworkAvailable(this))
			resultText.setText("No Internet Connection.");

		else {
			c = (Currency) fromSpinner.getSelectedItem();
			String fromCurr = c.code;

			c = (Currency) toSpinner.getSelectedItem();
			String toCurr = c.code;

			Convert(fromCurr, toCurr, amount.getText().toString());
		}

	}

	public void Switch(View v) {
		int from = fromSpinner.getSelectedItemPosition();
		int to = toSpinner.getSelectedItemPosition();

		fromSpinner.setSelection(to, true);
		toSpinner.setSelection(from, true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add("Add").setIcon(R.drawable.ic_action_add_currency).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == 0) {
			openAddCurrencyActivity();
			return true;
		}

		return super.onOptionsItemSelected(item);

	}

	private void openAddCurrencyActivity() {
		Intent addCurrency = new Intent();
		addCurrency.setClass(getApplicationContext(), AddActivity.class);
		startActivity(addCurrency);
	}

	private void Convert(String from_Currency, String to_Currency, String amount) {

		// www.google.com/ig/calculator?q=10USD=?EUR - Google calculator API
		String url = "http://www.google.com/ig/calculator?q=" + amount + from_Currency + "=?" + to_Currency;

		new DownloadTask().execute(url);

	}

	private class DownloadTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			resultText.setText("");
		}

		@Override
		protected String doInBackground(String... url) {

			String httpResult = Util.Download(url[0]);
			return httpResult;
		}

		@Override
		protected void onPostExecute(String result) {

			showResult(result);
			progressBar.setVisibility(View.INVISIBLE);
		}

	}

	private void showResult(String result) {
		String[] resultValue = result.split("\"");
		resultText.setText(resultValue[3].replace("\ufffd", " "));
	}
	

	private class MyAdapter implements SpinnerAdapter {
		List<Currency> mCurrencies;

		public MyAdapter(List<Currency> currencies) {
			mCurrencies = currencies;
		}

		@Override
		public int getCount() {
			return mCurrencies.size();
		}

		@Override
		public Object getItem(int position) {
			return mCurrencies.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return android.R.layout.simple_spinner_item;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.spinner_row, parent, false);

			TextView textView = (TextView) row.findViewById(R.id.dropdown_text);

			Currency currency = mCurrencies.get(position);
			textView.setText(currency.code);
			return textView;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.spinner_row_item, parent, false);

			TextView textView = (TextView) row.findViewById(R.id.spinner_text);

			Currency currency = mCurrencies.get(position);
			textView.setText(currency.code + " - " + currency.name);

			return textView;

		}

	}

}
