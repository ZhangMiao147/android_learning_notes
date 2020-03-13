# Android 中 Home 键的监听

　　采用广播的方式来监听到 Home 键。

## 1. 广播接收者

```java
    //自定义的广播接收者
    private HomeKeyBroadcastReceiver mHomeKeyReceiver = null;
	//广播接收者
    private class HomeKeyBroadcastReceiver extends BroadcastReceiver {
   		private final String INNER_TAG = "HomeKeyBroadcastReceiver";
        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        //action内的某些reason
        private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";//home键旁边的最近程序列表键
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";//按下home键
		//接收到广播
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) { 
                    // 短按Home键
                    Log.i(INNER_TAG, "点击Home键");
                        
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                    Log.i(INNER_TAG, "点击最近任务按键");
                }
            }
        }
    }
```

## 2. 注册广播

```java
    //注册广播接收者，监听Home键
    private void registerHomeKeyReceiver() {
        Log.i(TAG, "registerHomeKeyReceiver");
        mHomeKeyReceiver = new HomeKeyBroadcastReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyReceiver, homeFilter);
    }
```

## 3. 取消注册

```java
    //取消监听广播接收者
    private void unregisterHomeKeyReceiver() {
        Log.i(TAG, "unregisterHomeKeyReceiver");
        if (null != mHomeKeyReceiver) {
            unregisterReceiver(mHomeKeyReceiver);
        }
    }
```

