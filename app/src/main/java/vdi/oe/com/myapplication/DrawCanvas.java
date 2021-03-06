package vdi.oe.com.myapplication;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by uplusplus on 17-7-10.
 */

public class DrawCanvas extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private RenderThread renderThread;
    private UpdateThread updateThread;
    private boolean isDraw = false;// 控制绘制的开关
    Bitmap bmp = null;
    Paint p = new Paint();
    private Handler handler = new Handler();


    CFPSMaker fps, fps_udpate;

    public DrawCanvas(Context context) {
        super(context);
        init(context);
    }

    public DrawCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawCanvas(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        nativeInit();

        holder = this.getHolder();
        holder.setFormat(PixelFormat.RGBA_8888);
        holder.addCallback(this);

        updateThread = new UpdateThread();
        renderThread = new RenderThread();

        p.setColor(Color.WHITE);
        p.setTextSize(100);

        fps = new CFPSMaker();
        fps_udpate = new CFPSMaker();

        bmp = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
        setBitmap(bmp);
    }

    @Override
    protected void finalize() throws Throwable {
        nativeFree();
        if(bmp != null) {
            bmp.recycle();
            bmp = null;
        }
        super.finalize();
    }

    int surfaceWidth, surfaceHeight;
    Rect rect = new Rect();

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
        setBitmapSize(surfaceWidth, surfaceHeight);
        rect.set(0,0, surfaceWidth, surfaceHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isDraw = true;
        updateThread.start();
//        renderThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDraw = false;
    }

    private String update_fps = "0";
    private class UpdateThread extends Thread {
        @Override
        public void run() {
            fps_udpate.setTime(System.nanoTime());
            // 不停绘制界面
            while (isDraw) {
                updateBitmap();
                fps_udpate.makeFPS();
                update_fps = fps_udpate.getFPS();
            }
            super.run();
        }
    }

    /**
     * 绘制界面的线程
     *
     * @author Administrator
     *
     */
    private class RenderThread extends Thread {
        @Override
        public void run() {

            fps.setTime(System.nanoTime());
            // 不停绘制界面
            while (isDraw) {
                drawUI();
            }
            super.run();
        }
    }

    /**
     * 界面绘制
     */
    public void drawUI() {
        Canvas canvas = holder.lockCanvas();
        try {
            drawCanvas(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    Integer count = 0;


    private void drawCanvas(Canvas canvas) {
        // 在 canvas 上绘制需要的图形
        if(bmp != null)
            canvas.drawBitmap(bmp, rect, rect, p);
        fps.makeFPS();
        canvas.drawText(fps.getFPS(), 200,200, p);
        canvas.drawText(update_fps, 600, 200, p);
        count++;
    }

    private void onNativeMessage(final String message) {

    }

    private void onUpdateBitmap(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                drawUI();
            }
        });
    }

    private native int nativeInit();
    private native int nativeFree();
    public  native int setBitmap(Bitmap bmp);
    public native int setBitmapSize(int width, int height);
    public native int updateBitmap();

    static {
        System.loadLibrary("native-lib");
    }
}