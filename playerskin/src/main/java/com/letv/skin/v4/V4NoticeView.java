package com.letv.skin.v4;

import java.util.Observable;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.lecloud.leutils.ReUtils;
import com.letv.skin.BaseView;
import com.letv.skin.interfacev1.OnNetWorkChangeListener;
import com.letv.skin.utils.NetworkUtils;
import com.letv.universal.iplay.EventPlayProxy;
import com.letv.universal.iplay.ISplayer;
import com.letv.universal.notice.UIObserver;

/**
 * 提示view
 * 
 * @author pys
 *
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class V4NoticeView extends BaseView implements UIObserver, OnNetWorkChangeListener {
    private static final String TAG = "V4NoticeView";
    private TextView codeView;
    private TextView msgView;
    private TextView noteView;

    public V4NoticeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public V4NoticeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public V4NoticeView(Context context) {
        super(context);
    }

    @Override
    protected void initPlayer() {
        player.attachObserver(this);
    }

    @Override
    protected void initView(Context context) {
        LayoutInflater.from(context).inflate(ReUtils.getLayoutId(context, "letv_skin_v4_notice_layout"), this);
        codeView = (TextView) findViewById(ReUtils.getId(context, "tv_error_code"));
        msgView = (TextView) findViewById(ReUtils.getId(context, "tv_error_msg"));
        noteView = (TextView) findViewById(ReUtils.getId(context, "tv_error_message"));
        msgView.setVisibility(View.GONE);
        findViewById(ReUtils.getId(context, "btn_error_replay")).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    setVisibility(View.GONE);
                    player.resetPlay();
                }
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        if (NetworkUtils.hasConnect(context)) {
            Bundle bundle = (Bundle) data;
            int state = bundle.getInt("state");
            Log.d(TAG, "[errorView] state:" + state);
            switch (state) {
            case ISplayer.MEDIA_ERROR_DECODE_ERROR:
                setVisibility(View.VISIBLE);
                showMsg(state, "播放器解码失败");
                setNoteView();
            case ISplayer.MEDIA_ERROR_NO_STREAM:
                setVisibility(View.VISIBLE);
                showMsg(state, "流媒体连接失败");
                setNoteView();
                break;
            case ISplayer.MEDIA_EVENT_FIRST_RENDER:
                setNoteView();
                setVisibility(View.GONE);
                break;
            case ISplayer.PLAYER_PROXY_ERROR:
                setNoteView();
                setVisibility(View.VISIBLE);
                int errorCode = bundle.getInt("errorCode");
                String msg = bundle.getString("errorMsg");
                showMsg(errorCode, msg);
                break;
            case EventPlayProxy.PLAYER_TIME_SHIRT_SEEK_ERROR:
                showMsg(state, "时移失败");
                setVisibility(View.VISIBLE);
                break;
            default:
                break;
            }
        } else {
            showNetworkNotice();
        }
    }

    private void setNoteView() {
        noteView.setText(ReUtils.getStringId(context, "letv_notice_message"));
    }

    /**
     * 展示消息
     * 
     * @param event
     * @param msg
     */
    private void showMsg(int event, String msg) {
        String errorNote = "错误代码:";
        codeView.setText(errorNote + event);

        if (msg != null && !msg.isEmpty()) {
            msgView.setText("(" + msg + ")");
            msgView.setVisibility(View.VISIBLE);
        } else {
            msgView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        switch (visibility) {
        case View.GONE:
        case View.INVISIBLE:
            if (uiPlayContext != null) {
                uiPlayContext.setNotiveViewShowing(false);
            }
            break;
        case View.VISIBLE:
            if (uiPlayContext != null) {
                uiPlayContext.setNotiveViewShowing(true);
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void onNetWorkChange(boolean state, String message) {
        if (state) {
            // setVisibility(View.GONE);
        } else {
            showNetworkNotice();
        }
    }

    private void showNetworkNotice() {
        // 解决网络访问提示
        showMsg(10000, "网络访问失败");
        noteView.setText(ReUtils.getStringId(context, "letv_network_message"));
        setVisibility(View.VISIBLE);
    }
}
