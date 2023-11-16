package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Arrays;


import java.util.List;

public class OverlaySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder ;
    private Paint paint;
    private List<Integer> pathColorList;
    private Canvas canvas;
    private List<DetectionObject> objects = new ArrayList<>();

    public OverlaySurfaceView(SurfaceView surfaceView) {
        super(surfaceView.getContext());
        surfaceView.getHolder().addCallback(this);
        surfaceView.setZOrderOnTop(true);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        paint = new Paint();
        pathColorList = Arrays.asList(Color.RED, Color.GREEN, Color.CYAN, Color.BLUE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // SurfaceView를 투명하게 만듭니다.
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void draw(List<DetectionObject> detectedObjectList) {
//        objects = detectedObjectList;
//        Log.d("TAG", "draw: " + detectedObjectList.size());
//         SurfaceHolder를 통해 캔버스를 가져옵니다.
//        Log.d("TAG", "draw: " + detectedObjectList.size());
//        for(int i = 0; i< detectedObjectList.size(); i++) {
//            Log.d("TAG", "draw: " + detectedObjectList.get(i).getLabel());
//            Log.d("TAG", "draw: " + detectedObjectList.get(i).getBoundingBox().toString());
//        }

        canvas = surfaceHolder.lockCanvas();
        if (canvas == null) return;
//        Log.d("TAG", "draw: " + detectedObjectList.size());

        // 이전에 그린 것을 지웁니다.
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        for (int i = 0; i < detectedObjectList.size(); i++) {
            DetectionObject detectionObject = detectedObjectList.get(i);

            // 바운딩 박스 그리기
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7f);

            paint.setAntiAlias(false);
            RectF rectF = detectionObject.getBoundingBox();
            canvas.drawRect(rectF.left,rectF.top,rectF.right, rectF.bottom,paint);
//            canvas.drawRect(detectionObject.getBoundingBox(), paint);

//             라벨과 점수 표시
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setTextSize(77f);
            canvas.drawText(
                    detectionObject.getLabel() + " " + String.format("%.2f%%", detectionObject.getScore() * 100),
                    detectionObject.getBoundingBox().left,
                    detectionObject.getBoundingBox().top - 5f,
                    paint
            );
        }

        surfaceHolder.unlockCanvasAndPost(canvas);
    }



}
