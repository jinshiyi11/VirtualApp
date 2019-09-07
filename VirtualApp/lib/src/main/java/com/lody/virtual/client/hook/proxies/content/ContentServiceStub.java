package com.lody.virtual.client.hook.proxies.content;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.BinderInvocationStub;
import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.LogInvocation;

import mirror.android.content.ContentResolver;
import mirror.android.content.IContentService;
import mirror.android.content.IContentService.Stub;
import com.lody.virtual.client.ipc.ServiceManagerNative;
/**
 * @author Lody
 *
 * @see IContentService
 */
@Inject(MethodProxies.class)
@LogInvocation(LogInvocation.Condition.ALWAYS)
public class ContentServiceStub extends BinderInvocationProxy {

    public ContentServiceStub() {
        super(Stub.asInterface, "content");
    }

    public void inject() throws Throwable {
        super.inject();
        ContentResolver.sContentService.set(((BinderInvocationStub) getInvocationStub()).getProxyInterface());
    }
}