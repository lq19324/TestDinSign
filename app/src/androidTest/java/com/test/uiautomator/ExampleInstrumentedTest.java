package com.test.uiautomator;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExampleInstrumentedTest {

    private static final String testPkg = "com.alibaba.android.rimet";
    private static final String launchActivity = testPkg + ".biz.SplashActivity";

    private Context mContext;
    private UiDevice mDevice;

    private String mAccount;
    private String mPassword;

    @Test
    public void test() {

        testA_init();

        testLoadAccount();

        testB_startApp();

        testC_login();

        testD_gotoSignUI();

        testE_sign();
    }


    private void testA_init() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        // 获取上下文
        mContext = instrumentation.getContext();
        mDevice = UiDevice.getInstance(instrumentation);
        try {
            mDevice.wakeUp();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.i("test", "<0> init success");
    }

    private void testLoadAccount() {
        try {
            File file = new File("/sdcard/.ddaccount");
            if (!file.exists()) {
                throw new FileNotFoundException("/sdcard/.ddaccount not found");
            }
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            mAccount = br.readLine();
            mPassword = br.readLine();
            Log.i("test", "<#> testLoadAccount account:" + mAccount+" pwd:"+mPassword);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testB_startApp() {
        try{
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(testPkg);
            intent.setComponent(new ComponentName(testPkg, launchActivity));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);

            Log.i("test", "<1> startApp success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testC_login() {
        // 登陆
        try {
            // com.alibaba.android.rimet:id/et_phone_input
            UiObject editTextNum = mDevice.findObject(new UiSelector().resourceId("com.alibaba.android.rimet:id/et_phone_input"));
            editTextNum.setText(mAccount);

            // com.alibaba.android.rimet:id/et_pwd_login
            UiObject editTextPwd = mDevice.findObject(new UiSelector().resourceId("com.alibaba.android.rimet:id/et_pwd_login"));
            editTextPwd.setText(mPassword);

            // com.alibaba.android.rimet:id/btn_next
            UiObject loginBut = mDevice.findObject(new UiSelector().resourceId("com.alibaba.android.rimet:id/btn_next"));
            loginBut.clickAndWaitForNewWindow(1500);

            Log.i("test", "<2> login success");
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
            waitFor(5000);
            //12-01 03:20:45.826 3009-3785/system_process I/ActivityManager: START u0 {act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10200000 cmp=com.alibaba.android.rimet/.biz.SplashActivity bnds=[1056,904][1392,1304] (has extras)} from uid 10016 on display 0
        }
    }

    private void testD_gotoSignUI() {
        boolean enterCommitUI = false;
        // 进入签到界面
        // com.alibaba.android.rimet:id/home_bottom_tab_button_work
        UiObject mz1But = mDevice.findObject(new UiSelector().resourceId("com.alibaba.android.rimet:id/home_bottom_tab_button_work"));
        try {
            mz1But.click();
            enterCommitUI = true;
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
            enterCommitUI = false;
        }

        if (!enterCommitUI) {
            return;
        }

        waitFor(1000);

        try {
            UiObject titleTv = mDevice.findObject(new UiSelector().text("签到"));
            titleTv.click();
            enterCommitUI = true;
        } catch ( UiObjectNotFoundException e) {
            e.printStackTrace();
            enterCommitUI = false;
        }

        if (!enterCommitUI) {
            Log.e("test", "gotoSignUI failed, not found '签到'");
            return;
        }
        Log.i("test", "<3> gotoSignUI success");
        waitFor(5000);
    }

    private void testE_sign() {
        // 签到
        int deviceW = mDevice.getDisplayWidth();
        int deviceH = mDevice.getDisplayHeight();
        String pkg = mDevice.getCurrentPackageName();
        assertEquals(testPkg, pkg);
        mDevice.click(deviceW / 4, deviceH - 20);

        waitFor(4000);

        pkg = mDevice.getCurrentPackageName();
        assertEquals(testPkg, pkg);
        mDevice.click(deviceW / 2, 1620);

        waitFor(4000);

        pkg = mDevice.getCurrentPackageName();
        assertEquals(testPkg, pkg);
        mDevice.click(deviceW / 2, deviceH - 20);
        Log.i("test", "<4> sign success");

        // 截屏
        Date day = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //获取系统时间
        String addTime = sdf.format(day);
        boolean result = mDevice.takeScreenshot(new File("/sdcard/"+addTime+".jpg"));
        Log.w("test", "takeScreenshot result="+result);

        waitFor(4000);

        try {
            mDevice.executeShellCommand("am force-stop " + testPkg);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w("test", "kill process failed");
        }
        Log.i("test", "<5> test finished");
    }

    private void waitFor(long time) {
        synchronized (this) {
            try {
                wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getRunningProcess(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = am.getRunningTasks(2);
        int size = runningTaskInfos.size();
        for (int i = 0; i < size; i++) {
            ActivityManager.RunningTaskInfo info = runningTaskInfos.get(i);
            String className = info.topActivity.getClassName();
            String shortClassName = info.topActivity.getShortClassName();
            String pkg = info.topActivity.getPackageName();
            Log.i("test", "getRunningProcess className:"+className+" pkg:"+pkg);
        }
    }

    private int getRunningPID (Context context, String pkg) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : am.getRunningAppProcesses()) {
            if (appProcess.processName.equals(pkg)) {
                return appProcess.pid;
            }
        }
        return -1;
    }
}
