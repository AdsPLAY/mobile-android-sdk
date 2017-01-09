package net.adsplay.holder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import net.adsplay.common.AdData;

import net.adsplay.common.AdLogDataUtil;
import net.adsplay.common.AdPermissionChecker;
import net.adsplay.common.AdsPlayAdComponent;
import net.adsplay.common.AdsPlayCallback;
import net.adsplay.common.AsyncImageAdLoadTask;
import net.adsplay.common.DownloadImageTask;



/**
 * Created by trieu on 11/3/16.
 */

public class AdsPlayImageBannerHolder extends RelativeLayout implements AdsPlayAdComponent {

    Activity activity;
    private LinearLayout adHolder;
    private ImageView imageView;
    int width, height;
    AdsPlayCallback callback;

    public AdsPlayImageBannerHolder(Context context) {
        super(context);
        init();
    }

    public AdsPlayImageBannerHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdsPlayImageBannerHolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.holderimage, this);

        //find
        this.imageView = (ImageView) findViewById(R.id.imageBannerView);
        this.adHolder = (LinearLayout)findViewById(R.id.imageBannerHolder);
    }

    public ImageView getImageView() {
        return imageView;
    }

    void closeAdView(){
        AdsPlayImageBannerHolder.this.adHolder.setVisibility(GONE);
        AdsPlayImageBannerHolder.this.imageView.setVisibility(GONE);
    }

    void hideAdView(){
        ViewGroup.LayoutParams params = adHolder.getLayoutParams();
        this.width = params.width;
        this.height = params.height;
        params.height = 0;
        params.width = 0;
        adHolder.setLayoutParams(params);
    }

    void showAdView(AdData adData){
        ViewGroup.LayoutParams params = adHolder.getLayoutParams();
        params.height = this.height;
        params.width = this.width;
        adHolder.setLayoutParams(params);

        String metric = "impression";
        AdLogDataUtil.log(metric,adData);
    }

    @Override
    public void onMediaReady(final AdData adData) {
        if(adData != null){
            try {
                showAdView(adData);
                Log.i("AdsPlay","--------------------------------------------------------------------");
                String file =  adData.getMedia();
                Log.i("AdsPlay","-------> playAd file: "+file);

                new DownloadImageTask(this.imageView, this).execute(file);

                this.imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeAdView();
                        if(adData.getClickthroughUrl() != null){
                            AdLogDataUtil.log("click",adData);
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(adData.getClickthroughUrl()));
                            AdsPlayImageBannerHolder.this.activity.startActivity(i);
                        }
                    }
                });
            } catch (Exception ioe) {
                Log.i("AdsPlay",ioe.getMessage());
                Log.i("AdsPlay",ioe.toString());
            }
        } else {
            closeAdView();
        }
    }

    @Override
    public void loadAdData(Activity activity, int placementId, AdsPlayCallback callback){
        this.activity = activity;
        this.callback = callback;
        AdPermissionChecker.checkSystemPermissions(activity);
        hideAdView();
        new AsyncImageAdLoadTask(this).execute(placementId);
    }

    @Override
    public String getCacheDir(){
        return this.activity.getCacheDir().getPath();
    }

    public void triggerFinishingCallback(){
        this.callback.onFinished();
    }
}
