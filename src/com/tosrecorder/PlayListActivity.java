package com.tosrecorder;

import java.util.*;
import java.io.*;

import net.daum.adam.publisher.*;
import net.daum.adam.publisher.AdView.*;
import net.daum.adam.publisher.impl.*;

import android.app.*;
import android.content.*;
import android.database.*;
import android.media.*;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.text.format.DateFormat;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayListActivity extends Activity {
	public static final int DIALOG_DELETE_CHECK = 1001;

	boolean wasPlaying;
	int selectedPosition;
	SeekBar mPlaySeekbar;

	Cursor mCursor;
	ArrayList<RecordingInfo> mRecordingList;
	MediaPlayer mPlayer;
	ListView mPlayList;

	TextView remainTime;
	TextView elapseTime;
	ImageView mPlayBtn;
	ImageView partImg;

	ProgressDialog mProgressDialog;
	PlayListAdapter mPlayListAdapter;

	private AdView adView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);

		initAdam();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mRecordingList = new ArrayList<RecordingInfo>();
		mPlayListAdapter = new PlayListAdapter(this, mRecordingList);
		mPlayer = new MediaPlayer();

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("��ø� ��ٷ��ֽʽÿ�...");

		mPlayBtn = (ImageView) findViewById(R.id.playlist_playcontrol);
		remainTime = (TextView) findViewById(R.id.playlist_remaintime);
		elapseTime = (TextView) findViewById(R.id.playlist_elapsetime);
		mPlayList = (ListView) findViewById(R.id.playlist_recording_list);
		mPlaySeekbar = (SeekBar) findViewById(R.id.playlist_seekbar);

		mPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(MediaPlayer mp) {
				if (wasPlaying) {
					mp.start();
				}
			}
		});

		mPlaySeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				if (mPlayer.isPlaying())
					mPlayer.pause();
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					mPlayer.seekTo(progress);
					mPlayer.start();
				}
			}
		});

		mPlayList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long id) {
				mPlayer.reset();
				LoadMedia(position);
				playerPrepare();
				mPlaySeekbar.setMax(mPlayer.getDuration());
				remainTime.setText(convertMilsToMS(mPlayer.getDuration()));
				mPlayer.start();
				mProgressHandler.sendEmptyMessage(0);
				mPlayBtn.setImageResource(R.drawable.seekbar_pause);
			}
		});

		mPlayList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View v,
					int position, long id) {
				selectedPosition = position;
				showDialog(DIALOG_DELETE_CHECK);
				return true;
			}
		});

		mPlayBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mPlayer.isPlaying()) {
					mPlayer.start();
					mPlayBtn.setImageResource(R.drawable.seekbar_pause);
					mProgressHandler.sendEmptyMessage(0);
				} else {
					mPlayer.pause();
					mPlayBtn.setImageResource(R.drawable.seekbar_play);
					mProgressHandler.removeMessages(0);
				}
			}
		});

		mPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mPlayBtn.setImageResource(R.drawable.seekbar_play);
			}
		});

		mPlayList.setAdapter(mPlayListAdapter);
		mPlayList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setRegisterReceiver();

		updateSongList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}

		if (adView != null) {
			adView.destroy();
			adView = null;
		}

		mProgressHandler.removeMessages(0);
		unregisterReceiver(mScanReceiver);
	}

	private void initAdam() {
		adView = (AdView) findViewById(R.id.adview);

		adView.setOnAdClickedListener(new OnAdClickedListener() {

			@Override
			public void OnAdClicked() {
				Log.d("ADAM", "���� Ŭ���߽��ϴ�.");
			}
		});

		adView.setOnAdFailedListener(new OnAdFailedListener() {

			@Override
			public void OnAdFailed(AdError arg0, String arg1) {
				Log.d("ADAM", "���� �����ޱ⿡ �����߽��ϴ�.");
			}
		});

		adView.setOnAdLoadedListener(new OnAdLoadedListener() {

			@Override
			public void OnAdLoaded() {
				Log.d("ADAM", "���� ���������� �ε��Ǿ����ϴ�.");
			}
		});

		adView.setOnAdWillLoadListener(new OnAdWillLoadListener() {

			@Override
			public void OnAdWillLoad(String url) {
				Log.d("ADAM", "���� �ҷ����ϴ�." + url);
			}
		});

		adView.setAdCache(true);
		adView.setAnimationType(AnimationType.FLIP_HORIZONTAL);
		adView.setVisibility(View.VISIBLE);
	}

	void setRegisterReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addDataScheme("file");
		registerReceiver(mScanReceiver, intentFilter);
	}

	BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mRecordingList.clear();
			updateSongList();
		}
	};

	/**
	 * ���� ������ �̵�� �÷��̾ ����
	 */
	boolean LoadMedia(int idx) {
		try {
			mPlayer.setDataSource(mRecordingList.get(idx).filePath);
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IllegalStateException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		if (playerPrepare() == false) {
			return false;
		}
		return true;
	}

	/**
	 * �̵�� �÷��̾ �غ��Ų��
	 */
	boolean playerPrepare() {
		try {
			mPlayer.prepare();
		} catch (IllegalStateException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * ���� ������ �ִ� �������� �о����
	 */
	public void updateSongList() {
		new LoadRecordingTask().execute();
	}

	/**
	 * �и��ʸ� �а� �ʷ� ����ϴ� �޼ҵ�
	 */
	public String convertMilsToMS(long milseconds) {
		return (String) DateFormat.format("mm:ss", milseconds);
	}

	/**
	 * 0.2�ʿ� �� ���÷� ��� ��ġ ����
	 */
	Handler mProgressHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mPlayer.isPlaying()) {
				mPlaySeekbar.setProgress(mPlayer.getCurrentPosition());
				elapseTime
						.setText(convertMilsToMS(mPlayer.getCurrentPosition()));
				mProgressHandler.sendEmptyMessageDelayed(0, 100);
			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		switch (id) {
		case DIALOG_DELETE_CHECK:

			builder = new AlertDialog.Builder(PlayListActivity.this);

			builder.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							new File(
									mRecordingList.get(selectedPosition).filePath)
									.delete();
							sendBroadcast(new Intent(
									Intent.ACTION_MEDIA_MOUNTED,
									Uri.parse("file://"
											+ Environment
													.getExternalStorageDirectory())));
						}
					});

			builder.setNegativeButton(android.R.string.no, null);
			builder.setIcon(R.drawable.launcher_icon);
			builder.setTitle("���");
			builder.setMessage("���õ� �׸��� �����Ͻðڽ��ϱ�?");

			alertDialog = builder.create();
			return alertDialog;
		}

		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}

	class LoadRecordingTask extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			mRecordingList.clear();

			String[] mCursorCols = new String[] { MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.DATA };

			mCursor = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
					"album='TOSRecorder'", null, null);

			if(mCursor==null){
				return null;
			}
			
			if (!(mCursor.getCount() == 0)) {
				mCursor.moveToFirst();

				for (int i = 0; i < mCursor.getCount(); i++) {
					RecordingInfo info = new RecordingInfo();
					info.title = mCursor.getString(0);
					String convertedDuration = mCursor.getString(1);
					info.duration = convertMilsToMS(Long
							.parseLong(convertedDuration));
					info.filePath = mCursor.getString(2);
					mRecordingList.add(info);
					mCursor.moveToNext();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mPlayListAdapter.notifyDataSetChanged();
			mProgressDialog.hide();
		}
	}
}