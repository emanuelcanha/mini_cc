package com.minicc;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;


public class AddActivity extends SherlockListActivity {
	private final String SHARED_PREFS_FILE = "currencies";
	private SharedPreferences prefs;
	private Editor editor;
	private MyAdapter adapter;
	private static List<Currency> allCurrencies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTheme(R.style.Theme_Sherlock);
		setContentView(R.layout.add_currency);

		prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
		editor = prefs.edit();
		String currenciesJSONString = prefs.getString("allCurrencies", null);

		allCurrencies = new ArrayList<Currency>();
		allCurrencies = Util.getCurrenciesList(currenciesJSONString);

		Log.d("Currencies Size", String.valueOf(allCurrencies.size()));

		adapter = new MyAdapter(this, allCurrencies);
		setListAdapter(adapter);

	}

	public void Save(View v) {

		List<Currency> personalCurrencies = new ArrayList<Currency>();
		personalCurrencies = Util.getPersonalCurrencyList(allCurrencies);

		editor.putString("allCurrencies", Util.getJSONString(allCurrencies));
		editor.putString("currencies", Util.getJSONString(personalCurrencies));
		editor.commit();

		this.finish();
	}

	public static class MyAdapter extends ArrayAdapter<Currency> {

		private final List<Currency> list;
		private final Activity context;

		public MyAdapter(Activity context, List<Currency> list) {
			super(context, R.layout.list_row, list);
			this.context = context;
			this.list = list;
		}

		static class ViewHolder {
			protected TextView text;
			protected CheckBox checkbox;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if (convertView == null) {
				LayoutInflater inflator = context.getLayoutInflater();
				view = inflator.inflate(R.layout.list_row, null);
				final ViewHolder viewHolder = new ViewHolder();
				viewHolder.text = (TextView) view.findViewById(R.id.list_text);
				viewHolder.checkbox = (CheckBox) view.findViewById(R.id.add_currency_checkbox);
				viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Currency currency = (Currency) viewHolder.checkbox.getTag();
						currency.active = buttonView.isChecked();

					}
				});
				view.setTag(viewHolder);
				viewHolder.checkbox.setTag(list.get(position));
				
			} else {
				
				view = convertView;
				((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
			}
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.text.setText(list.get(position).code + " - " + list.get(position).name);
			holder.checkbox.setChecked(list.get(position).active);
			return view;
		}
	}

}
