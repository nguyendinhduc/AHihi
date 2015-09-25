package com.phongbm.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AHihiBroadcastComplete extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentStartService = new Intent();
        intentStartService.setClassName(CommonValue.PACKAGE_NAME_MAIN,
                CommonValue.PACKAGE_NAME_COMMON + ".AHihiService");
        context.startService(intentStartService);
    }

}