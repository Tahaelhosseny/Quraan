package eg.com.ma3lshnyshokrny.www.quraan;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.r3bl.samples.simplemediaplayer.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import eg.com.ma3lshnyshokrny.www.quraan.LocalEventFromMainActivity;
import eg.com.ma3lshnyshokrny.www.quraan.LocalEventFromMediaPlayerHolder;
import eg.com.ma3lshnyshokrny.www.quraan.MediaPlayerHolder;

public class MainActivity extends AppCompatActivity
{

    public static final String TAG = "MainActivity";
    public static final String MEDIA_RESOURCE_ID = "https://server11.mp3quran.net/hawashi/019.mp3";
    private MediaPlayerHolder mMediaPlayerHolder;
    private boolean isUserSeeking;
    TextView mTextDebug;
    Button mPlayButton;
    Button mPauseButton;
    Button mResetButton;
    SeekBar mSeekbarAudio;
    ScrollView mScrollContainer;

    // Activity lifecycle.

    int duration ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        EventBus.getDefault().register(this);





    }

    private void bindViews()  {
        mTextDebug = findViewById(R.id.text_debug);
        mPlayButton = findViewById(R.id.button_play);
        mPauseButton = findViewById(R.id.button_pause);
        mResetButton = findViewById(R.id.button_reset);
        mSeekbarAudio = findViewById(R.id.seekbar_audio);
        mScrollContainer = findViewById(R.id.scroll_container);

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource("https://server11.mp3quran.net/hawashi/019.mp3");
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                duration = mp.getDuration();
                mMediaPlayerHolder = new MediaPlayerHolder(getApplicationContext() , duration);
                setupSeekbar();
                mMediaPlayerHolder.load(MEDIA_RESOURCE_ID);
            }
        });
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
      //  mMediaPlayerHolder.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // Handle user input for Seekbar changes.

    public void setupSeekbar() {
        mSeekbarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // This holds the progress value for onStopTrackingTouch.
            int userSelectedPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Only fire seekTo() calls when user stops the touch event.
                if (fromUser) {
                    userSelectedPosition = progress;
                    isUserSeeking = true;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                EventBus.getDefault().post(new LocalEventFromMainActivity.SeekTo(
                        userSelectedPosition));
            }
        });
    }

    // Handle user input for button presses.

    void pause() {
        EventBus.getDefault().post(new LocalEventFromMainActivity.PausePlayback());
    }

    void play() {
        EventBus.getDefault().post(new LocalEventFromMainActivity.StartPlayback());
    }

    void reset() {
        EventBus.getDefault().post(new LocalEventFromMainActivity.ResetPlayback());
    }

    // Display log messges to the UI.

    public void log(StringBuffer formattedMessage) {
        if (mTextDebug != null) {
            mTextDebug.setText(formattedMessage);
            // Move the mScrollContainer focus to the end.
            mScrollContainer.post(new Runnable() {
                @Override
                public void run() {
                    mScrollContainer.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        } else {
            Log.d(TAG, String.format("log: %s", formattedMessage));
        }
    }

    // Event subscribers.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocalEventFromMediaPlayerHolder.UpdateLog event) {
        log(event.formattedMessage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocalEventFromMediaPlayerHolder.PlaybackDuration event) {
        mSeekbarAudio.setMax(event.duration);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocalEventFromMediaPlayerHolder.PlaybackPosition event) {
        if (!isUserSeeking) {
            mSeekbarAudio.setProgress(event.position, true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocalEventFromMediaPlayerHolder.StateChanged event) {
        Toast.makeText(this, String.format("State changed to:%s", event.currentState),
                       Toast.LENGTH_SHORT).show();
    }

}
