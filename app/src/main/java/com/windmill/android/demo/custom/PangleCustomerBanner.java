package com.windmill.android.demo.custom;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAd;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdLoadListener;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerRequest;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerSize;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomBannerAdapter;
import com.windmill.sdk.models.BidPrice;

import org.json.JSONObject;

import java.util.Map;


public class PangleCustomerBanner extends WMCustomBannerAdapter {

    private String TAG = PangleCustomerBanner.this.getClass().getSimpleName();
    private PAGBannerAd pagBannerAd;

    /**
     * @param activity
     * @param localExtra
     * @param serverExtra Placement:CustomInfo:{"codeId":"980099802","adSize":"320x50"}
     */
    @Override
    public void loadAd(final Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            String placementCustomInfo = (String) serverExtra.get(WMConstants.CUSTOM_INFO);

            JSONObject object = new JSONObject(placementCustomInfo);

            String codeId = object.optString("codeId");
            String adSize = object.optString("adSize");

            Log.d(TAG, "loadAd:" + codeId + ":" + adSize);

            PAGBannerSize bannerSize = null;
            try {
                switch (adSize) {
                    case "320x50":
                        bannerSize = PAGBannerSize.BANNER_W_320_H_50;
                        break;
                    case "300x250":
                        bannerSize = PAGBannerSize.BANNER_W_300_H_250;
                        break;
                    case "728x90":
                        bannerSize = PAGBannerSize.BANNER_W_728_H_90;
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (activity == null || bannerSize == null) {
                callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "loadAd with activity is null or adSize is error"));
                return;
            }

            //PAGBannerSize bannerSize = new PAGBannerSize(320,50);
            PAGBannerRequest bannerRequest = new PAGBannerRequest(bannerSize);
            PAGBannerAd.loadAd(codeId, bannerRequest, new PAGBannerAdLoadListener() {
                @Override
                public void onError(int code, String message) {
                    Log.d(TAG, "onError:" + code + ":" + message);
                    callLoadFail(new WMAdapterError(code, message));
                }

                @Override
                public void onAdLoaded(PAGBannerAd bannerAd) {
                    if (bannerAd == null || bannerAd.getBannerView() == null) {
                        callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "bannerAd is null"));
                        return;
                    }

                    Log.d(TAG, "onAdLoaded()");

                    pagBannerAd = bannerAd;
                    pagBannerAd.setAdInteractionListener(new PAGBannerAdInteractionListener() {
                        @Override
                        public void onAdShowed() {
                            callBannerAdShow();
                        }

                        @Override
                        public void onAdClicked() {
                            callBannerAdClick();
                        }

                        @Override
                        public void onAdDismissed() {
                            callBannerAdClosed();
                        }
                    });
                    if (getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
                        String eCpm = "0";
                        Map<String, Object> mediaExtraInfo = bannerAd.getMediaExtraInfo();
                        if (mediaExtraInfo != null) {
                            Object price = mediaExtraInfo.get("price");
                            if (price != null) {
                                eCpm = String.valueOf(price);
                            }
                        }
                        callLoadBiddingSuccess(new BidPrice(eCpm));
                    }

                    callLoadSuccess();
                }
            });
        } catch (Throwable e) {
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), e.getMessage()));
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price) {
        Log.d(TAG, "notifyBiddingResult:" + isWin + ":" + price);
        if (pagBannerAd != null) {
            if (isWin) {
                pagBannerAd.win(Double.parseDouble(price));
            } else {
                pagBannerAd.loss(Double.parseDouble(price), "102", "");
            }
        }
    }

    @Override
    public boolean isReady() {
        return (pagBannerAd != null && pagBannerAd.getBannerView() != null);
    }

    @Override
    public View getBannerView() {
        if (pagBannerAd != null) {
            return pagBannerAd.getBannerView();
        }
        return null;
    }

    @Override
    public void destroyAd() {
        if (pagBannerAd != null) {
            pagBannerAd.destroy();
            pagBannerAd = null;
        }
    }
}
