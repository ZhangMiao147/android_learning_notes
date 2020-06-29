package com.example.myinputmethod;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * 数字键盘
 */
public class NumKeyboardView extends KeyboardView {

    // 点击键盘时，按下的位置坐标
    private float mDownX;
    private float mDownY;
    /**
     * 数字键盘 / 字母键盘 / 符号键盘
     */
    private Keyboard mKeyboardNum, mKeyboardLetter, mKeyboardSymbol;

    public NumKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initKeyboardView(context);
    }

    public NumKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initKeyboardView(context);
    }

    private void initKeyboardView(Context context) {
        mKeyboardLetter = new Keyboard(context, R.xml.qwerty);
        mKeyboardNum = new Keyboard(context, R.xml.digit);
        mKeyboardSymbol = new Keyboard(context, R.xml.symbol);

        // 默认显示字母键盘
        setKeyboard(mKeyboardNum);
        setOnKeyboardActionListener(new MyOnKeyboardActionListener());
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            int code = key.codes[0];
            if (code == Keyboard.KEYCODE_SHIFT) {   // ABC
                drawKeyBackground(R.drawable.keyboard_shift, canvas, key);
            } else if (code == DemoKeyCode.CODE_TYPE_SWITCH_INPUT) { // 切换输入法
                drawKeyBackground(R.drawable.keyboard_switch, canvas, key);
            } else if (code == DemoKeyCode.KEYCODE_DELETE) {  // 删除
                drawKeyBackground(R.drawable.keyboard_delete, canvas, key);
            } else if (code == Keyboard.KEYCODE_DONE) {  //完成 return
                drawKeyBackground(R.drawable.keyboard_enter, canvas, key);
            } else if (code == DemoKeyCode.CODE_TYPE_SYMBOL
                    || code == DemoKeyCode.CODE_TYPE_QWERTY
                    || code == DemoKeyCode.CODE_TYPE_NUM) {//符号 数组 abc
                drawKeyBackground(R.drawable.keyboard_gray, canvas, key);
                drawText(canvas, key);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        float x = me.getX();
        float y = me.getY();
        switch (me.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                setPreviewEnabled(false);
                // 滑动距离小于 10 dp 时不隐藏键盘预览，小于 10dp 时隐藏键盘按键预览
                if (Math.abs(x - mDownX) > MyUtill.dp2px(10) || Math.abs(y - mDownY) > MyUtill.dp2px(10)) {
                    // 取消预览
                    setPopupOffset(0, MyUtill.dp2px(0));
                }
                break;
        }
        return super.onTouchEvent(me);
    }

    /**
     * 绘制按钮背景
     *
     * @param drawableId
     * @param canvas
     * @param key
     */
    private void drawKeyBackground(int drawableId, Canvas canvas, Keyboard.Key key) {
        Drawable npd = (Drawable) getContext().getResources().getDrawable(drawableId);
        int[] drawableState = key.getCurrentDrawableState();
        if (key.codes[0] != 0) {
            npd.setState(drawableState);
        }

        // 绘制按钮背景，加上 MyUtil.dp2px(4)
        npd.setBounds(key.x + (key.width - npd.getIntrinsicWidth()) / 2, key.y, key.x + key.width, key.y + key.height);
        npd.draw(canvas);
    }

    private void drawText(Canvas canvas, Keyboard.Key key) {
        Rect bounds = new Rect();
        bounds.set(key.x, key.y, key.x + key.width, key.y + key.height);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        if (Keyboard.KEYCODE_DONE == key.codes[0]) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(ContextCompat.getColor(MyApp.getInstance(), android.R.color.black));
        }
        if (key.label != null) {
            paint.getTextBounds(key.label.toString(), 0, key.label.toString().length(), bounds);
            canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                    (key.y + key.height / 2) + bounds.height() / 2, paint
            );
        }

    }

    class MyOnKeyboardActionListener implements OnKeyboardActionListener {
        /**
         * 是否是大写字母
         */
        private boolean mIsUpper;

        @Override
        public void onPress(int primaryCode) {
            // 设置某些按键不显示预览的 view
            if (primaryCode == Keyboard.KEYCODE_SHIFT
                    || primaryCode == Keyboard.KEYCODE_DELETE
                    || primaryCode == Keyboard.KEYCODE_DONE
                    || primaryCode == DemoKeyCode.CODE_SPACE
                    || primaryCode == DemoKeyCode.CODE_TYPE_QWERTY
                    || primaryCode == DemoKeyCode.CODE_TYPE_NUM
                    || primaryCode == DemoKeyCode.CODE_TYPE_SYMBOL
                    || primaryCode == DemoKeyCode.CODE_SETTING
                    || primaryCode == DemoKeyCode.CODE_TYPE_SWITCH_INPUT
                    || primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                setPreviewEnabled(false);
            } else {
                setPreviewEnabled(true);
            }
        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            // 键盘服务
            MyInputMethodService service = (MyInputMethodService) getContext();
            // 当前输入的连接
            InputConnection ic = service.getCurrentInputConnection();
            switch (primaryCode) {
                //删除
                case Keyboard.KEYCODE_DELETE:
                    ic.deleteSurroundingText(1, 0);
                    break;
                // 完成
                case Keyboard.KEYCODE_DONE:
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    break;
                // 大小写切换
                case Keyboard.KEYCODE_SHIFT:
                    MyUtill.switchUpperOrLowerCase(mIsUpper, mKeyboardLetter);
                    mIsUpper = !mIsUpper;
                    setKeyboard(mKeyboardLetter);
                    break;
                // 数字键盘切换
                case DemoKeyCode.CODE_TYPE_NUM:
                    setKeyboard(mKeyboardNum);
                    break;
                // 字母键盘切换
                case DemoKeyCode.CODE_TYPE_QWERTY:
                    setKeyboard(mKeyboardLetter);
                    break;
                // 符号键盘切换
                case DemoKeyCode.CODE_TYPE_SYMBOL:
                    setKeyboard(mKeyboardSymbol);
                    break;
                // settings
                case DemoKeyCode.CODE_SETTING:
                    Toast.makeText(service, "Settings==", Toast.LENGTH_SHORT).show();
                    break;
                // 切换输入法
                case DemoKeyCode.CODE_TYPE_SWITCH_INPUT:
                    ((InputMethodManager) service.getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
                    break;
                // 一般文本
                default:
                    char inputChar = (char) primaryCode;
                    ic.commitText(String.valueOf(inputChar), 1);
                    break;

            }

        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    }

}
