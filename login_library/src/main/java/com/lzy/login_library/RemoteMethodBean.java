package com.lzy.login_library;

import android.support.annotation.MainThread;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by lizhiyun on 2017/4/15.
 * 用于非页面跳转的拦截，记录ui线程拦截的方法和唤醒拦截时被阻塞的子线程
 */

public class RemoteMethodBean {
    private Object target;
    private Method method;
    private Object[] objects;
    private static volatile RemoteMethodBean instance;
    private OnDestroyListener onDestroyListener;

    public OnDestroyListener getOnDestroyListener() {
        return onDestroyListener;
    }

    private RemoteMethodBean() {

    }

    public static RemoteMethodBean getInstance() {
        if (instance == null) {
            synchronized (RemoteMethodBean.class) {
                if (instance == null) {
                    instance = new RemoteMethodBean();
                }
            }
        }
        return instance;
    }


    @MainThread
    public void setMessage(Object target, Method method, Object[] objects) {
        this.target = target;
        this.method = method;
        this.objects = objects;
        this.onDestroyListener = new OnDestroyListener() {
            @Override
            public void onDestroy() {
                setNull();
            }
        };
    }

    @MainThread
    public void setNull() {
        this.target = null;
        this.method = null;
        this.objects = null;
        this.onDestroyListener = null;
    }

    public void doMethod() {
        if (target != null) {
            try {
                method.setAccessible(true);
                method.invoke(target, objects);
                Log.e("test","RemoteMethodBean invoke");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            setNull();
        }

        synchronized (LoginUtil.sObjectLogin) {
            LoginUtil.sObjectLogin.notifyAll();
        }
    }
}
