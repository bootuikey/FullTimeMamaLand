package com.letv.skin.loading;

import java.util.Observable;

import com.letv.skin.BaseView;
import com.letv.universal.iplay.EventPlayProxy;
import com.letv.universal.iplay.IPlayer;
import com.letv.universal.iplay.ISplayer;
import com.letv.universal.notice.UIObserver;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

/**
 * loadingview
 * 
 * @author pys
 *
 */
public abstract class BaseLoadingView extends BaseView implements UIObserver {

    private static final String TAG = BaseLoadingView.class.getSimpleName();

    public BaseLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseLoadingView(Context context) {
        super(context);
    }

    /**
     * init view
     * 
     * @param context
     */
    protected void initView(Context context) {
        onInitView(context);
    }

    protected abstract void onInitView(Context context);

    @Override
    public void update(Observable observable, Object data) {
        Bundle bundle = (Bundle) data;
        switch (bundle.getInt("state")) {
        case IPlayer.MEDIA_EVENT_FIRST_RENDER:
            setVisibility(View.GONE);
            break;
        case IPlayer.MEDIA_EVENT_BUFFER_START:
            setVisibility(View.VISIBLE);
            break;
        case IPlayer.MEDIA_EVENT_BUFFER_END:
            setVisibility(View.GONE);
            break;
        case IPlayer.PLAYER_EVENT_PREPARE:
            setVisibility(View.VISIBLE);
            break;
        case ISplayer.PLAYER_EVENT_RATE_TYPE_CHANGE:
            setVisibility(View.VISIBLE);
            break;
        case ISplayer.MEDIA_ERROR_DECODE_ERROR:
        case ISplayer.MEDIA_ERROR_NO_STREAM:
        case ISplayer.PLAYER_PROXY_ERROR:
            setVisibility(View.GONE);
            break;
        case EventPlayProxy.PLAYER_TIME_SHIRT_SEEK:
            setVisibility(View.VISIBLE);
            break;
        case EventPlayProxy.PLAYER_TIME_SHIRT_SEEK_COMPLETE:
        case EventPlayProxy.PLAYER_TIME_SHIRT_SEEK_ERROR:
            setVisibility(View.GONE);
            break;
        case EventPlayProxy.PLAYER_PROXY_AD_START:
            setVisibility(View.GONE);
            break;
        case EventPlayProxy.PLAYER_PROXY_AD_END:
            setVisibility(View.VISIBLE);
            bringToFront();
        case ISplayer.PLAYER_EVENT_RESET_PLAY:
            setVisibility(View.VISIBLE);
            bringToFront();
            break;

        default:
            break;
        }
    }

    @Override
    protected void initPlayer() {
        player.attachObserver(this);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (player != null) {
            player.getObserver().deleteObserver(this);
        }
    }

}