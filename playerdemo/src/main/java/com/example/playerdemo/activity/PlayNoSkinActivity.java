package com.example.playerdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.WindowManager;
import android.widget.Toast;

import com.lecloud.entity.ActionInfo;
import com.lecloud.entity.LiveInfo;
import com.lecloud.lecloudsdkdemo.R;
import com.letv.controller.PlayContext;
import com.letv.controller.PlayProxy;
import com.letv.simple.utils.PlayerFactory;
import com.letv.universal.iplay.EventPlayProxy;
import com.letv.universal.iplay.ISplayer;
import com.letv.universal.iplay.OnPlayStateListener;
import com.letv.universal.play.util.PlayerParamsHelper;
import com.letv.universal.widget.ILeVideoView;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 无皮肤版demo
 */
public class PlayNoSkinActivity extends Activity implements OnPlayStateListener {
    public final static String DATA = "data";
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    private ISplayer player;
    private String path = "";
    private PlayContext playContext;
    private ILeVideoView videoView;
    private Bundle mBundle;
    private long lastposition;

    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    // surfaceView的生命周期

    private Callback surfaceCallback = new Callback() {
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            /**
             * surfaceview 销毁的时候销毁播放器
             */
            stopAndRelease();
        }

        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            /**
             * 创建播放器
             */
            createOnePlayer(holder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (player != null) {
                /**
                 * surfaceView 宽高改变的时候，需要通知player
                 */
                PlayerParamsHelper.setViewSizeChange(player, width, height);
            }
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    // 处理activity生命周期

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_no_skin);
        loadDataFromIntent();// 加载数据
        if (mBundle != null) {
            initVideoView();// 初始化videoView
            initPlayContext();// 初始化playContext
        }
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mBundle = intent.getBundleExtra(DATA);
            if (mBundle == null) {
                Toast.makeText(this, "no data", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void initVideoView() {
        /**
         * 创建视频显示View，ILeVideoView是我们定义的基础view用于显示视频，当前如果用户希望自己的view
         */
        videoView = (ILeVideoView) findViewById(R.id.sf);
        videoView.getHolder().addCallback(surfaceCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoView != null) {
            /**
             * 当屏幕旋转的时候，videoView需要全屏居中显示, 如果用户使用自己的view显示视频（比如SurfaceView）,
             * 比较简单的方法是：对surfaceView的layourParams()进行设置。 1）竖屏转横屏的时候，可以占满全屏居中显示；
             * 2）横屏转竖屏时，需要设置layoutParams()恢复之前的显示大小
             *
             */
            videoView.setVideoLayout(-1, 0);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    /**
     * 初始化PlayContext PlayContext作为播放器的上下文而存在，既保存了播放器运行时的临时数据，也记录播放器所需要的环境变量。
     */
    private void initPlayContext() {
        playContext = new PlayContext(this);
        //当前视频渲染所使用的View，可能是Surfaceview(glSurfaceView也属于Surfaceview ),也可能是textureView
        playContext.setVideoContentView(videoView.getMysef());
    }

    /**
     * 停止和释放播放器
     */
    private void stopAndRelease() {
        if (player != null) {
            lastposition = player.getCurrentPosition();
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
    }

    /**
     * 创建一个新的播放器
     *
     * @param surface
     */
    private void createOnePlayer(Surface surface) {
        player = PlayerFactory.createOnePlayer(playContext, mBundle, this, surface);
        player.setDataSource(path);
        if (lastposition > 0 && mBundle.getInt(PlayProxy.PLAY_MODE) == EventPlayProxy.PLAYER_VOD) {
            player.seekTo(lastposition);
        }

        /**
         * 该过程是异步的，在播放器回调事件中获取到该过程的结果。 请求成功:
         * <p/>
         * ISplayer.MEDIA_EVENT_PREPARE_COMPLETE，此时调用start()方法开始播放 请求失败：
         * ISplayer.PLAYER_PROXY_ERROR://请求媒体资源信息失败
         * ISplayer.MEDIA_ERROR_NO_STREAM:// 播放器尝试连接媒体服务器失败
         */
        player.prepareAsync();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * 播放器回调
     */
    @Override
    public void videoState(int state, Bundle bundle) {
        handleADEvent(state, bundle);// 处理广告事件
        handleVideoInfoEvent(state, bundle);// 处理视频信息事件
        handlePlayerEvent(state, bundle);// 处理播放器事件
        handleLiveEvent(state, bundle);// 处理直播类事件,如果是点播，则这些事件不会回调
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * 处理视频信息类事件
     * 
     * @param state
     * @param bundle
     */
    private void handleVideoInfoEvent(int state, Bundle bundle) {
        switch (state) {
        case EventPlayProxy.PROXY_WAITING_SELECT_DEFNITION_PLAY:// 获取码率
            /**
             * 处理码率
             */
            if(playContext!=null){
                Map<Integer, String> definationsMap = playContext.getDefinationsMap();// 获取到的码率
                if(definationsMap!=null&&definationsMap.entrySet()!=null){
                    Iterator<Entry<Integer, String>> iterator = definationsMap.entrySet().iterator();
                    while (iterator!=null&&iterator.hasNext()){
                        Entry<Integer, String> next = iterator.next();
                        Integer key = next.getKey();//码率所对于的key值,key值用于切换码率时，方法playedByDefination(type)所对于的值
                        String value = next.getValue();//码率名字，比如：标清，高清，超清
                    }
                }
            }
            break;
        case EventPlayProxy.PROXY_VIDEO_INFO_REFRESH:// 获取视频信息，比如title等等
            break;
        case ISplayer.PLAYER_PROXY_ERROR:// 请求媒体资源信息失败
            int errorCode = bundle.getInt("errorCode");
            String msg = bundle.getString("errorMsg");
            break;
        }
    }

    /**
     * 处理播放器本身事件
     * 
     * @param state
     * @param bundle
     */
    private void handlePlayerEvent(int state, Bundle bundle) {
        switch (state) {

        case ISplayer.MEDIA_EVENT_VIDEO_SIZE:
            if (videoView != null && player != null) {
                /**
                 * 获取到视频的宽高的时候，此时可以通过视频的宽高计算出比例，进而设置视频view的显示大小。
                 * 如果不按照视频的比例进行显示的话，(以surfaceView为例子)内容会填充整个surfaceView。
                 * 意味着你的surfaceView显示的内容有可能是拉伸的
                 */
                videoView.onVideoSizeChange(player.getVideoWidth(), player.getVideoHeight());

                /**
                 * 获取宽高的另外一种方式
                 */
                bundle.getInt("width");
                bundle.getInt("height");
            }
            break;

        case ISplayer.MEDIA_EVENT_PREPARE_COMPLETE:// 播放器准备完成，此刻调用start()就可以进行播放了
            if(player!=null){
                player.start();
            }
            break;

        case ISplayer.MEDIA_EVENT_FIRST_RENDER:// 视频第一帧数据绘制
            break;
        case ISplayer.MEDIA_EVENT_PLAY_COMPLETE:// 视频播放完成
            break;
        case ISplayer.MEDIA_EVENT_BUFFER_START:// 开始缓冲
            break;
        case ISplayer.MEDIA_EVENT_BUFFER_END:// 缓冲结束
            break;
        case ISplayer.MEDIA_EVENT_SEEK_COMPLETE:// seek完成
            break;
        case ISplayer.MEDIA_ERROR_DECODE_ERROR:// 解码错误
            break;
        case ISplayer.MEDIA_ERROR_NO_STREAM:// 播放器尝试连接媒体服务器失败
            break;
        default:
            break;
        }
    }

    /**
     * 处理直播类事件
     */
    private void handleLiveEvent(int state, Bundle bundle) {
        switch (state) {
        case EventPlayProxy.PROXY_SET_ACTION_LIVE_CURRENT_LIVE_ID:// 获取当前活动直播的id
            String liveId = bundle.getString("liveId");
            break;
        case EventPlayProxy.PROXY_WATING_SELECT_ACTION_LIVE_PLAY:// 当收到该事件后，用户可以选择优先播放的活动直播
            ActionInfo actionInfo = playContext.getActionInfo();
            // 查找正在播放的直播 或者 可以秒转点播的直播信息
            LiveInfo liveInfo = actionInfo.getFirstCanPlayLiveInfo();
            if (liveInfo != null) {
                playContext.setLiveId(liveInfo.getLiveId());
            }
            break;
        default:
            break;
        }
    }

    /**
     * 处理广告事件
     * 
     * @param state
     * @param bundle
     */
    private void handleADEvent(int state, Bundle bundle) {
        switch (state) {
        case EventPlayProxy.PLAYER_PROXY_AD_START:// 广告开始
            break;
        case EventPlayProxy.PLAYER_PROXY_AD_END:// 广告播放结束
            break;
        case EventPlayProxy.PLAYER_PROXY_AD_POSITION:// 广告倒计时
            int position = bundle.getInt(String.valueOf(EventPlayProxy.PLAYER_PROXY_AD_POSITION));// 获取倒计时
            break;
        default:
            break;
        }
    }

}
