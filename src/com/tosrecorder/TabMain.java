package com.tosrecorder;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.widget.*;
import android.widget.TabHost.OnTabChangeListener;

//
// ���� ������ �����ϴ� Ŭ����
//
public class TabMain extends TabActivity implements OnTabChangeListener {
	TabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(this);

		tabHost.addTab(tabHost
				.newTabSpec("tab1")
				.setIndicator("����",
						getResources().getDrawable(R.drawable.mic_icon))
				.setContent(
						
				// FLAG_ACTIVITY_CLEAR_TOP �÷��׸� �����س��� ���, ���� �ִ� ���� �����ִ� ��Ƽ��Ƽ�� �ڵ����� �����Ű�� ���ο� ��Ƽ��Ƽ�� ����
						new Intent(this, RecordingActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost
				.newTabSpec("tab2")
				.setIndicator("������",
						getResources().getDrawable(R.drawable.notepad_icon))
				.setContent(
						new Intent(this, PlayListActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
	}

	
	//
	// ���� Ŭ�� �� �׸��� ������ Selector�� ��ȯ��Ű��, ���� ���� ��ȯ�� �� �޼ҵ忡�� �����Ѵ�.
	//
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			TextView clsTextView2 = (TextView) tabHost.getTabWidget()
					.getChildAt(i).findViewById(android.R.id.title);
			clsTextView2.setTextColor(Color.WHITE);
		}
		TextView clsTextView = (TextView) tabHost.getTabWidget()
				.getChildAt(tabHost.getCurrentTab())
				.findViewById(android.R.id.title);
		clsTextView.setTextColor(Color.BLACK);
	}
}