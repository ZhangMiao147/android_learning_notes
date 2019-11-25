package cn.dream.exerciseanalysis.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import cn.dream.exerciseanalysis.R;

/**
 *     <declare-styleable name="WaveViewBySinCos">
 <attr name="waveColor" format="color" />
 <attr name="waveAmplitude" format="dimension" />
 <attr name="waveSpeed" format="float" />
 <attr name="waveStartPeriod" format="float" />
 <attr name="waveStart" format="boolean" />
 <attr name="waveFillTop" format="boolean" />
 <attr name="waveFillBottom" format="boolean" />
 <attr name="waveFillType" format="enum">
 <enum name="top" value="0" />
 <enum name="bottom" value="1" />
 </attr>

 <attr name="waveType" format="enum">
 <enum name="sin" value="0" />
 <enum name="cos" value="1" />
 </attr>
 </declare-styleable>
 * 水波纹动画
 * Author: zhangmiao
 * Date: 2018/10/16
 */
public class WaveViewBySinCos extends View {


    private Context mContext;

    /**
     * 振幅
     */
    private int A;

    /**
     * 偏距
     */
    private int K;

    private int waveColor = 0xaaFF7E37;

    /**
     * 初相
     */
    private float q;

    /**
     * 波形移动的速度
     */
    private float waveSpeed = 3f;

    /**
     * 角速度
     */
    private double w;

    /**
     * 开始位置相差多少个周期
     */
    private double startPeriod;

    /**
     * 是否直接开启波形
     */
    private boolean waveStart;

    private Path path;
    private Paint paint;

    private static final int SIN = 0;
    private static final int COS = 1;

    private int waveType;

    private static final int TOP = 0;
    private static final int BOTTOM = 1;

    private int waveFillType;

    private ValueAnimator valueAnimator;

    public WaveViewBySinCos(Context context) {
        this(context, null);
    }

    public WaveViewBySinCos(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveViewBySinCos(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getAttr(attrs);
        K = A;
        initPaint();
        initAnimation();
    }

    private void getAttr(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.WaveViewBySinCos);
        waveType = typedArray.getInt(R.styleable.WaveViewBySinCos_waveType, SIN);
        waveFillType = typedArray.getInt(R.styleable.WaveViewBySinCos_waveFillType, BOTTOM);
        A = typedArray.getDimensionPixelOffset(R.styleable.WaveViewBySinCos_waveAmplitude, 10);
        waveColor = typedArray.getColor(R.styleable.WaveViewBySinCos_waveColor, waveColor);
        waveSpeed = typedArray.getFloat(R.styleable.WaveViewBySinCos_waveSpeed, waveSpeed);
        startPeriod = typedArray.getFloat(R.styleable.WaveViewBySinCos_waveStartPeriod, 0);
        waveStart = typedArray.getBoolean(R.styleable.WaveViewBySinCos_waveStart, false);
        typedArray.recycle();
    }

    private void initPaint() {
        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(waveColor);
    }

    private void initAnimation() {
        valueAnimator = ValueAnimator.ofInt(0, getWidth());
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        if (waveStart) {
            valueAnimator.start();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = 2 * Math.PI / getWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (waveType) {
            case SIN:
                drawSin(canvas);
                break;
            case COS:
                drawCos(canvas);
                break;
            default:
                break;

        }
    }

    private void drawSin(Canvas canvas) {
        switch (waveFillType) {
            case TOP:
                fillTop(canvas);
                break;
            case BOTTOM:
                fillBottom(canvas);
                break;
            default:
                break;
        }
    }

    private void drawCos(Canvas canvas) {
        switch (waveFillType) {
            case TOP:
                fillTop(canvas);
                break;
            case BOTTOM:
                fillCosBottom(canvas);
                break;
            default:
                break;
        }
    }

    private void fillTop(Canvas canvas) {
        q -= waveSpeed / 100;
        float y;
        path.reset();
        path.moveTo(0, getHeight());
        for (float x = 0; x <= getWidth(); x += 20) {
            y = (float) (A * Math.sin(w * x + q + Math.PI * startPeriod) + K);
            path.lineTo(x, getHeight() - y);
        }
        path.lineTo(getWidth(), 0);
        path.lineTo(0, 0);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void fillBottom(Canvas canvas) {
        q -= waveSpeed / 100;
        float sinY, cosY;
        path.reset();
        path.moveTo(0, 0);
        for (float x = 0; x <= getWidth(); x += 20) {
            sinY = (float) (A * Math.sin(w * x + q + Math.PI * startPeriod) + K);
            path.lineTo(x, sinY);
        }
        path.lineTo(getWidth(), getHeight());
        path.lineTo(0, getHeight());
        path.close();
        canvas.drawPath(path, paint);
    }

    private void fillCosBottom(Canvas canvas) {
        q -= waveSpeed / 100;
        float cosY;
        path.reset();
        path.moveTo(0, 0);
        for (float x = 0; x <= getWidth(); x += 20) {
            cosY = (float) (A * Math.cos(w * x + q + Math.PI * startPeriod) + K);
            path.lineTo(x, cosY);
        }
        path.lineTo(getWidth(), getHeight());
        path.lineTo(0, getHeight());
        path.close();
        canvas.drawPath(path, paint);
    }

    public void startAnimation() {
        if (valueAnimator != null) {
            valueAnimator.start();
        }
    }

    public void stopAnimation() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }
}
