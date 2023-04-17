package com.windmill.android.demo.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bytedance.sdk.openadsdk.api.nativeAd.PAGImageItem;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAd;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdData;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGVideoAdListener;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGVideoMediaView;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMLogUtil;
import com.windmill.sdk.custom.WMCustomNativeAdapter;
import com.windmill.sdk.natives.WMNativeAdContainer;
import com.windmill.sdk.natives.WMNativeAdData;
import com.windmill.sdk.natives.WMNativeAdDataType;
import com.windmill.sdk.natives.WMNativeAdRender;

import java.util.List;

public class PangleNativeAdData implements WMNativeAdData {

    private PAGNativeAd pagNativeAd;
    private PAGNativeAdData adData;
    private NativeAdInteractionListener nativeAdListener;
    private WMCustomNativeAdapter adAdapter;
    private NativeADMediaListener nativeADMediaListener;
    private DislikeInteractionCallback dislikeInteractionCallback;
    private WMNativeAdData nativeAdData = this;

    public PangleNativeAdData(PAGNativeAd ttFeedAd, WMCustomNativeAdapter adAdapter) {
        this.pagNativeAd = ttFeedAd;
        this.adData = pagNativeAd.getNativeAdData();
        this.adAdapter = adAdapter;
    }

    @Override
    public int getInteractionType() {
        return WMConstants.INTERACTION_TYPE_UNKNOWN;
    }

    @Override
    public View getExpressAdView() {
        return null;
    }

    @Override
    public void render() {

    }

    @Override
    public boolean isExpressAd() {
        return false;
    }

    @Override
    public boolean isNativeDrawAd() {
        return false;
    }

    @Override
    public String getCTAText() {
        if (adData != null) {
            return adData.getButtonText();
        }
        return "查看详情";
    }

    public PAGNativeAd getPagNativeAd() {
        return pagNativeAd;
    }

    @Override
    public String getTitle() {
        if (adData != null) {
            return adData.getTitle();
        }
        return "";
    }

    @Override
    public String getDesc() {
        if (adData != null) {
            return adData.getDescription();
        }
        return "";
    }

    @Override
    public Bitmap getAdLogo() {
        if (adData != null) {
            View adLogoView = adData.getAdLogoView();
            return convertViewToBitmap(adLogoView);
        }
        return null;
    }

    private Bitmap convertViewToBitmap(View view) {
        Bitmap bitmap = null;
        try {
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

            view.buildDrawingCache();

            bitmap = view.getDrawingCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public String getIconUrl() {
        if (adData != null) {
            PAGImageItem icon = adData.getIcon();
            if (icon != null) {
                return icon.getImageUrl();
            }
        }
        return "";
    }

    @Override
    public View getAdChoice() {
        if (adData != null) {
            return adData.getAdChoicesView();
        }
        return null;
    }

    @Override
    public int getNetworkId() {
        if (adAdapter != null) {
            return adAdapter.getChannelId();
        }
        return 0;
    }

    @Override
    public int getAdPatternType() {//pangle只有视频类型:视频分为图片和视频
        if (adData != null) {
            PAGNativeAdData.PAGNativeMediaType mediaType = adData.getMediaType();
            if (mediaType == PAGNativeAdData.PAGNativeMediaType.PAGNativeMediaTypeImage || mediaType == PAGNativeAdData.PAGNativeMediaType.PAGNativeMediaTypeVideo) {
                return WMNativeAdDataType.NATIVE_VIDEO_AD;
            } else {
                return WMNativeAdDataType.NATIVE_UNKNOWN;
            }
        }
        return WMNativeAdDataType.NATIVE_UNKNOWN;
    }

    @Override
    public void connectAdToView(Activity activity, WMNativeAdContainer adContainer, WMNativeAdRender adRender) {
        if (adRender != null) {
            View view = adRender.createView(activity, getAdPatternType());
            adRender.renderAdView(view, PangleNativeAdData.this);
            if (adContainer != null) {
                adContainer.removeAllViews();
                adContainer.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    @Override
    public void bindViewForInteraction(Context context, View view, List<View> clickableViews, List<View> creativeViewList, View disLikeView) {
        if (pagNativeAd != null) {
            pagNativeAd.registerViewForInteraction((ViewGroup) view, clickableViews, creativeViewList, disLikeView, new PAGNativeAdInteractionListener() {
                @Override
                public void onAdShowed() {
                    if (nativeAdListener != null && adAdapter != null) {
                        nativeAdListener.onADExposed(adAdapter.getAdInFo());
                    }
                    if (adAdapter != null) {
                        adAdapter.callNativeAdShow(nativeAdData);
                    }
                }

                @Override
                public void onAdClicked() {
                    if (nativeAdListener != null && adAdapter != null) {
                        nativeAdListener.onADClicked(adAdapter.getAdInFo());
                    }
                    if (adAdapter != null) {
                        adAdapter.callNativeAdClick(nativeAdData);
                    }
                }

                @Override
                public void onAdDismissed() {
                    if (dislikeInteractionCallback != null) {
                        dislikeInteractionCallback.onSelected(0, "pangle", true);
                    }
                }
            });
        }
    }

    @Override
    public void bindImageViews(Context context, List<ImageView> imageViews, int imageRes) {

    }

    @Override
    public void setInteractionListener(NativeAdInteractionListener adInteractionListener) {
        if (adInteractionListener != null) {
            this.nativeAdListener = adInteractionListener;
        }
    }

    @Override
    public void bindMediaView(Context context, final ViewGroup mediaLayout) {
        if (adData != null) {
            //视频广告设置播放状态回调（可选）
            if (mediaLayout != null) {
                View video = adData.getMediaView();
                if (video instanceof PAGVideoMediaView) {
                    ((PAGVideoMediaView) video).setVideoAdListener(new PAGVideoAdListener() {
                        @Override
                        public void onVideoAdPlay() {
                            WMLogUtil.d(WMLogUtil.TAG, "onVideoAdStartPlay()");
                            if (nativeADMediaListener != null) {
                                nativeADMediaListener.onVideoStart();
                            }
                        }

                        @Override
                        public void onVideoAdPaused() {
                            WMLogUtil.d(WMLogUtil.TAG, "onVideoAdPaused()");
                            if (nativeADMediaListener != null) {
                                nativeADMediaListener.onVideoPause();
                            }
                        }

                        @Override
                        public void onVideoAdComplete() {
                            WMLogUtil.d(WMLogUtil.TAG, "onVideoAdComplete()");
                            if (nativeADMediaListener != null) {
                                nativeADMediaListener.onVideoCompleted();
                            }
                        }

                        @Override
                        public void onVideoError() {
                            WMLogUtil.d(WMLogUtil.TAG, "onVideoError()");
                            if (nativeADMediaListener != null) {
                                WindMillError windMillError = WindMillError.ERROR_AD_PLAY;
                                nativeADMediaListener.onVideoError(windMillError);
                            }
                        }
                    });
                }
                if (video != null) {
                    if (video.getParent() == null) {
                        mediaLayout.removeAllViews();
                        mediaLayout.addView(video);
                    }
                }
            }
        }
    }

    @Override
    public void setMediaListener(NativeADMediaListener nativeADMediaListener) {
        if (nativeADMediaListener != null) {
            this.nativeADMediaListener = nativeADMediaListener;
        }
    }

    @Override
    public void setDislikeInteractionCallback(Activity activity, final DislikeInteractionCallback dislikeInteractionCallback) {
        //如果初始化ttAdManager.createAdNative(getApplicationContext())没有传入activity 则需要在此传activity，否则影响使用Dislike逻辑
        if (dislikeInteractionCallback != null) {
            this.dislikeInteractionCallback = dislikeInteractionCallback;
        }
    }

    @Override
    public void setDownloadListener(final AppDownloadListener appDownloadListener) {

    }

    @Override
    public void startVideo() {

    }

    @Override
    public void pauseVideo() {

    }

    @Override
    public void resumeVideo() {

    }

    @Override
    public void stopVideo() {

    }

    @Override
    public void destroy() {

    }
}
