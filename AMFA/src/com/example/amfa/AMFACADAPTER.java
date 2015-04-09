package com.example.amfa;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AMFACADAPTER extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> mdata;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ArrayList<String> msgi;

	public AMFACADAPTER(Activity amfachat,
			ArrayList<HashMap<String, String>> stringslist,
			ArrayList<String> imglist) {
		activity = amfachat;
		mdata = stringslist;
		msgi = imglist;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
	}

	public int getCount() {
		return mdata.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		CharSequence msgs;

		View v = convertView;
		if (convertView == null)
			v = inflater.inflate(R.layout.chat_message_row, null);

		TextView textView0 = (TextView) v.findViewById(R.id.cmr_jid);
		TextView textView1 = (TextView) v.findViewById(R.id.cmr_date);
		TextView textView2 = (TextView) v.findViewById(R.id.cmr_text);
		ImageView imageView0 = (ImageView) v.findViewById(R.id.cmr_img);

		HashMap<String, String> message = new HashMap<String, String>();
		message = mdata.get(position);

		// Меняем цвет отправителя
		if (message.get(AMFACHAT.KEY_SOURCE) == "IN") {
			textView0.setTextColor(Color.RED);
		} else {
			textView0.setTextColor(Color.BLUE);
		}

		// Задаем значения
		textView0.setText(message.get(AMFACHAT.KEY_JID));
		textView1.setText(message.get(AMFACHAT.KEY_DATE));
		// Парсим смайлики
		msgs = addSmileysToMessage(message.get(AMFACHAT.KEY_TEXT));
		textView2.setText(msgs);

		// Показываем изображение
		if (msgi.get(position) != "noimage") {
			imageView0.setVisibility(View.VISIBLE);
			imageLoader.DisplayImage(msgi.get(position), imageView0);
		} else {
			imageView0.setVisibility(View.GONE);
		}

		// Возвращаем вид
		return v;
	}

	// Добавляем смайлы Spannable в строку
	// @param сообщение с смайлами
	// @return сообщ с смайлами spannable

	private CharSequence addSmileysToMessage(String msg) {
		SmileyParser parser = SmileyParser.getInstance();
		return parser.addSmileySpans(msg);
	}

}