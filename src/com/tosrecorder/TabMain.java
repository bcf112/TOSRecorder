package com.tosrecorder;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.widget.*;
import android.widget.TabHost.OnTabChangeListener;

//
// 탭을 여러개 구현하는 클래스
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
				.setIndicator("녹음",
						getResources().getDrawable(R.drawable.mic_icon))
				.setContent(
						
				// FLAG_ACTIVITY_CLEAR_TOP 플래그를 지정해놓을 경우, 전에 있던 탭이 보여주던 액티비티는 자동으로 종료시키고 새로운 액티비티를 생성
						new Intent(this, RecordingActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost
				.newTabSpec("tab2")
				.setIndicator("재생목록",
						getResources().getDrawable(R.drawable.notepad_icon))
				.setContent(
						new Intent(this, PlayListActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
	}

	
	//
	// 탭을 클릭 시 그림의 색깔은 Selector로 변환시키고, 글자 색깔 변환은 이 메소드에서 수행한다.
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