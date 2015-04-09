package com.example.amfa;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AMFA extends Activity {

	// 6 private - заменил на public
	public EditText mUserid;
	public EditText mPassword;
	public EditText mHost;
	public EditText mPort;
	public EditText mService;
	public SharedPreferences loginPreferences;
	public XMPPConnection connection;
	public Intent intentTabs;
	public AsyncTask<String, Void, Boolean> connectiontask;
	public Integer ConLogStatus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 0-offline, 1-connected
		ConLogStatus = 0;

		mUserid = (EditText) this.findViewById(R.id.userid);
		mPassword = (EditText) this.findViewById(R.id.password);
		mHost = (EditText) this.findViewById(R.id.host);
		mPort = (EditText) this.findViewById(R.id.port);
		mService = (EditText) this.findViewById(R.id.service);

		// Загружаем ранее введенные данные
		loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
		mUserid.setText(loginPreferences.getString("mUserid", ""));
		mHost.setText(loginPreferences.getString("mHost", ""));
		mPort.setText(loginPreferences.getString("mPort", ""));
		mService.setText(loginPreferences.getString("mService", ""));
	}

	// Кнопка входа
	public boolean enter_Click(View v) {
		if (isInternetOn()) {
			if (isAllRight()) {
				Toast.makeText(getApplicationContext(), R.string.wronginput,
						Toast.LENGTH_SHORT).show();
			} else {
				intentTabs = new Intent(AMFA.this, AMFATABS.class);
				// Сохраняем данные для передачи в другую активити
				intentTabs.putExtra("eUserid", mUserid.getText().toString());
				intentTabs
						.putExtra("ePassword", mPassword.getText().toString());
				intentTabs.putExtra("eHost", mHost.getText().toString());
				intentTabs.putExtra("ePort", mPort.getText().toString());
				intentTabs.putExtra("eService", mService.getText().toString());

				// Запоминаем введенные данные
				loginPreferences.edit()
						.putString("mUserid", mUserid.getText().toString())
						.putString("mHost", mHost.getText().toString())
						.putString("mPort", mPort.getText().toString())
						.putString("mService", mService.getText().toString())
						.commit();

				// Вызываем подключение с настройками
				Log.i("AMFA", "CreConWitSet START");
				CreConWitSet();
				Log.i("AMFA", "CreConWitSet END");
			}
			return true;
		} else {
			Toast.makeText(this, R.string.noethernet, Toast.LENGTH_SHORT)
					.show();
			return false;
		}
	}

	// Создаем подключение с настройками
	public void CreConWitSet() {

		// Уведомление о процессе подключения
		connectiontask = new AsyncTask<String, Void, Boolean>() {
			private ProgressDialog progressDialog = new ProgressDialog(
					AMFA.this);

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog.setTitle(getString(R.string.pdrostertitle));
				progressDialog.setMessage(getString(R.string.pdconnectiontext));
				progressDialog.setIndeterminate(true);
				if (!progressDialog.isShowing())
					progressDialog.show();
			}

			@Override
			protected Boolean doInBackground(String... params) {

				// Создаем подключение из полученных данных
				ConnectionConfiguration connConfig = new ConnectionConfiguration(
						mHost.getText().toString(), Integer.parseInt(mPort
								.getText().toString()), mService.getText()
								.toString());
				// Укажем в конфиге возможность переподключения
				connConfig.setReconnectionAllowed(true);

				// XMPPConnection connection =
				connection = new XMPPConnection(connConfig);

				// Сбросим значение статуса
				ConLogStatus = 0;

				try {
					connection.connect();
					Log.i("AMFA",
							"[SettingsDialog] Connected to "
									+ connection.getHost());
				} catch (final XMPPException ex) {
					Log.e("AMFA", "[SettingsDialog] Failed to connect to "
							+ connection.getHost());
					Log.e("AMFA", ex.toString());
					// setConnection(null);
					// Заглушка для подключения
					Activity aAmfa = AMFA.this;
					aAmfa.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(),
									ex.toString(), Toast.LENGTH_SHORT).show();
						}
					});

				}
				try {
					connection.login(mUserid.getText().toString(), mPassword
							.getText().toString());
					Log.i("AMFA", "Logged in as " + connection.getUser());

					// Задаем статус - доступен
					Presence presence = new Presence(Presence.Type.available);
					connection.sendPacket(presence);

					Activity aAmfa = AMFA.this;
					aAmfa.runOnUiThread(new Runnable() {
						public void run() {
							// Уведомление об успешном подключении и входе
							Toast.makeText(
									getApplicationContext(),
									getString(R.string.successfulconnect)
											+ mUserid.getText().toString(),
									Toast.LENGTH_SHORT).show();
						}
					});

					// Задаем статус подключения
					ConLogStatus = 1;

				} catch (final XMPPException ex) {
					Log.e("AMFA", "[SettingsDialog] Failed to log in as "
							+ mUserid.getText().toString());
					Log.e("AMFA", ex.toString());
					// setConnection(null);

					Activity aAmfa = AMFA.this;
					aAmfa.runOnUiThread(new Runnable() {
						public void run() {
							// Заглушка для авторизации
							Toast.makeText(getApplicationContext(),
									ex.toString(), Toast.LENGTH_SHORT).show();
						}
					});

				}
				return null;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				progressDialog.dismiss();

				// Если подключение установлено то
				if (ConLogStatus == 1) {
					// Сохраняем подключение
					AMFAXL.getInstance().setConnection(connection);
					// Запускаем табс активити
					startActivity(intentTabs);
					finish();
				} else {
					// ничего
				}

				super.onPostExecute(result);
			}
		};
		// Запускаем процесс подключения с диалогом
		connectiontask.execute();
		// Сохраняем подключение в любом случае
		AMFAXL.getInstance().setConnection(connection);
	}

	// Кнопка регистрации
	public void register_Click(View v) {
		Intent registerIntent = new Intent(this, AMFAREGISTER.class);
		startActivity(registerIntent);
	}

	// Проверка наличия введенных данных
	// TextUtils.isEmpty(mPassword) else if (mPassword.length() < 4)
	public boolean isAllRight() {
		if (mUserid.getText().toString().trim().equals("")
				|| !mUserid.getText().toString().contains("@")
				|| mPassword.getText().toString().trim().equals("")
				|| mHost.getText().toString().trim().equals("")
				|| !mHost.getText().toString().contains(".")
				|| mPort.getText().toString().trim().equals("")
				|| mService.getText().toString().trim().equals("")) {
			return true;
		} else {
			return false;
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
}