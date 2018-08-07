package com.lody.virtual.client.hook.proxies.notification;

import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.MethodInvocationProxy;
import com.lody.virtual.client.hook.base.MethodInvocationStub;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.helper.compat.BuildCompat;

import mirror.android.app.NotificationManager;
import mirror.android.widget.Toast;

/**
 * @author Lody
 * @see android.app.NotificationManager
 * @see android.widget.Toast
 */
@Inject(MethodProxies.class)
public class NotificationManagerStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

    public NotificationManagerStub() {
        //NotificationManager.getService.call() 返回值 IInterface
        //因为实现binder功能的接口，一定会实现 IInterface  ，所以泛型接口定义成IInterface 在合适不过了
        super(new MethodInvocationStub<IInterface>(NotificationManager.getService.call()));
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("enqueueToast"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("cancelToast"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("removeAutomaticZenRules"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getImportance"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("areNotificationsEnabled"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("setNotificationPolicy"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationPolicy"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("isNotificationPolicyAccessGrantedForPackage"));
        }
        if ("samsung".equalsIgnoreCase(Build.BRAND) || "samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("removeEdgeNotification"));
        }

        if (BuildCompat.isOreo()) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("createNotificationChannelGroups"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationChannelGroups"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("deleteNotificationChannelGroup"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("createNotificationChannels"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationChannels"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationChannel"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("deleteNotificationChannel"));
        }

    }

    @Override
    public void inject() throws Throwable {
        //反射替换掉某个对象，在这里是反射替换 sService 成员变量
        NotificationManager.sService.set(getInvocationStub().getProxyInterface());
        Toast.sService.set(getInvocationStub().getProxyInterface());
    }

    @Override
    public boolean isEnvBad() {
        return NotificationManager.getService.call() != getInvocationStub().getProxyInterface();
    }
}
