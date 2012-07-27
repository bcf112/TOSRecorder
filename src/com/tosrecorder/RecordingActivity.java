package com.tosrecorder;

import java.io.*;
import java.text.*;
import java.util.*;

import android.app.*;
import android.content.Intent;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

// Client ID : 2f2cZ0QT1381df82293

public class RecordingActivity extends Activity {
	TextView mPrepareTime;
	ProgressBar mPrepareProgress;
	TextView mResponseTime;
	ProgressBar mResponseProgress;
	boolean prepareStatus = true;

	SimpleDateFormat date;
	SimpleDateFormat time;

	String Path = "";
	MediaRecorder mRecorder = null;
	int responseSecond;
	int prepareSecond;

	ImageView mRecordButton;
	Spinner mPartSpinner;

	String partname = "[P1]";
	SoundPool mSoundPool;
	int loudBeep;

	boolean isRecordBtnClicked = false;

	ArrayList<PartInfo> partSpinnerList;

	ProgressDialog mProgressDialog;
	String sd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recording);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("잠시만 기다려주십시오...");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPrepareTime = (TextView) findViewById(R.id.recording_prepare_time);
		mPrepareProgress = (ProgressBar) findViewById(R.id.recording_prepare_progress);
		mResponseTime = (TextView) findViewById(R.id.recording_response_time);
		mResponseProgress = (ProgressBar) findViewById(R.id.recording_response_progress);
		mRecordButton = (ImageView) findViewById(R.id.recording_record);
		mPartSpinner = (Spinner) findViewById(R.id.recording_spinner);

		sd = Environment.getExternalStorageDirectory().getAbsolutePath();
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		loudBeep = mSoundPool.load(this, R.raw.beep, 1);

		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath(), "TOSRecorder");
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// 파일 명을 저장하기 위한 현재 날짜를 미리 세팅
		date = new SimpleDateFormat("[yyyy-MM-dd] ");
		time = new SimpleDateFormat("hh시mm분ss초");

		// 레코더 생성
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
		} else {
			mRecorder.reset();
		}
		setRecorderConfig(mRecorder);

		partSpinnerList = new ArrayList<PartInfo>();
		createInitPartData();
		PartSpinnerAdapter adapter = new PartSpinnerAdapter(this,
				partSpinnerList);
		mPartSpinner.setAdapter(adapter);

		mRecordButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isRecordBtnClicked) {
					Date today = new Date();
					String songTitle = partname + date.format(today)
							+ time.format(today) + ".amr";
					Path = sd + "/TOSRecorder/" + songTitle;

					mRecorder.reset();
					setRecorderConfig(mRecorder);

					mPartSpinner.setClickable(false);
					mRecordButton.setImageResource(R.drawable.stop_button);

					mTimerHandler.sendEmptyMessageDelayed(0, 1000);

					isRecordBtnClicked = true;
				} else {
					if (!prepareStatus) {
						mRecorder.stop();
						sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
								Uri.parse("file://"
										+ Environment
												.getExternalStorageDirectory())));
					}
					mTimerHandler.removeMessages(0);

					Toast.makeText(RecordingActivity.this, "녹음이 취소되었습니다",
							Toast.LENGTH_SHORT).show();

					prepareStatus = true;
					setTimer(mPartSpinner.getSelectedItemPosition());

					mPartSpinner.setClickable(true);
					mRecordButton.setImageResource(R.drawable.record_button);
					isRecordBtnClicked = false;
				}
			}
		});

		mPartSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onNothingSelected(AdapterView<?> arg0) {

			}

			public void onItemSelected(AdapterView<?> adapter, View view,
					int position, long id) {
				Toast.makeText(RecordingActivity.this,
						partSpinnerList.get(position).partTitle + "를 선택하셨습니다.",
						Toast.LENGTH_SHORT).show();

				setTimer(position);
			}
		});
	}

	protected void onDestroy() {
		super.onDestroy();
		mTimerHandler.removeMessages(0);
		mSoundPool.release();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	void setRecorderConfig(MediaRecorder recorder) {
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	}

	/**
	 * Spinner에 초기 데이터를 설정하는 메소드
	 */
	public void createInitPartData() {
		PartInfo info = new PartInfo();
		info.partTitle = "Question 1,2";
		info.partImage = getResources().getDrawable(R.drawable.icon_part1);
		partSpinnerList.add(info);

		info = new PartInfo();
		info.partTitle = "Question 3";
		info.partImage = getResources().getDrawable(R.drawable.icon_part2);
		partSpinnerList.add(info);

		info = new PartInfo();
		info.partTitle = "Question 4,5";
		info.partImage = getResources().getDrawable(R.drawable.icon_part3);
		partSpinnerList.add(info);

		info = new PartInfo();
		info.partTitle = "Question 6";
		info.partImage = getResources().getDrawable(R.drawable.icon_part3);
		partSpinnerList.add(info);

		info = new PartInfo();
		info.partTitle = "Question 7,8";
		info.partImage = getResources().getDrawable(R.drawable.icon_part4);
		partSpinnerList.add(info);

		info = new PartInfo();
		info.partTitle = "Question 9";
		info.partImage = getResources().getDrawable(R.drawable.icon_part4);
		partSpinnerList.add(info);

		info = new PartInfo();
		info.partTitle = "Question 10";
		info.partImage = getResources().getDrawable(R.drawable.icon_part5);
		partSpinnerList.add(info);

		info = new PartInfo();
		info.partTitle = "Question 11";
		info.partImage = getResources().getDrawable(R.drawable.icon_part6);
		partSpinnerList.add(info);
	}

	/**
	 * 목록을 선택하면 전달받은 값으로 타이머에 설정해준다.
	 */
	public void setTimer(int selectedPart) {
		int prepareSec = 0;
		int responseSec = 0;

		switch (selectedPart) {
		case 0:
			partname = "[P1]";
			prepareSec = 45;
			responseSec = 45;
			break;
		case 1:
			partname = "[P2]";
			prepareSec = 30;
			responseSec = 45;
			break;
		case 2:
			partname = "[P3]";
			prepareSec = 0;
			responseSec = 15;
			break;
		case 3:
			partname = "[P3]";
			prepareSec = 0;
			responseSec = 30;
			break;
		case 4:
			partname = "[P4]";
			prepareSec = 0;
			responseSec = 15;
			break;
		case 5:
			partname = "[P4]";
			prepareSec = 0;
			responseSec = 30;
			break;
		case 6:
			partname = "[P5]";
			prepareSec = 30;
			responseSec = 60;
			break;
		case 7:
			partname = "[P6]";
			prepareSec = 15;
			responseSec = 60;
			break;
		}

		mPrepareTime.setText(convertSecToMil(prepareSec));
		mPrepareProgress.setMax(prepareSec);
		mPrepareProgress.setProgress(prepareSec);
		mResponseTime.setText(convertSecToMil(responseSec));
		mResponseProgress.setMax(responseSec);
		mResponseProgress.setProgress(responseSec);

		responseSecond = responseSec;
		prepareSecond = prepareSec;
	}

	public String convertSecToMil(int sec) {
		long ell = sec * 1000;
		String sEll = String.format("%02d:%02d", (ell) / 1000 / 60,
				(ell / 1000) % 60);

		return sEll;
	}

	/**
	 * Handler로부터 호출되어 1초가 흐를 때마다 타이머에도 1초를 빼고 표시한다.
	 */
	public void startTimer() {
		if (prepareStatus) {
			if (prepareSecond != 0) {
				prepareSecond = prepareSecond - 1;
				mPrepareProgress.setProgress(prepareSecond);
				mPrepareTime.setText(convertSecToMil(prepareSecond));
			} else {
				mRecorder.setOutputFile(Path);
				try {
					mRecorder.prepare();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				mPartSpinner.setClickable(false);

				mRecorder.start();
				mSoundPool.play(loudBeep, 1, 1, 0, 0, 1);

				prepareStatus = false;
				mTimerHandler.sendEmptyMessageDelayed(0, 2000);

				return;
			}
			mTimerHandler.sendEmptyMessageDelayed(0, 1000);
		} else {
			responseSecond = responseSecond - 1;
			mResponseProgress.setProgress(responseSecond);
			mResponseTime.setText(convertSecToMil(responseSecond));

			if (responseSecond == 0) {
				mTimerHandler.removeMessages(0);

				mRecorder.stop();

				mPartSpinner.setClickable(true);
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory())));

				Toast.makeText(RecordingActivity.this, "시간이 종료되었습니다",
						Toast.LENGTH_SHORT).show();

				mRecordButton.setImageResource(R.drawable.record_button);

				prepareStatus = true;
				isRecordBtnClicked = false;
				setTimer(mPartSpinner.getSelectedItemPosition());
				return;
			}
			mTimerHandler.sendEmptyMessageDelayed(0, 1000);
		}
	}

	/**
	 * 1초마다 초가 감소하는 부분을 처리하는 핸들러
	 */
	Handler mTimerHandler = new Handler() {
		public void handleMessage(Message msg) {
			startTimer();
		}
	};
}
