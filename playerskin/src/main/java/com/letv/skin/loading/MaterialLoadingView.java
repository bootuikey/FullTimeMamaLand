package com.letv.skin.loading;

import com.lecloud.leutils.ReUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

public class MaterialLoadingView extends BaseLoadingView {

    public MaterialLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaterialLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialLoadingView(Context context) {
        super(context);
    }

    @Override
    protected void onInitView(Context context) {
      LayoutInflater.from(context).inflate(ReUtils.getLayoutId(context, "letv_skin_bonus_loading_layout"), this);
    }

}
