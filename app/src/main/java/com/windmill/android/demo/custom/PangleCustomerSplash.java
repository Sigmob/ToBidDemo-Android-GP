package com.windmill.android.demo.custom;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel;
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAd;
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAdInteractionCallback;
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAdLoadListener;
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenRequest;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomSplashAdapter;
import com.windmill.sdk.models.BidPrice;

import org.json.JSONObject;

import java.util.Map;

public class PangleCustomerSplash extends WMCustomSplashAdapter {

    private String TAG = PangleCustomerSplash.this.getClass().getSimpleName();
    private PAGAppOpenAd mSplashAD;
    private boolean isReady = false;

    /**
     * @param activity
     * @param viewGroup
     * @param localExtra
     * @param serverExtra Placement:CustomInfo:{"codeId":"890000078","timeOut":"5"}
     */
    @Override
    public void loadAd(Activity activity, ViewGroup viewGroup, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            isReady = false;

            if (activity == null) {
                callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "activity is null"));
            } else {
                String placementCustomInfo = (String) serverExtra.get(WMConstants.CUSTOM_INFO);

                JSONObject object = new JSONObject(placementCustomInfo);

                String codeId = object.optString("codeId");

                String timeOut = object.optString("timeOut");

                int show = 5;
                if (!TextUtils.isEmpty(timeOut)) {
                    show = Integer.parseInt(timeOut);
                }

                Log.d(TAG, "loadAd " + codeId + ":" + show);

                PAGAppOpenRequest request = new PAGAppOpenRequest();
                request.setTimeout(show * 1000);//App Open ad timeout recommended >=3000ms

                PAGAppOpenAd.loadAd(codeId, request, new PAGAppOpenAdLoadListener() {
                    @Override
                    public void onError(int code, String message) {
                        Log.d(TAG, "onError:" + code + ":" + message);
                        callLoadFail(new WMAdapterError(code, message));
                    }

                    @Override
                    public void onAdLoaded(PAGAppOpenAd appOpenAd) {
                        Log.d(TAG, "onAdLoaded");
                        if (appOpenAd == null) {
                            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "appOpenAd is null"));
                            return;
                        }
                        mSplashAD = appOpenAd;
                        isReady = true;

                        if (getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
                            String eCpm = "0";
                            Map<String, Object> mediaExtraInfo = appOpenAd.getMediaExtraInfo();
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
            }
        } catch (Exception e) {
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), e.getMessage()));
        }
    }

    @Override
    public void showAd(Activity activity, ViewGroup viewGroup, Map<String, Object> serverExtra) {
        try {
            if (mSplashAD != null && isReady) {
                mSplashAD.setAdInteractionListener(new PAGAppOpenAdInteractionCallback() {
                    @Override
                    public void onAdShowed() {
                        callSplashAdShow();
                    }

                    @Override
                    public void onAdClicked() {
                        callSplashAdClick();
                    }

                    @Override
                    public void onAdDismissed() {
                        callSplashAdClosed();
                    }

                    @Override
                    public void onAdShowFailed(PAGErrorModel pagErrorModel) {
                        callLoadFail(new WMAdapterError(pagErrorModel.getErrorCode(), pagErrorModel.getErrorMessage()));
                    }
                });
                mSplashAD.show(activity);
            } else {
                callSplashAdShowError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(), "mSplashAD is null"));
            }
            isReady = false;
        } catch (Exception e) {
            callSplashAdShowError(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_PLAY.getErrorCode(), e.getMessage()));
        }
    }

    @Override
    public boolean isReady() {
        return isReady && mSplashAD != null;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price) {
        Log.d(TAG, "notifyBiddingResult " + isWin + ":" + price);
        if (mSplashAD != null) {
            if (isWin) {
                mSplashAD.win(Double.parseDouble(price));
            } else {
                mSplashAD.loss(Double.parseDouble(price), "102", "");
            }
        }
    }

    @Override
    public void destroyAd() {
        if (mSplashAD != null) {
            mSplashAD = null;
            isReady = false;
        }
    }

}
