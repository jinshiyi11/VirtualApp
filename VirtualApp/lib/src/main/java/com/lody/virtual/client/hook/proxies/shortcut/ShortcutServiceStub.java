package com.lody.virtual.client.hook.proxies.shortcut;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutInfo.Builder;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.text.TextUtils;
import android.util.ArraySet;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.helper.utils.BitmapUtils;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mirror.android.content.pm.IShortcutService.Stub;
import mirror.android.content.pm.ParceledListSlice;

public class ShortcutServiceStub extends BinderInvocationProxy {

    public ShortcutServiceStub() {
        super(Stub.TYPE, "shortcut");
    }

    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getManifestShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("setDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("addDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("createShortcutResultIntent"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("disableShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("enableShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getRemainingCallCount"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getRateLimitResetTime"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getIconMaxDimensions"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getMaxShortcutCountPerActivity"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("reportShortcutUsed"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("onApplicationActive"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("hasShortcutHostPermission"));
        addMethodProxy(new RequestPinShortcut());
        addMethodProxy(new GetPinnedShortcuts());
        addMethodProxy(new ReplaceCallingPkgMethodProxy("updateShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("removeAllDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("removeDynamicShortcuts"));
    }


    @TargetApi(25)
    private static class GetPinnedShortcuts extends ReplaceCallingPkgMethodProxy {
        public GetPinnedShortcuts() {
            super("getPinnedShortcuts");
        }

        public Object call(Object who, Method method, Object... args) throws Throwable {
            Object parceledListSlice = super.call(who, method, args);
            if (parceledListSlice == null) {
                return null;
            }
            ArrayList result = new ArrayList();
            if (!VASettings.ENABLE_INNER_SHORTCUT) {
                return ParceledListSliceCompat.create(result);
            }
            List<ShortcutInfo> list = (List<ShortcutInfo>) ParceledListSlice.getList.call(parceledListSlice, new Object[0]);
            if (list != null) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    ShortcutInfo obj = list.get(i);
                    if (obj != null && (obj instanceof ShortcutInfo)) {
                        ShortcutInfo target = ShortcutServiceStub.unWrapper(VClientImpl.get().getCurrentApplication(), obj, MethodProxy.getAppPkg(), MethodProxy.getAppUserId());
                        if (target != null) {
                            result.add(target);
                        }
                    }
                }
            }
            return ParceledListSliceCompat.create(result);
        }
    }

    //https://www.jianshu.com/p/c3b862279e38
    @TargetApi(25)
    private static class RequestPinShortcut extends ReplaceCallingPkgMethodProxy {
        public RequestPinShortcut() {
            super("requestPinShortcut");
        }

        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (!VASettings.ENABLE_INNER_SHORTCUT) {
                return Boolean.FALSE;
            }
            ShortcutInfo shortcutInfo = (ShortcutInfo) args[1];
            if (shortcutInfo == null) {
                return Boolean.FALSE;
            }
            args[1] = ShortcutServiceStub.wrapper(VClientImpl.get().getCurrentApplication(), shortcutInfo, MethodProxy.getAppPkg(), MethodProxy.getAppUserId());
            return super.call(who, method, args);
        }
    }


    @TargetApi(25)
    private static ShortcutInfo wrapper(Context appContext, ShortcutInfo shortcutInfo, String pkg, int userId) {
        Bitmap bmp;
        Icon icon = Reflect.on(shortcutInfo).opt("mIcon");
        if (icon != null) {
            bmp = BitmapUtils.drawableToBitmap(icon.loadDrawable(appContext));
        } else {
            bmp = BitmapUtils.drawableToBitmap(appContext.getApplicationInfo().loadIcon(VirtualCore.get().getPackageManager()));
        }
        Intent proxyIntent = VirtualCore.get().wrapperShortcutIntent(shortcutInfo.getIntent(), null, pkg, userId);
        proxyIntent.putExtra("_VA_|categories", setToString(shortcutInfo.getCategories()));
        proxyIntent.putExtra("_VA_|activity", shortcutInfo.getActivity());
        Builder builder = new Builder(VirtualCore.get().getContext(), pkg + "@" + userId + "/" + shortcutInfo.getId());
        if (shortcutInfo.getLongLabel() != null) {
            builder.setLongLabel(shortcutInfo.getLongLabel());
        }
        if (shortcutInfo.getShortLabel() != null) {
            builder.setShortLabel(shortcutInfo.getShortLabel());
        }
        builder.setIcon(Icon.createWithBitmap(bmp));
        builder.setIntent(proxyIntent);
        return builder.build();
    }

    @TargetApi(25)
    private static ShortcutInfo unWrapper(Context appContext, ShortcutInfo shortcutInfo, String _pkg, int _userId) throws URISyntaxException {
        Intent intent = shortcutInfo.getIntent();
        if (intent == null) {
            return null;
        }
        String pkg = intent.getStringExtra("_VA_|_pkg_");
        int userId = intent.getIntExtra("_VA_|_user_id_", 0);
        if (!TextUtils.equals(pkg, _pkg) || userId != _userId) {
            return null;
        }
        String _id = shortcutInfo.getId();
        String id = _id.substring(_id.indexOf("/") + 1);
        Icon icon = Reflect.on(shortcutInfo).opt("mIcon");
        String uri = intent.getStringExtra("_VA_|_uri_");
        Intent targetIntent = null;
        if (!TextUtils.isEmpty(uri)) {
            targetIntent = Intent.parseUri(uri, 0);
        }
        ComponentName componentName = intent.getParcelableExtra("_VA_|activity");
        String categories = intent.getStringExtra("_VA_|categories");
        Builder builder = new Builder(appContext, id);
        if (icon != null) {
            builder.setIcon(icon);
        }
        if (shortcutInfo.getLongLabel() != null) {
            builder.setLongLabel(shortcutInfo.getLongLabel());
        }
        if (shortcutInfo.getShortLabel() != null) {
            builder.setShortLabel(shortcutInfo.getShortLabel());
        }
        if (componentName != null) {
            builder.setActivity(componentName);
        }
        if (targetIntent != null) {
            builder.setIntent(targetIntent);
        }
        Set<String> cs = toSet(categories);
        if (cs != null) {
            builder.setCategories(cs);
        }
        return builder.build();
    }

    private static <T> String setToString(Set<T> sets) {
        if (sets == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (T append : sets) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(",");
            }
            stringBuilder.append(append);
        }
        return stringBuilder.toString();
    }

    @TargetApi(23)
    private static Set<String> toSet(String allStr) {
        if (allStr == null) {
            return null;
        }
        String[] strs = allStr.split(",");
        Set<String> sets = new ArraySet<>();
        for (String str : strs) {
            sets.add(str);
        }
        return sets;
    }
}