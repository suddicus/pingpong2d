package com.jhin.pong2dgame;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.SystemClock;
import android.view.SurfaceHolder;

public class GameThread extends Thread{

    public static final int STATE_READY = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_WIN = 3;
    public static final int STATE_LOSE = 4;

    private boolean mSensorsOn;
    private final Context mCtx;
    private final SurfaceHolder mSurfaceHolder;
    private final PongTable mPongTable;
    private final Handler mGameStatusHandler;
    private final Handler mScoreHandler;

    private boolean mRun = false;
    private int mGameSate;
    private Object mRunLock;

    private static final int PHY_FPS = 60;
    public GameThread(Context mCtx, SurfaceHolder mSurfaceHolder, PongTable mPongTable, Handler mGameStatusHandler, Handler mScoreHandler) {
        this.mCtx = mCtx;
        this.mSurfaceHolder = mSurfaceHolder;
        this.mPongTable = mPongTable;
        this.mGameStatusHandler = mGameStatusHandler;
        this.mScoreHandler = mScoreHandler;
        mRunLock = new Object();
    }

    @Override
    public void run() {
        long mNextGameTick = SystemClock.uptimeMillis();
        int skipTicks = 1000 / PHY_FPS;

        while(mRun){
            Canvas c = null;
            try{
                c = mSurfaceHolder.lockCanvas(null);
                if(c != null){
                    synchronized (mSurfaceHolder){
                        if (mGameSate == STATE_RUNNING){
                            mPongTable.update(c);
                        }
                        synchronized (mRunLock){
                            if (mRun){

                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (c != null){
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }

            mNextGameTick = skipTicks;
            long sleepTime = mNextGameTick - SystemClock.uptimeMillis();
            if (sleepTime > 0){
                try{
                    Thread.sleep(sleepTime);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

        }
    }

    public void setStae(int state){

    }
}
