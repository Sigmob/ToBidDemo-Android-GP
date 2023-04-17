package com.windmill.android.demo.custom;

import android.app.Activity;
import android.util.Log;

import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd;
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadListener;
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomInterstitialAdapter;
import com.windmill.sdk.models.BidPrice;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PangleCustomerInterstitial extends WMCustomInterstitialAdapter {

    private String TAG = PangleCustomerInterstitial.this.getClass().getSimpleName();
    private PAGInterstitialAd mInterstitialAd;
    private boolean isReady = false;

    /**
     * @param activity
     * @param localExtra
     * @param serverExtra Placement:CustomInfo:{"codeId":"980088188"}
     */
    @Override
    public void loadAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            isReady = false;

            String placementCustomInfo = (String) serverExtra.get(WMConstants.CUSTOM_INFO);

            JSONObject object = new JSONObject(placementCustomInfo);

            String codeId = object.optString("codeId");

            Log.d(TAG, "loadAd:" + codeId);

            PAGInterstitialAd.loadAd(codeId, new PAGInterstitialRequest(), new PAGInterstitialAdLoadListener() {
                @Override
                public void onError(int code, String message) {
                    Log.d(TAG, "onError " + code + ":" + message);
                    callLoadFail(new WMAdapterError(code, message));
                }

                @Override
                public void onAdLoaded(PAGInterstitialAd interstitialAd) {
                    Log.d(TAG, "onAdLoaded");
                    mInterstitialAd = interstitialAd;
                    isReady = true;
                    if (getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
                        String eCpm = "0";
                        Map<String, Object> mediaExtraInfo = interstitialAd.getMediaExtraInfo();
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
    public void showAd(Activity activity, HashMap<String, String> localExtra, Map<String, Object> serverExtra) {
        try {
            if (mInterstitialAd != null && isReady) {
                mInterstitialAd.setAdInteractionListener(new PAGInterstitialAdInteractionListener() {
                    @Override
                    public void onAdShowed() {
                        callVideoAdShow();
                    }

                    @Override
                    public void onAdClicked() {
                        callVideoAdClick();
                    }

                    @Override
                    public void onAdDismissed() {
                        callVideoAdClosed();
                    }
                });
                mInterstitialAd.show(activity);
            } else {
                callVideoAdPlayError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(), "Please load the ad before showing it!"));
            }
            isReady = false;
        } catch (Throwable throwable) {
            callVideoAdPlayError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(), throwable.getMessage()));
        }
    }

    @Override
    public boolean isReady() {
        return (mInterstitialAd != null && isReady);
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price) {
        Log.d(TAG, "notifyBiddingResult " + isWin + ":" + price);
        if (mInterstitialAd != null) {
            if (isWin) {
                mInterstitialAd.win(Double.parseDouble(price));
            } else {
                mInterstitialAd.loss(Double.parseDouble(price), "102", "");
            }
        }
    }

    @Override
    public void destroyAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
            isReady = false;
        }
    }

}
