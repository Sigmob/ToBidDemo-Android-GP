package com.windmill.android.demo.custom;

import android.content.Context;
import android.util.Log;

import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomNativeAdapter;
import com.windmill.sdk.models.BidPrice;
import com.windmill.sdk.natives.WMNativeAdData;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;


public class PangleCustomerNative extends WMCustomNativeAdapter implements PangleNativeAd.AdListener {

    private String TAG = PangleCustomerNative.this.getClass().getSimpleName();
    private PangleNativeAd ttNativeAdAdapter;

    /**
     * @param context
     * @param localExtra
     * @param serverExtra Placement:CustomInfo:{"codeId":"980088216","isExpressAd":false}
     */
    @Override
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            String placementCustomInfo = (String) serverExtra.get(WMConstants.CUSTOM_INFO);

            JSONObject object = new JSONObject(placementCustomInfo);

            String codeId = object.optString("codeId");

            boolean isExpressAd = object.optBoolean("isExpressAd");

            Log.d(TAG, "loadAd:" + codeId + ":" + isExpressAd);

            /**
             * 只支持自渲染广告
             */
            if (isExpressAd) {
                ttNativeAdAdapter = new PangleNativeUnifiedAd(this, this);
            } else {
                callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "not support expressAd"));
                return;
            }
            ttNativeAdAdapter.loadAd(context, codeId, localExtra, serverExtra);
        } catch (Throwable e) {
            callLoadFail(new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), e.getMessage()));
        }
    }

    @Override
    public void onNativeAdLoadSuccess(List<WMNativeAdData> nativeAdDataList, Object price) {
        Log.d(TAG, "onNativeAdLoadSuccess:" + price);
        if (getBiddingType() == WMConstants.AD_TYPE_CLIENT_BIDING) {
            String eCpm = "0";
            if (price != null) {
                eCpm = String.valueOf(price);
            }
            callLoadBiddingSuccess(new BidPrice(eCpm));
        }

        callLoadSuccess(nativeAdDataList);
    }

    @Override
    public void onNativeAdFailToLoad(WMAdapterError error) {
        Log.d(TAG, "onNativeAdFailToLoad:" + error.toString());
        callLoadFail(error);
    }

    @Override
    public boolean isReady() {
        if (ttNativeAdAdapter != null) {
            return ttNativeAdAdapter.isReady();
        }
        return false;
    }

    @Override
    public List<WMNativeAdData> getNativeAdDataList() {
        if (ttNativeAdAdapter != null) {
            return ttNativeAdAdapter.getNativeAdDataList();
        }
        return null;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price) {
        Log.d(TAG, "notifyBiddingResult:" + isWin + ":" + price);
        if (ttNativeAdAdapter != null) {
            if (isWin) {
                ttNativeAdAdapter.win(Double.parseDouble(price));
            } else {
                ttNativeAdAdapter.loss(Double.parseDouble(price), "102", "");
            }
        }
    }

    @Override
    public void destroyAd() {
        if (ttNativeAdAdapter != null) {
            ttNativeAdAdapter.destroy();
        }
    }

}
