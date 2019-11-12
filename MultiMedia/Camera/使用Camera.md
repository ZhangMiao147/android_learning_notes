# 使用 Camera
　　有关 Camera 的理论知识请查看：[]()。

## 使用 Camera 实现预览

#### 1. UI页面
　　在这里，使用 SurfaceView 来显示从相机获取到的画面。
```
    <SurfaceView
        android:id="@+id/shoot_video_surface_sv"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

#### 2. Camera 获取
```
package com.zhangmiao.clipassistant.Help;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Camera1 使用工具类
 * Author: zhangmiao
 * Date: 2019/11/8
 */
public class CameraHelper implements Camera.PreviewCallback {

    private static final String TAG = CameraHelper.class.getSimpleName();

    /**
     * Camera 对象
     */
    private Camera mCamera;
    /**
     * Camera 对象的参数
     */
    private Camera.Parameters mParameters;

    /**
     * 用于预览的 SurfaceView 对象
     */
    private SurfaceView mSurfaceView;

    /**
     * SurfaceHolder 对象
     */
    private SurfaceHolder mSurfaceHolder;

    private Activity mActivity;

    /**
     * 自定义的互调
     */
    private CallBack mCallBack;

    /**
     * 摄像头方向
     */
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

    /**
     * 预览旋转的角度
     */
    private int mDisplayOrientation = 0;


    public CameraHelper(Activity activity, SurfaceView surfaceView) {
        this.mActivity = activity;
        this.mSurfaceView = surfaceView;
        mSurfaceHolder = surfaceView.getHolder();
        Log.d(TAG, "CameraHelper mSurfaceHolder:" + mSurfaceHolder);
        init();
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void addCallBack(CallBack callBack) {
        this.mCallBack = callBack;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (mCallBack != null) {
            mCallBack.onPreviewFrame(bytes);
        }
    }

    public void takePic() {
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                mCamera.startPreview();
                if (mCallBack != null) {
                    mCallBack.onTakePic(bytes);
                }
            }
        });
    }

    private void init() {
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "surfaceCreated");
                if (mCamera == null) {
                    openCamera(mCameraFacing);
                }
                startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.d(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "surfaceDestroyed");
                releaseCamera();
            }
        });
    }

    /**
     * 打开相机
     *
     * @param cameraFacing
     * @return
     */
    private boolean openCamera(int cameraFacing) {
        boolean supportCameraFacing = supportCameraFacing(cameraFacing);

        if (supportCameraFacing) {
            try {
                mCamera = Camera.open(cameraFacing);
                initParameters(mCamera);
                mCamera.setPreviewCallback(this);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "打开相机失败！");
            }

        }

        return supportCameraFacing;
    }

    /**
     * 判断是否支持某个相机
     *
     * @param cameraFacing
     * @return
     */
    private boolean supportCameraFacing(int cameraFacing) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == cameraFacing) {
                return true;
            }
        }
        return false;
    }

    private void initParameters(Camera camera) {
        try {
            mParameters = camera.getParameters();
            mParameters.setPreviewFormat(ImageFormat.NV21);

            //获取与执行宽高相等或最接近的尺寸
            //设置预览尺寸
            Camera.Size bestPreviewSize = getBestSize(mSurfaceView.getWidth(), mSurfaceView.getHeight(), mParameters.getSupportedPreviewSizes());
            mParameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

            //设置保存图片尺寸
            Camera.Size bestPic = getBestSize(mSurfaceView.getWidth(), mSurfaceView.getHeight(), mParameters.getSupportedPictureSizes());
            mParameters.setPictureSize(bestPic.width, bestPic.height);

            //对焦模式
            if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            camera.setParameters(mParameters);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "相机初始化失败！");
        }

    }

    /**
     * 获取与指定宽高相等或最接近的尺寸
     *
     * @param targetWidth
     * @param targetHeight
     * @param sizeList
     * @return
     */
    private Camera.Size getBestSize(int targetWidth, int targetHeight, List<Camera.Size> sizeList) {
        Camera.Size bestSize = null;

        double targetRatio = (double) targetWidth / targetHeight;
        double minDiff = targetRatio;

        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);
            double supportedRatio = (double) size.width / size.height;
            Log.d(TAG, "系统支持的尺寸 " + size.width + "*" + size.height + ",比例：" + supportedRatio);
        }

        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);
            if (size.width == targetWidth && size.height == targetHeight) {
                bestSize = size;
                break;
            }

            double supportedRatio = (double) size.width / size.height;
            if (Math.abs(supportedRatio - targetRatio) < minDiff) {
                minDiff = Math.abs(supportedRatio - targetRatio);
                bestSize = size;
            }
        }

        Log.d(TAG, "目标尺寸：" + targetWidth + "*" + targetHeight + ",比例：" + targetRatio);
        Log.d(TAG, "最优尺寸：" + bestSize.width + "*" + bestSize.height);
        return bestSize;
    }

    /**
     * 判断是否支持某一对焦模式
     *
     * @param focusMode
     * @return
     */
    private boolean isSupportFocus(String focusMode) {
        boolean autoFocus = false;
        List<String> listFocusMode = mParameters.getSupportedFocusModes();
        for (int i = 0; i < listFocusMode.size(); i++) {
            String mode = listFocusMode.get(i);
            if (mode.equals(focusMode)) {
                autoFocus = true;
            }
            Log.d(TAG, "isSupportFocus 相机支持的对焦模式：" + mode);
        }
        return autoFocus;
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            setCameraDisplayOrientation(mActivity);
            mCamera.startPreview();
            if (mCallBack != null) {
                mCallBack.onStartPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "预览失败！");
        }

    }

    /**
     * 设置预览旋转的角度
     *
     * @param activity
     */
    private void setCameraDisplayOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraFacing, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int screenDegree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                screenDegree = 0;
                break;
            case Surface.ROTATION_90:
                screenDegree = 90;
                break;
            case Surface.ROTATION_180:
                screenDegree = 180;
                break;
            case Surface.ROTATION_270:
                screenDegree = 270;
                break;
            default:
                break;
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //前置摄像头
            mDisplayOrientation = (info.orientation + screenDegree) % 360;
            mDisplayOrientation = (360 - mDisplayOrientation) % 360;
        } else {
            mDisplayOrientation = (info.orientation - screenDegree + 360) % 360;
        }
        mCamera.setDisplayOrientation(mDisplayOrientation);

        Log.d(TAG, "setCameraDisplayOrientation 屏幕的旋转角度：" + rotation);
        Log.d(TAG, "setCameraDisplayOrientation mDisplayOrientation:" + mDisplayOrientation);


    }

    private ArrayList<RectF> transform(Camera.Face[] faces) {
        Matrix matrix = new Matrix();
        boolean mirror = (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        matrix.setScale(mirror ? -1f : 1f, 1f);
        matrix.postRotate(mDisplayOrientation);
        matrix.postScale(mSurfaceView.getWidth() / 2000f, mSurfaceView.getHeight() / 2000f);
        matrix.postTranslate(mSurfaceView.getWidth() / 2f, mSurfaceView.getHeight() / 2f);

        ArrayList<RectF> rectList = new ArrayList<>();

        for (int i = 0; i < faces.length; i++) {
            Camera.Face face = faces[i];
            RectF srcRect = new RectF(face.rect);
            RectF dstRect = new RectF(0f, 0f, 0f, 0f);
            matrix.mapRect(dstRect, srcRect);
            rectList.add(dstRect);
        }
        return rectList;
    }

    public void exchangeCamera() {
        releaseCamera();
        mCameraFacing = mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        openCamera(mCameraFacing);
        startPreview();
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public interface CallBack {
        void onPreviewFrame(byte[] data);

        void onTakePic(byte[] data);

        void onStartPreview();
    }
}

```

#### 3. 使用
```
package com.zhangmiao.clipassistant.Activity;

import android.app.Activity;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zhangmiao.clipassistant.Help.CameraHelper;
import com.zhangmiao.clipassistant.R;

/**
 * 拍摄视频界面
 * 使用 Camera1
 */
public class ShootVideoActivity extends Activity {

    private static final String TAG = ShootVideoActivity.class.getSimpleName();

    private SurfaceView sv_surface;
    private RelativeLayout rl_btnLayout;
    private ImageView iv_setting;
    private ImageButton ib_takePic;
    private ImageView iv_start;
    private ImageView iv_stop;
    private ImageView iv_exchange;

    private CameraHelper mCameraHelper;
    private boolean lock = false; //控制 MediaRecorderHelper 的初始化


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoot_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();

        mCameraHelper = new CameraHelper(this, sv_surface);
        mCameraHelper.addCallBack(new CameraHelper.CallBack() {
            @Override
            public void onPreviewFrame(byte[] data) {

            }

            @Override
            public void onTakePic(byte[] data) {

            }

            @Override
            public void onStartPreview() {
                if (!lock) {
                    Camera camera = mCameraHelper.getCamera();
                    if (camera != null) {
                        lock = true;
                    }
                }
            }
        });
    }

    private void initView() {
        sv_surface = (SurfaceView) findViewById(R.id.shoot_video_surface_sv);
        rl_btnLayout = (RelativeLayout) findViewById(R.id.shoot_video_btn_layout_rl);
        iv_setting = (ImageView) findViewById(R.id.shoot_video_setting_iv);
        ib_takePic = (ImageButton) findViewById(R.id.shoot_video_take_pic_ib);
        iv_start = (ImageView) findViewById(R.id.shoot_video_start_iv);
        iv_stop = (ImageView) findViewById(R.id.shoot_video_stop_iv);
        iv_exchange = (ImageView) findViewById(R.id.shoot_video_exchange_iv);
        iv_exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraHelper.exchangeCamera();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mCameraHelper.releaseCamera();
        super.onDestroy();
    }
}

```



