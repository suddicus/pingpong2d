package com.jhin.pong2dgame;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class PongTable extends SurfaceView implements SurfaceHolder.Callback{

    private GameThread mGame;
    private TextView mStatus;
    private TextView mScorePlayer;
    private TextView mSCoreOpponent;

    private Player mPlayer;
    private Player mOpponent;
    private Ball mBall;
    private Paint mNetPaint;
    private Paint mTableBoundsPaint;
    private int mTableHeight;
    private int mTableWidth;
    private Context mContext;

    SurfaceHolder mHolder;

    public static float PHY_RACQUET_SPEED = 15.0f;
    public static float PHY_BALL_SPEED = 15.0f;

    private float mAiMoveProbability;
    private boolean moving = false;
    private float mLastTouchY;

    public void initPongTable(Context ctx, AttributeSet attr){

        mContext = ctx;
        mHolder = getHolder();
        mHolder.addCallback(this);

        mGame = new GameThread(this.getContext(),mHolder,this,new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                mStatus.setVisibility(msg.getData().getInt("visibility"));
                mStatus.setText(msg.getData().getString("text"));
            }
        },new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                mScorePlayer.setText(msg.getData().getString("player"));
                mSCoreOpponent.setText(msg.getData().getString("opponent"));
            }
        });

        TypedArray a = ctx.obtainStyledAttributes(attr,R.styleable.PongTable);
        int racketHeight = a.getInteger(R.styleable.PongTable_racketHeight, 340);
        int racketWidth = a.getInteger(R.styleable.PongTable_racketWidth, 100);
        int ballRadius = a.getInteger(R.styleable.PongTable_ballRadius, 25);

        // Set Player
        Paint playerPaint  = new Paint();
        playerPaint.setAntiAlias(true);
        playerPaint.setColor(ContextCompat.getColor(mContext,R.color.player_color));
        mPlayer = new Player(racketWidth,racketHeight,playerPaint);

        // Set Opponent
        Paint opponentPaint = new Paint();
        opponentPaint.setAntiAlias(true);
        opponentPaint.setColor(ContextCompat.getColor(mContext,R.color.opponent_color));
        mOpponent = new Player(racketWidth,racketHeight,playerPaint);

        // Set Ball
        Paint ballPaint = new Paint();
        ballPaint.setAntiAlias(true);
        ballPaint.setColor(ContextCompat.getColor(mContext,R.color.ball_color));
        mBall = new Ball(ballRadius,ballPaint);

        // Draw Middle Lines
        mNetPaint = new Paint();
        mNetPaint.setAntiAlias(true);
        mNetPaint.setColor(Color.WHITE);
        mNetPaint.setAlpha(80);
        mNetPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mNetPaint.setStrokeWidth(10.f);
        mNetPaint.setPathEffect(new DashPathEffect(new float[]{5,5},0));

        // Draw Bounds
        mTableBoundsPaint = new Paint();
        mTableBoundsPaint.setAntiAlias(true);
        mTableBoundsPaint.setColor(ContextCompat.getColor(mContext,R.color.table_color));
        mTableBoundsPaint.setStyle(Paint.Style.STROKE);
        mTableBoundsPaint.setStrokeWidth(15.f);

        mAiMoveProbability = 0.8f;

    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        canvas.drawColor(ContextCompat.getColor(mContext,R.color.table_color));
        canvas.drawRect(0,0,mTableWidth,mTableHeight,mTableBoundsPaint);

        int middle = mTableWidth/2;
        canvas.drawLine(middle,1,middle, mTableHeight-1,mNetPaint);

        mPlayer.draw(canvas);
        mOpponent.draw(canvas);
        mBall.draw(canvas);
    }

    public PongTable(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPongTable(context,attrs);
    }

    public PongTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPongTable(context,attrs);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mTableWidth = width;
        mTableHeight = height;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    private void doAI(){
        if(mOpponent.bounds.top > mBall.cy){
            movePlayer(mOpponent,mOpponent.bounds.left,mOpponent.bounds.top - PHY_RACQUET_SPEED);
        }else if(mOpponent.bounds.top + mOpponent.getRacquetHeight() < mBall.cy){
            movePlayer(mOpponent,mOpponent.bounds.left,mOpponent.bounds.top + PHY_RACQUET_SPEED);
        }
    }

    public void update(Canvas canvas){
        // collisions code

        if(new Random(System.currentTimeMillis()).nextFloat() < mAiMoveProbability)
            doAI();
        mBall.moveBall(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(!mGame.SensorsOn()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mGame.isBetweenBounds()) {
                        mGame.setState(GameThread.STATE_RUNNING);
                    } else {
                        if (isTouchOnRacket(event, mPlayer)) {
                            moving = true;
                            mLastTouchY = event.getY();
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (moving) {
                        float y = event.getY();
                        float dy = y - mLastTouchY;
                        mLastTouchY = y;
                        movePlayerRacquet(dy, mPlayer);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    moving = false;
                    break;
            }
        }else {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                if (mGame.isBetweenBounds()){
                    mGame.setState(GameThread.STATE_RUNNING);
                }
            }
        }
        return true;
    }

    private boolean isTouchOnRacket(MotionEvent event, Player mPlayer){
        return mPlayer.bounds.contains(event.getX(),event.getY());
    }

    public void movePlayerRacquet(float dy,Player player){
        synchronized (mHolder){
            movePlayer(player,player.bounds.left,player.bounds.top + dy);
        }
    }
    public synchronized void movePlayer(Player player, float left, float top){
        if(left < 2){
            left = 2 ;
        }else if(left + player.getRacquetWidth() >= mTableWidth - 2){
            top = mTableWidth - player.getRacquetWidth() - 2;
        }
        if(top < 0){
            top = 0;
        }else if(top + player.getRacquetHeight() >= mTableHeight){
            top = mTableHeight - player.getRacquetHeight() - 1;
        }

        player.bounds.offsetTo(left,top);
    }

    public void setupTable(){
        placeBall();
        placePlayers();
    }

    private void placePlayers(){
        mPlayer.bounds.offsetTo(2,(mTableHeight - mPlayer.getRacquetHeight())/2);
        mOpponent.bounds.offsetTo(mTableWidth - mOpponent.getRacquetWidth()-2, (mTableHeight - mOpponent.getRacquetHeight())/2);
    }

    private void placeBall(){
        mBall.cx = mTableWidth/2;
        mBall.cy = mTableHeight/2;
        mBall.velocity_y = (mBall.velocity_y / Math.abs(mBall.velocity_y) * PHY_BALL_SPEED);
        mBall.velocity_x = (mBall.velocity_x / Math.abs(mBall.velocity_x) * PHY_BALL_SPEED);
    }


}
