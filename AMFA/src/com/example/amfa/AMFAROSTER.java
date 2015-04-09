package com.example.amfa;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class AMFAROSTER extends Activity {

	public XMPPConnection connection;
	public String jid;
	public String password;
	public String host;
	public String port;
	public String service;
	public ListView rosterlist;
	public ArrayList<String> subrosterlist;
	public ArrayAdapter<String> rosteradapter;
	public AsyncTask<String, Void, Boolean> rostertask;
	public static String outterjid;
	public String deljid;
	public Spinner spinner;
	public Integer PresenceSave;
	public static Integer PresenceSaved;
	public EditText ARIET;
	public Roster roster;
	final int dialog_delete_item = 1;
	final int dialog_exit_menu = 2;
	public ImageLoader imageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roster);

		// Загрузчик изображений, для очистки кеша на выходе из
		imageLoader = new ImageLoader(getApplicationContext());

		// Используем общее подключение
		connection = AMFAXL.getInstance().getConnection();

		// Очищаем переменные
		outterjid = null;
		deljid = null;

		// Задаем онлайн
		PresenceSave = 1;

		// Заглушка для API 11+
		// В будущем заменить на AsyncTask + ProgressDialog
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		// Получаем данные из другого активити
		// добавил getParent(). т.к. табс родитель
		jid = getParent().getIntent().getExtras().getString("eUserid");
		password = getParent().getIntent().getExtras().getString("ePassword");
		host = getParent().getIntent().getExtras().getString("eHost");
		port = getParent().getIntent().getExtras().getString("ePort");
		service = getParent().getIntent().getExtras().getString("eService");

		// Кнопка опции
		Button setup = (Button) this.findViewById(R.id.setup);
		setup.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View view) {
				// Показываем диалог с подтверждением о выходе в меню
				showDialog(dialog_exit_menu);
			}
		});

		// Список статусов
		String[] statusarray = { getString(R.string.s_availabletochat),
				getString(R.string.s_online), getString(R.string.s_busy),
				getString(R.string.s_away), getString(R.string.s_unavailable),
				getString(R.string.s_offline) };

		// Адаптер для списка статусов
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, statusarray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner = (Spinner) findViewById(R.id.ChangeStatusSpinner);
		spinner.setAdapter(adapter);
		// Заголовок
		spinner.setPrompt(getString(R.string.presencelabel));
		// Выделяем элемент
		spinner.setSelection(1);
		// Задаем обработчик выбора
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				// Меняем статус на выбранный
				// Toast.makeText(getBaseContext(), "Position = " + position,
				// Toast.LENGTH_SHORT).show();
				Presence presenceswitch = null;

				switch (position) {
				case 0: {
					presenceswitch = new Presence(Presence.Type.available);
					// Задаем статус сообщение
					// presenceswitch.setStatus("AMFA");
					// Задаем высший приоритет
					// presenceswitch.setPriority(24);
					// Задаем режим готовый болтать и тд.
					presenceswitch.setMode(Presence.Mode.chat);
					break;
				}
				case 1: {
					presenceswitch = new Presence(Presence.Type.available);
					presenceswitch.setMode(Presence.Mode.available);
					break;
				}
				case 2: {
					presenceswitch = new Presence(Presence.Type.available);
					presenceswitch.setMode(Presence.Mode.dnd);
					break;
				}
				case 3: {
					presenceswitch = new Presence(Presence.Type.available);
					presenceswitch.setMode(Presence.Mode.away);
					break;
				}
				case 4: {
					presenceswitch = new Presence(Presence.Type.available);
					presenceswitch.setMode(Presence.Mode.xa);
					break;
				}
				case 5: {
					presenceswitch = new Presence(Presence.Type.unavailable);
					break;
				}
				}

				// Сообщаем серверу свой статус
				connection.sendPacket(presenceswitch);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
		// outState.putInt("count", cnt); }
		// protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// super.onRestoreInstanceState(savedInstanceState); //cnt =
		// savedInstanceState.getInt("count"); }

		// if(savedInstanceState == null) { // Initialize here.
		// CreConWitSet(); }

		// AddRosterItemEditText
		ARIET = (EditText) this.findViewById(R.id.addcontact);

		// Кнопка добавить контакт
		Button addrosteritem = (Button) this
				.findViewById(R.id.addcontactbutton);
		addrosteritem.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					if (ARIET.getText().toString().trim().equals("")
							|| !ARIET.getText().toString().contains("@")) {
						// Уведомление о пустом / неверном поле
						Toast.makeText(getApplicationContext(),
								getString(R.string.nocontactadd),
								Toast.LENGTH_LONG).show();
					} else {
						// Добавляем контакт
						roster.createEntry(ARIET.getText().toString(), null,
								null);
						ARIET.setText(null);
						// Контакт добавлен
						Toast.makeText(getApplicationContext(),
								getString(R.string.contactaddsuccesfull),
								Toast.LENGTH_LONG).show();
						// Обновляем список контактов
						loadRosterList();
					}
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
		});

		// Список контактов, лист и адаптер
		rosterlist = (ListView) findViewById(R.id.RosterList);
		subrosterlist = new ArrayList<String>();
		rosteradapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, subrosterlist);
		rosterlist.setAdapter(rosteradapter);
		this.rosteradapter.setNotifyOnChange(true);

		// Вызываем подключение с настройками
		// Log.i("AMFA", "CreConWitSet START");
		// CreConWitSet();
		// Log.i("AMFA", "CreConWitSet END");

		// Запускаем фоновую подгрузку контактов
		loadRosterList();

		// Задаем клик-действие адаптеру
		rosterlist.setOnItemClickListener(rosterClickedHandler);
		// Задаем длительное клик-действие адаптеру
		rosterlist.setOnItemLongClickListener(rosterItemDelete);

	}

	// Загружаем лист контактов
	public void loadRosterList() {
		rostertask = new AsyncTask<String, Void, Boolean>() {
			private ProgressDialog progressDialog = new ProgressDialog(
					AMFAROSTER.this);

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog.setTitle(getString(R.string.pdrostertitle));
				progressDialog.setMessage(getString(R.string.pdrostertext));
				progressDialog.setIndeterminate(true);
				if (!progressDialog.isShowing())
					progressDialog.show();
				subrosterlist.clear();
			}

			@Override
			protected Boolean doInBackground(String... params) {

				roster = connection.getRoster();
				Collection<RosterEntry> entries = roster.getEntries();
				for (RosterEntry entry : entries) {
					// Presence presence = roster.getPresence(entry.getUser());
					// System.out.println(entry.getUser());
					// System.out.println(presence.getType().name());
					// System.out.println(presence.getStatus());
					String rostername = entry.getUser();
					subrosterlist.add(rostername);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				progressDialog.dismiss();
				super.onPostExecute(result);
				rosteradapter.notifyDataSetChanged();
			}
		};
		rostertask.execute();
		this.rosteradapter.notifyDataSetChanged();
		Log.i("AMFA", "RosterListLoad END");
	}

	// Обработчик нажатий на контакты
	private OnItemClickListener rosterClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// По нажатию на контакт - начинаем чат с ним
			// Заносим имя контакта в строку
			outterjid = subrosterlist.get(position);
			// Переключаем вкладку, а в ней считываем имя
			switchTabInActivity();
		}
	};

	// Обработчик удаления контакта
	private OnItemLongClickListener rosterItemDelete = new OnItemLongClickListener() {
		@SuppressWarnings("deprecation")
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// Сохраняем имя нажатого контакта
			deljid = subrosterlist.get(position);
			// Показать диалог удаления
			showDialog(dialog_delete_item);
			return true;
		}
	};

	// Диалоги удаления контакта и выхода в настройки
	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id) {
		if (id == dialog_delete_item) {
			AlertDialog.Builder dialog_delete_item = new AlertDialog.Builder(
					this);
			dialog_delete_item.setTitle(getString(R.string.app_name));
			dialog_delete_item.setMessage(getString(R.string.addeleteitem)
					+ deljid + "?");
			dialog_delete_item.setPositiveButton(getString(R.string.adyes),
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {
							// Удаляем выбранный контакт из адаптера
							rosteradapter.remove(deljid);
							// Удаляем контакт на сервере
							try {
								connection.getRoster().removeEntry(
										roster.getEntry(deljid));
							} catch (XMPPException e) {
								e.printStackTrace();
							}
						}
					});
			dialog_delete_item.setNegativeButton(getString(R.string.adno),
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {
							// Ничего
						}
					});
			dialog_delete_item.setCancelable(true);
			dialog_delete_item.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					// Ничего
				}
			});

			return dialog_delete_item.create();
		}

		if (id == dialog_exit_menu) {
			AlertDialog.Builder dialog_exit_menu = new AlertDialog.Builder(this);
			dialog_exit_menu.setTitle(getString(R.string.app_name));
			dialog_exit_menu.setMessage(getString(R.string.adexitmenu));
			dialog_exit_menu.setPositiveButton(getString(R.string.adyes),
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {
							// Следующий коммент реализован onDestroy - tabs
							// Убираем уведомление о сообщении
							// NotificationManager notificationManager =
							// (NotificationManager)
							// getSystemService(Context.NOTIFICATION_SERVICE);
							// notificationManager.cancel(1);

							// Завершаем текущее подключение
							CloseCon();
							// Очищаем кеш изображений
							imageLoader.clearCache();
							// Запускаем страницу настроек
							Intent intent = new Intent(AMFAROSTER.this,
									AMFA.class);
							startActivity(intent);
							finish();
						}
					});
			dialog_exit_menu.setNegativeButton(getString(R.string.adno),
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {
							// Ничего
						}
					});
			dialog_exit_menu.setCancelable(true);
			dialog_exit_menu.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					// Ничего
				}
			});

			return dialog_exit_menu.create();
		}

		return super.onCreateDialog(id);
	}

	// Переключаем вкладку
	public void switchTabInActivity() {
		AMFATABS amfatabshandler;
		amfatabshandler = (AMFATABS) this.getParent();
		amfatabshandler.switchToChat();
	}

	// Закрываем подключение
	public void CloseCon() {
		if (connection.isConnected()) {
			Presence pres = new Presence(Presence.Type.unavailable);
			connection.sendPacket(pres);
			connection.disconnect();
		}
	}

	// При переключении или сворачивании
	protected void onPause() {
		super.onPause();
		// Запоминаем статус до паузы
		PresenceSave = spinner.getSelectedItemPosition();
		PresenceSaved = spinner.getSelectedItemPosition();
	}

	// При восстановлении
	@Override
	protected void onResume() {
		super.onResume();

		// Проверяем соединение и если оно есть то
		if (isInternetOn()) {

			// Обновляем вью контактов
			rosteradapter.notifyDataSetChanged();
			// Ставим статус из спиннера
			spinner.setSelection(PresenceSave);
			// Задаем сохраненный статус подключению
			Presence presenceresume = null;
			switch (PresenceSave) {
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
			Intent intent = new Intent(AMFAROSTER.this, AMFA.class);
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

			// Запоминаем статус до сворачивания
			PresenceSave = spinner.getSelectedItemPosition();
			PresenceSaved = spinner.getSelectedItemPosition();

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