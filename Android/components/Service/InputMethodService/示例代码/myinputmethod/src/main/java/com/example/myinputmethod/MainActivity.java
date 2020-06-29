package com.example.myinputmethod;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int INPUT_TYPE_LETTER = 0x00000100;
    public static final int INPUT_TYPE_NUM = 0x00000200;
    public static final int INPUT_TYPE_SYMBOL = 0x00000300;

    private EditText et_edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_go_settings).setOnClickListener(this);
        findViewById(R.id.btn_select_keyboard).setOnClickListener(this);
        findViewById(R.id.btn_input_letter).setOnClickListener(this);
        findViewById(R.id.btn_input_num).setOnClickListener(this);
        findViewById(R.id.btn_input_symbol).setOnClickListener(this);
        et_edit = (EditText) findViewById(R.id.et_input);

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {

                } else {

                }
            }
        });
    }

    public int getStatusBarHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        wm.getDefaultDisplay().getRealSize(point);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.heightPixels;
        int height = displayMetrics.widthPixels;

        View decorView = getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility();

        boolean hasNavigationBar = checkDeviceHasNavigationBar(context);

        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return 1;
    }

    /**
     * 判断是否存在 NavigationBar
     *
     * @param context
     * @return
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go_settings:
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS);
                startActivity(intent);

                break;
            case R.id.btn_select_keyboard:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
                break;
            case R.id.btn_input_letter:
                et_edit.setInputType(INPUT_TYPE_LETTER);
                break;
            case R.id.btn_input_num:
                et_edit.setInputType(INPUT_TYPE_NUM);
                break;
            case R.id.btn_input_symbol:
                et_edit.setInputType(INPUT_TYPE_SYMBOL);
                break;
        }
    }
}
