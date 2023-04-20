package com.windmill.android.demo.custom;

import android.content.Context;
import android.util.Log;

import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.api.init.PAGConfig;
import com.bytedance.sdk.openadsdk.api.init.PAGSdk;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillConsentStatus;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.custom.WMCustomAdapterProxy;

import org.json.JSONObject;

import java.util.Map;

public class PangleCustomerProxy extends WMCustomAdapterProxy {

    private String TAG = PangleCustomerProxy.this.getClass().getSimpleName();

    /**
     * @param context
     * @param serverExtra APP:CustomInfo:{"appId":"8025677"}
     */
    @Override
    public void initializeADN(final Context context, Map<String, Object> serverExtra) {
        try {
            String appCustomInfo = (String) serverExtra.get(WMConstants.CUSTOM_INFO);

            JSONObject object = new JSONObject(appCustomInfo);

            String appId = object.optString("appId");

            Log.d(TAG, "initializeADN:" + appId);

            WindMillConsentStatus userGDPRConsentStatus = WindMillAd.sharedAds().getUserGDPRConsentStatus();

            boolean adult = WindMillAd.sharedAds().isAdult();

            PAGConfig config = new PAGConfig.Builder()
                    .appId(appId)
                    .useTextureView(true) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                    .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                    .debugLog(true)
                    .setChildDirected(adult ? 0 : 1)
                    .setDoNotSell(1)
                    .setGDPRConsent(userGDPRConsentStatus == WindMillConsentStatus.ACCEPT ? 0 : 1)
                    .build();

            //强烈建议在应用对应的Application#onCreate()方法中调用，避免出现content为null的异常
            PAGSdk.init(context, config, new PAGSdk.PAGInitCallback() {
                @Override
                public void success() {
                    Log.d(TAG, "success");
//                        callInitSuccess();
                }

                @Override
                public void fail(int code, String msg) {
                    Log.d(TAG, "fail:" + code + ":" + msg);
                    callInitFail(code, msg);
                }
            });

            callInitSuccess();
        } catch (Throwable e) {
            e.printStackTrace();
            callInitFail(WindMillError.ERROR_AD_ADAPTER_LOAD.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public String getNetworkSdkVersion() {
        return PAGSdk.getSDKVersion();
    }

    @Override
    public int baseOnToBidCustomAdapterVersion() {
        return WMConstants.TO_BID_CUSTOM_ADAPTER_VERSION_1;
    }

    @Override
    public void notifyPrivacyStatusChange() {
        try {
            Log.d(TAG, "notifyPrivacyStatusChange");

            WindMillConsentStatus userGDPRConsentStatus = WindMillAd.sharedAds().getUserGDPRConsentStatus();

            boolean adult = WindMillAd.sharedAds().isAdult();

            PAGConfig.setChildDirected(adult ? 0 : 1);//Set the configuration of COPPA

            PAGConfig.setGDPRConsent(userGDPRConsentStatus == WindMillConsentStatus.ACCEPT ? 0 : 1);//Set the configuration of GDPR

            PAGConfig.setDoNotSell(1);//Set the configuration of CCPA
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
