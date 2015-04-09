package com.example.amfa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class AMFACHAT extends Activity {

	// 11 private - заменил на public
	public Handler mHandler = new Handler();
	public EditText mRecipient;
	public EditText mSendText;
	public ListView mList;
	public XMPPConnection connection;
	public String jid;
	public String recipientjid;
	public ArrayList<HashMap<String, String>> stringslist;
	public String imgurl;
	public ArrayList<String> imglist;
	public int presencesaved;
	private int NOTIFICATION_ID;
	private int ctab2;

	static final String KEY_JID = "jid";
	static final String KEY_DATE = "date";
	static final String KEY_TEXT = "text";
	static final String KEY_SOURCE = "source";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		// Парсер смайликов для чата
		SmileyParser.init(this);

		// Используем общее подключение
		connection = AMFAXL.getInstance().getConnection();

		mRecipient = (EditText) this.findViewById(R.id.recipient);
		mSendText = (EditText) this.findViewById(R.id.sendText);
		mList = (ListView) this.findViewById(R.id.listMessages);
		// setListAdapter();
		// Создаем инстансы переменных
		stringslist = new ArrayList<HashMap<String, String>>();
		imglist = new ArrayList<String>();

		mSendText.requestFocus();

		// Получаем данные из другого активити
		// добавил getParent(). т.к. табс родитель
		jid = getParent().getIntent().getExtras().getString("eUserid");

		// Слушатель пакетов входящих сообщений
		PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
		connection.addPacketListener(new PacketListener() {
			public void processPacket(Packet packet) {
				Message message = (Message) packet;
				if (message.getBody() != null) {
					// Получаем имя отправителя
					String fromName = StringUtils.parseBareAddress(message
							.getFrom());

					// Парсим картинки
					imgurl = "noimage";
					imgurl = getLink(message.getBody());

					// Если ссылка содержит расширение файла
					// Расширение для Android 4.0+, сделать заглушку
					// || !msgi.contains(".webp")
					if (URLUtil.isValidUrl(imgurl) == true) {
						if (!imgurl.contains(".jpg")
								|| !imgurl.contains(".gif")
								|| !imgurl.contains(".png")
								|| !imgurl.contains(".bmp")) {
						} else {
							// Если ссылка не правильная не отображаем
							imgurl = "noimage";
						}
					} else {
						imgurl = "noimage";
					}

					// Заполняем массив ссылками
					imglist.add(imgurl);

					// Отметка времени
					Long TimeStampLong = System.currentTimeMillis();

					// Ложим данные в переменные для адаптера
					HashMap<String, String> stringsmap = new HashMap<String, String>();
					stringsmap.put(KEY_JID, fromName);
					stringsmap.put(KEY_DATE, getDateString(TimeStampLong));
					stringsmap.put(KEY_TEXT, message.getBody());
					stringsmap.put(KEY_SOURCE, "IN");
					stringslist.add(stringsmap);

					// Добавляем входящие сообщения в список
					mHandler.post(new Runnable() {
						public void run() {
							setListAdapter();
						}
					});

					// Уведомление в статус бар если вкладка контактов
					// Узнаем какая сейчас вкладка
					getTabInActivity();
					// Если вкладка контактов то выводим уведомление
					if (ctab2 == 0) {
						triggerNotification(getString(R.string.newmsgfrom)
								+ " " + fromName);
					}
					// if in background - show message, by service

				}
			}
		}, filter);

		// Прослушиватель для отправки сообщений
		Button send = (Button) this.findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (isAllRightRecipient()) {
					mRecipient.requestFocus();
					Toast.makeText(getApplicationContext(),
							R.string.norecipient, Toast.LENGTH_SHORT).show();
				} else {
					if (isAllRightMessage()) {
						mSendText.requestFocus();
						Toast.makeText(getApplicationContext(),
								R.string.nomessage, Toast.LENGTH_SHORT).show();
					} else {
						// else if
						String to = mRecipient.getText().toString();
						String text = mSendText.getText().toString();
						Log.i("AMFA", "Sending text [" + text + "] to [" + to
								+ "]");
						Message msg = new Message(to, Message.Type.chat);
						msg.setBody(text);
						connection.sendPacket(msg);

						// Парсим картинки
						imgurl = "noimage";
						imgurl = getLink(text);

						// Если ссылка содержит расширение файла
						if (URLUtil.isValidUrl(imgurl) == true) {
							if (!imgurl.contains(".jpg")
									|| !imgurl.contains(".gif")
									|| !imgurl.contains(".png")
									|| !imgurl.contains(".bmp")) {
							} else {
								// Если ссылка не правильная не отображаем
								imgurl = "noimage";
							}
						}

						// Заполняем массив ссылками
						imglist.add(imgurl);

						// Отметка времени
						Long TimeStampLong = System.currentTimeMillis();

						// Ложим данные в переменные для адаптера
						HashMap<String, String> stringsmap = new HashMap<String, String>();
						stringsmap.put(KEY_JID, jid);
						stringsmap.put(KEY_DATE, getDateString(TimeStampLong));
						stringsmap.put(KEY_TEXT, text);
						stringsmap.put(KEY_SOURCE, "OUT");
						stringslist.add(stringsmap);

						// Очистили поле текстового ввода
						mSendText.setText(null);
						// Уведомляем, что данные изменились
						setListAdapter();
					}
				}
			}
		});
	}

	// Получаем текущую вкладку
	public void getTabInActivity() {
		AMFATABS amfatabshandler;
		amfatabshandler = (AMFATABS) this.getParent();
		amfatabshandler.getcurrenttab();
		ctab2 = AMFATABS.ctab;
	}

	// Уведомление в статусную строку
	@SuppressWarnings("deprecation")
	private void triggerNotification(String nmsg) {
		NOTIFICATION_ID = 1;
		CharSequence title = "AMFA";
		CharSequence message = nmsg;
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,
				nmsg, System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		Intent notificationIntent = new Intent(this, AMFATABS.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// (this, 0, notificationIntent/null , 0);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		// this
		notification.setLatestEventInfo(getApplicationContext(), title,
				message, pendingIntent);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	// Форматируем отметку времени в нужный формат и отдаем строку
	public String getDateString(long milliSeconds) {
		Locale locale = Locale.US;
		SimpleDateFormat dateFormater = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", locale);
		Date date = new Date(milliSeconds);
		return dateFormater.format(date);
	}

	// Проверяем указан ли получатель
	public boolean isAllRightRecipient() {
		if (mRecipient.getText().toString().trim().equals("")
				|| !mRecipient.getText().toString().contains("@")) {
			return true;
		} else {
			return false;
		}
	}

	// Проверяем не пустое ли сообщение
	public boolean isAllRightMessage() {
		if (mSendText.getText().toString().trim().equals("")) {
			return true;
		} else {
			return false;
		}
	}

	// Адаптер строки чата
	private void setListAdapter() {
		// Передали в адаптер данные
		AMFACADAPTER adapter = new AMFACADAPTER(this, stringslist, imglist);
		// Задали адаптер
		mList.setAdapter(adapter);
		// Уведомили адаптер об измененных данных
		adapter.notifyDataSetChanged();
		// Обновили графически
		mList.refreshDrawableState();
		// Скролл на последнее сообщение Bottom
		// mList.smoothScrollToPosition(mList.getTop());
	}

	// Парсер изображений
	private String getLink(String msg) {
		// Совпадает ли сообщение с маской ссылки
		Matcher urlmatcher = Regex.WEB_URL_PATTERN.matcher(msg);
		while (urlmatcher.find()) {
			imgurl = urlmatcher.group(0);
		}
		return imgurl;
	}

	// При переключении или сворачивании
	protected void onPause() {
		super.onPause();
		// Получаем статус до паузы
		presencesaved = AMFAROSTER.PresenceSaved;
	}

	// При переключении вкладки или восстановлении
	@Override
	protected void onResume() {
		super.onResume();
		// Проверяем соединение и если оно есть то
		if (isInternetOn()) {

			// Получим статус при восстановлении
			presencesaved = AMFAROSTER.PresenceSaved;

			// Адресат полученный из списка контактов
			// Мы его очищаем при каждом открытии т.к. получаем новый
			recipientjid = null;
			recipientjid = AMFAROSTER.outterjid;
			// Задаем поле получателя полученным контактом
			if (recipientjid != null) {
				mRecipient.setText(recipientjid);
			}
			mSendText.requestFocus();

			// Задаем сохраненный статус подключению
			Presence presenceresume = null;
			switch (presencesaved) {
			case 0: {
				presenceresume = new Presence(Presence.Type.available);
				presenceresume.setMode(Presence.Mode.chat);
				break;
			}
			case 1: {
				presenceresume = new Presence(Presence.Type.available);
				presenceresume.setMode(Presence.Mode.available);
				break;
			}
			case 2: {
				presenceresume = new Presence(Presence.Type.available);
				presenceresume.setMode(Presence.Mode.dnd);
				break;
			}
			case 3: {
				presenceresume = new Presence(Presence.Type.available);
				presenceresume.setMode(Presence.Mode.away);
				break;
			}
			case 4: {
				presenceresume = new Presence(Presence.Type.available);
				presenceresume.setMode(Presence.Mode.xa);
				break;
			}
			case 5: {
				presenceresume = new Presence(Presence.Type.unavailable);
				break;
			}
			}
			// Сообщаем серверу свой статус
			connection.sendPacket(presenceresume);
		} else {
			// Интернета нет - Запускаем страницу настроек
			Intent intent = new Intent(AMFACHAT.this, AMFA.class);
			startActivity(intent);
			finish();
		}
	}

	// Проверка наличие интернета
	public final boolean isInternetOn() {
		ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
				|| connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING
				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING
				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
			return true;
		} else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
			return false;
		}
		return false;
	}

	// Не закрывать приложение на кнопку назад от родителя
	// Для API 4+
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			// Получаем статус до сворачивания
			presencesaved = AMFAROSTER.PresenceSaved;

			// Ставим статус отошел
			Presence presencekeydown = new Presence(Presence.Type.available);
			presencekeydown.setStatus("AMFA");
			presencekeydown.setPriority(24);
			presencekeydown.setMode(Presence.Mode.away);
			connection.sendPacket(presencekeydown);

			this.getParent().moveTaskToBack(true);
		}
		return super.onKeyDown(keyCode, event);
	}
}