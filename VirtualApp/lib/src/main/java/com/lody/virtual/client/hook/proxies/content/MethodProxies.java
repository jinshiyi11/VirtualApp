package com.lody.virtual.client.hook.proxies.content;

import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.accounts.VContentService;

import java.lang.reflect.Method;

/**
 * Author: liuqiang
 * Date: 2018-12-05
 * Time: 16:50
 * Description:  https://www.androidos.net.cn/android/8.0.0_r4/xref/frameworks/base/services/core/java/com/android/server/content/ContentService.java
 *
 */
public class MethodProxies {

    static class RegisterContentObserver extends MethodProxy {

        @Override
        public String getMethodName() {
            return "registerContentObserver";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            Log.d("Q_M","call registerContentObserver");
            //降低 targetSdkVersion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && args.length >= 5) {
                args[4] = Build.VERSION_CODES.LOLLIPOP_MR1;
            }
            return super.call(who, method, args);
        }
    }
}
