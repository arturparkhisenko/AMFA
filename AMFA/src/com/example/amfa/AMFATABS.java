package com.example.amfa;

import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class AMFATABS extends TabActivity {

	private TabHost tabHost;
	private TabSpec rosterspec;
	private TabSpec chatspec;
	public static Integer ctab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabs);

		// Новый хост закладок
		tabHost = getTabHost();

		// Таб Контактов
		rosterspec = tabHost.newTabSpec(getString(R.string.title_section1));
		// Тайтл и иконка
		rosterspec.setIndicator(getString(R.string.title_section1),
				getResources().getDrawable(R.drawable.icon_roster_tab));
		Intent rosterIntent = new Intent(this, AMFAROSTER.class);
		rosterspec.setContent(rosterIntent);

		// Таб Чата
		chatspec = tabHost.newTabSpec(getString(R.string.title_section2));
		// Тайтл и иконка
		chatspec.setIndicator(getString(R.string.title_section2),
				getResources().getDrawable(R.drawable.icon_chat_tab));
		Intent chatIntent = new Intent(this, AMFACHAT.class);
		chatspec.setContent(chatIntent);

		// Добавляем табы в TabHost
		tabHost.addTab(rosterspec);
		tabHost.addTab(chatspec);

		// Ициниализация вкладки чата, для прослушивания
		switchToChat();
		switchToRoster();
	}

	public void switchToChat() {
		tabHost.setCurrentTabByTag(getString(R.string.title_section2));
		tabHost.getCurrentTab();
	}

	public void switchToRoster() {
		tabHost.setCurrentTabByTag(getString(R.string.title_section1));
		tabHost.getCurrentTab();
	}

	public void getcurrenttab() {
		ctab = tabHost.getCurrentTab();
	}

	protected void onDestroy() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(1);
		super.onDestroy();
	}
}