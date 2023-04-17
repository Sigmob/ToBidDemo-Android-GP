package com.windmill.android.demo.custom;

import android.content.Context;

import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAd;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdLoadListener;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeRequest;
import com.windmill.pangle.PagNativeAdData;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.base.WMAdapterError;
import com.windmill.sdk.custom.WMCustomNativeAdapter;
import com.windmill.sdk.natives.WMNativeAdData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PangleNativeUnifiedAd extends PangleNativeAd {

    private List<WMNativeAdData> nativeAdDataList = new ArrayList<>();
    private AdListener adListener;
    private WMCustomNativeAdapter adAdapter;

    public PangleNativeUnifiedAd(WMCustomNativeAdapter adAdapter, AdListener adListener) {
        this.adAdapter = adAdapter;
        this.adListener = adListener;
    }

    @Override
    public void win(double price) {
        if (nativeAdDataList != null && nativeAdDataList.size() > 0) {
            PangleNativeAdData nativeAdData = (PangleNativeAdData) nativeAdDataList.get(0);
            PAGNativeAd ttFeedAd = nativeAdData.getPagNativeAd();
            if (ttFeedAd != null) {
                ttFeedAd.win(price);
            }
        }
    }

    @Override
    public void loss(double price, String lossReason, String winBidder) {
        if (nativeAdDataList != null && nativeAdDataList.size() > 0) {
            PangleNativeAdData nativeAdData = (PangleNativeAdData) nativeAdDataList.get(0);
            PAGNativeAd ttFeedAd = nativeAdData.getPagNativeAd();
            if (ttFeedAd != null) {
                ttFeedAd.loss(price, lossReason, winBidder);
            }
        }
    }

    @Override
    public List<WMNativeAdData> getNativeAdDataList() {
        return nativeAdDataList;
    }

    @Override
    public void loadAd(Context context, final String codeId, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            nativeAdDataList.clear();

            PAGNativeAd.loadAd(codeId, new PAGNativeRequest(), new PAGNativeAdLoadListener() {
                @Override
                public void onError(int code, String message) {
                    if (adListener != null) {
                        adListener.onNativeAdFailToLoad(new WMAdapterError(code, message));
                    }
                }

                @Override
                public void onAdLoaded(PAGNativeAd pagNativeAd) {
                    if (pagNativeAd == null || pagNativeAd.getNativeAdData() == null) {
                        if (adListener != null) {
                            WMAdapterError adAdapterError = new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), "pagNativeAd is null");
                            adListener.onNativeAdFailToLoad(adAdapterError);
                        }
                        return;
                    }

                    PagNativeAdData nativeAdData = new PagNativeAdData(pagNativeAd, adAdapter);
                    nativeAdDataList.add(nativeAdData);

                    Object price = null;
                    Map<String, Object> mediaExtraInfo = pagNativeAd.getMediaExtraInfo();
                    if (mediaExtraInfo != null) {
                        price = mediaExtraInfo.get("price");
                    }

                    if (adListener != null) {
                        adListener.onNativeAdLoadSuccess(nativeAdDataList, price);
                    }
                }
            });
        } catch (Throwable e) {
            if (adListener != null) {
                WMAdapterError windAdError = new WMAdapterError(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), e.getMessage());
                adListener.onNativeAdFailToLoad(windAdError);
            }
        }
    }

    @Override
    public boolean isReady() {
        return nativeAdDataList.size() > 0;
    }

    @Override
    public void destroy() {

    }

}
