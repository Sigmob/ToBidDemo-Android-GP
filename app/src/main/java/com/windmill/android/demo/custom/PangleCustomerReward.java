package com.windmill.android.demo.custom;

import android.app.Activity;
import android.util.Log;

import com.bytedance.sdk.openadsdk.api.reward.PAGRewardItem;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAd;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdLoadListener;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedRequest;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomRewardAdapter;
import com.windmill.sdk.models.BidPrice;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PangleCustomerReward extends WMCustomRewardAdapter {

    private String TAG = PangleCustomerReward.this.getClass().getSimpleName();
    private PAGRewardedAd ttRewardVideoAd;
    private boolean isReady = false;

    /**
     * @param activity
     * @param localExtra
     * @param serverExtra Placement:CustomInfo:{"codeId":"980088192"}
     */
    @Override
    public void loadAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            isReady = false;

            String placementCustomInfo = (String) serverExtra.get(WMConstants.CUSTOM_INFO);

            JSONObject object = new JSONObject(placementCustomInfo);

            String codeId = object.optString("codeId");

            Log.d(TAG, "loadAd:" + codeId);

            PAGRewardedRequest pagRewardedRequest = new PAGRewardedRequest();

            pagRewardedRequest.setExtraInfo(localExtra);

            PAGRewardedAd.loadAd(codeId, new PAGRewardedRequest(), new PAGRewardedAdLoadListener() {
                @Override
                public void onError(int code, String message) {
                    Log.d(TAG, "onError " + code + ":" + message);
                    callLoadFail(new WMAdapterError(code, message));
                }

                @Override
                public void onAdLoaded(PAGRewardedAd rewardedAd) {
                    Log.d(TAG, "onAdLoaded");
                    ttRewardVideoAd = rewardedAd;
                    isReady = true;
                    if (getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
                        String eCpm = "0";
                        Map<String, Object> mediaExtraInfo = ttRewardVideoAd.getMediaExtraInfo();
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
            if (ttRewardVideoAd != null && isReady) {
                ttRewardVideoAd.setAdInteractionListener(new PAGRewardedAdInteractionListener() {

                    @Override
                    public void onAdShowed() {
                        Log.d(TAG, "onAdShowed");
                        callVideoAdShow();
                    }

                    @Override
                    public void onAdClicked() {
                        Log.d(TAG, "onAdClicked");
                        callVideoAdClick();
                    }

                    @Override
                    public void onAdDismissed() {
                        Log.d(TAG, "onAdDismissed");
                        callVideoAdClosed();
                    }

                    @Override
                    public void onUserEarnedReward(PAGRewardItem item) {
                        Log.d(TAG, "onUserEarnedReward");
                        callVideoAdReward(true);
                    }

                    @Override
                    public void onUserEarnedRewardFail(int errorCode, String errorMsg) {
                        Log.d(TAG, "onUserEarnedRewardFail:" + errorCode + ":" + errorMsg);
                        callVideoAdReward(false);
                    }
                });
                ttRewardVideoAd.show(activity);
            } else {
                callVideoAdPlayError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(), "Please load the ad before showing it!"));
            }
            isReady = false;
        } catch (Throwable e) {
            callVideoAdPlayError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(), e.getMessage()));
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price) {
        Log.d(TAG, "notifyBiddingResult " + isWin + ":" + price);
        if (ttRewardVideoAd != null) {
            if (isWin) {
                ttRewardVideoAd.win(Double.parseDouble(price));
            } else {
                ttRewardVideoAd.loss(Double.parseDouble(price), "102", "");
            }
        }
    }

    @Override
    public boolean isReady() {
        return (ttRewardVideoAd != null && isReady);
    }

    @Override
    public void destroyAd() {
        if (ttRewardVideoAd != null) {
            ttRewardVideoAd = null;
            isReady = false;
        }
    }

}
