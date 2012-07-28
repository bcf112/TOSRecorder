package com.tosrecorder;

import java.util.*;
import java.io.*;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import net.daum.adam.publisher.*;
import net.daum.adam.publisher.AdView.*;
import net.daum.adam.publisher.impl.*;

import android.app.*;
import android.content.*;
import android.media.*;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.*;
import android.text.format.DateFormat;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayListActivity extends SherlockActivity {
	public static final int DIALOG_DELETE_CHECK = 1001;

	boolean wasPlaying;
	int selectedPosition;
	SeekBar mPlaySeekbar;

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
		mProgressDialog.setMessage("잠시만 기다려주십시오...");

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_record:
			Intent intent = new Intent(getApplicationContext(),
					RecordingActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;

		case R.id.item_list:
			break;

		default:
			return false;
		}
		return true;
	}

	private void initAdam() {
		adView = (AdView) findViewById(R.id.adview);

		adView.setOnAdClickedListener(new OnAdClickedListener() {

			@Override
			public void OnAdClicked() {
				Log.d("ADAM", "광고를 클릭했습니다.");
			}
		});

		adView.setOnAdFailedListener(new OnAdFailedListener() {

			@Override
			public void OnAdFailed(AdError arg0, String arg1) {
				Log.d("ADAM", "광고 내려받기에 실패했습니다.");
			}
		});

		adView.setOnAdLoadedListener(new OnAdLoadedListener() {

			@Override
			public void OnAdLoaded() {
				Log.d("ADAM", "광고가 정상적으로 로딩되었습니다.");
			}
		});

		adView.setOnAdWillLoadListener(new OnAdWillLoadListener() {

			@Override
			public void OnAdWillLoad(String url) {
				Log.d("ADAM", "광고를 불러봅니다." + url);
			}
		});

		adView.setAdCache(true);
		adView.setAnimationType(AnimationType.FLIP_HORIZONTAL);
		adView.setVisibility(View.VISIBLE);
	}

	/**
	 * 녹음 파일을 미디어 플레이어에 지정
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
	 * 미디어 플레이어를 준비시킨다
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
	 * 현재 폴더에 있는 녹음파일 읽어오기
	 */
	public void updateSongList() {
		new LoadRecordingTask().execute();
	}

	/**
	 * 밀리초를 분과 초로 계산하는 메소드
	 */
	public String convertMilsToMS(long milseconds) {
		return (String) DateFormat.format("mm:ss", milseconds);
	}

	/**
	 * 0.2초에 한 번꼴로 재생 위치 갱신
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
							mRecordingList.clear();
							updateSongList();
						}
					});

			builder.setNegativeButton(android.R.string.no, null);
			builder.setIcon(R.drawable.launcher_icon);
			builder.setTitle("경고");
			builder.setMessage("선택된 항목을 삭제하시겠습니까?");

			alertDialog = builder.create();
			return alertDialog;
		}
		return null;
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

			File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath(), "TOSRecorder");
			
			if (dir.listFiles(new AMRFilter()).length > 0) {
				for (File file : dir.listFiles(new AMRFilter())) {
					MediaMetadataRetriever mmr = new MediaMetadataRetriever();
					mmr.setDataSource(file.getPath());

					RecordingInfo info = new RecordingInfo();
					info.title = file.getName().replace(".amr", "");
					info.duration = convertMilsToMS(Long
							.valueOf(mmr
									.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
					info.filePath = file.getPath();
					mRecordingList.add(info);
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
	
	class AMRFilter implements FilenameFilter{

		@Override
		public boolean accept(File dir, String filename) {
			return filename.endsWith(".amr");
		}
		
	}
}