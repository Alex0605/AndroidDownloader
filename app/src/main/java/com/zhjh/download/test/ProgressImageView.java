package com.zhjh.download.test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import static android.graphics.Canvas.ALL_SAVE_FLAG;

/**
 * An iOS style progress image view.
 *
 * Created by ydcool on 15/11/30.
 *
 * @author ydcool
 */
public class ProgressImageView extends ImageView {
    private static final int SMOOTH_ANIM_THRESHOLD = 5;

    private static final String TAG = "ProgressImageView";

    private float mProgress;
    private int mHeight;
    private int mWidth;
    private int mStrokeWidth;
    private float mRadius;
    private float mInterDelta;
    private int mMaskColor;

    private float mMaxMaskRadius;
    private float mMaskAnimDelta;
    private boolean mIsSquare;
    private boolean mMaskAnimRunning;

    private long mMediumAnimTime;

    private Paint mPaint;
    private Paint bgPaint;
    private Paint textPaint;
    private RectF mProgressOval;
    private ValueAnimator mInterAnim;
    private ValueAnimator mProgressAnimator;

    private int roundRadius = 40;

    private String mProgressStr = "0%";

    private int bmpWidth,bmpHeight;

    public ProgressImageView(Context context) {
        super(context);
        init(context, null);
    }

    public ProgressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProgressImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mMediumAnimTime = getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressImageView);
        try {
            this.mProgress = a.getInteger(R.styleable.ProgressImageView_pi_progress, 0);
            this.mStrokeWidth = a.getDimensionPixelOffset(R.styleable.ProgressImageView_pi_stroke, 8);
            this.mRadius = a.getDimensionPixelOffset(R.styleable.ProgressImageView_pi_radius, 0);
            this.mIsSquare = a.getBoolean(R.styleable.ProgressImageView_pi_force_square, false);
            this.mMaskColor = a.getColor(R.styleable.ProgressImageView_pi_mask_color, Color.argb(180, 0, 0, 0));

            this.mPaint = new Paint();
            mPaint.setColor(mMaskColor);
            mPaint.setAntiAlias(true);
            this.bgPaint = new Paint();

            this.textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(50.0f);
        } finally {
            a.recycle();
        }
    }

    private void initParams() {
        if (mWidth == 0)
            mWidth = getWidth();

        if (mHeight == 0)
            mHeight = getHeight();

        if (mWidth != 0 && mHeight != 0) {
            if (mRadius == 0)
                mRadius = Math.min(mWidth, mHeight) / 4f;

            if (mMaxMaskRadius == 0)
                mMaxMaskRadius = (float) (0.5f * Math.sqrt(mWidth * mWidth + mHeight * mHeight));

            if (mProgressOval == null)
                mProgressOval = new RectF(
                        mWidth / 2f - mRadius + mStrokeWidth,
                        mHeight / 2f - mRadius + mStrokeWidth,
                        mWidth / 2f + mRadius - mStrokeWidth,
                        mHeight / 2f + mRadius - mStrokeWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (null != drawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap b = getRoundBitmap(bitmap, roundRadius);
            final Rect rectSrc = new Rect(0, 0, b.getWidth(), b.getHeight());
            final Rect rectDest = new Rect(0,0,getWidth(),getHeight());
            bgPaint.reset();
            canvas.drawBitmap(b, rectSrc, rectDest, bgPaint);

        } else {
            super.onDraw(canvas);
        }
        int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, ALL_SAVE_FLAG);

        initParams();

        if (mProgress < 100) {
            drawMask(canvas);

            if (mProgress == 0)
                updateInterAnim(canvas);
            else
                drawProgress(canvas);

            if(mProgress>0) {
                drawProgressText(canvas);
            }
        }
        if (mMaskAnimRunning)
            updateMaskAnim(canvas);


        canvas.restoreToCount(sc);
    }

    private void drawProgressText(Canvas canvas){
        //绘制我们想要设置的文字 (并让它显示在圆水平和垂直方向的中心处)
        float textHeight = textPaint.descent() - textPaint.ascent();
        float verticalTextOffset = (textHeight / 2) - textPaint.descent();

        float horizontalTextOffset = textPaint.measureText(mProgressStr) / 2;
        canvas.drawText(mProgressStr, this.getWidth() / 2 - horizontalTextOffset,
                        this.getHeight() / 2 + verticalTextOffset, textPaint);
    }

    /**
     * 获取圆角矩形图片方法
     * @param bitmap
     * @param roundPx,一般设置成14
     * @return Bitmap
     * @author caizhiming
     */
    private Bitmap getRoundBitmap(Bitmap bitmap, int roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                                            bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;

        bmpWidth = bitmap.getWidth();
        bmpHeight = bitmap.getHeight();
        final Rect rect = new Rect(0, 0, bmpWidth, bmpHeight);
        final RectF rectF = new RectF(rect);
        bgPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        bgPaint.setColor(color);

        canvas.drawRoundRect(rectF, roundPx, roundPx, bgPaint);
        bgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, bgPaint);
        return output;


    }

    private void drawMask(Canvas canvas) {
        final Rect rect = new Rect(0, 0, mWidth, mHeight);
        final RectF rectF = new RectF(rect);
        mPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        mPaint.setColor(mMaskColor);
        canvas.drawRoundRect(rectF,roundRadius-16,roundRadius-16,mPaint);
    }

    private void drawProgress(Canvas canvas) {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius, mPaint);
        mPaint.setXfermode(null);

        //start angle : -90 ~ 270;sweep Angle : 360 ~ 0;
        canvas.drawArc(mProgressOval, -90 + mProgress * 3.6f, 360 - mProgress * 3.6f, true, mPaint);
    }

    private void updateInterAnim(Canvas canvas) {
        //outer circle
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawCircle(mWidth / 2.f, mHeight / 2.f, mRadius, mPaint);
        mPaint.setXfermode(null);

        //inner circle
        canvas.drawCircle(mWidth / 2.f, mHeight / 2.f, mRadius - mInterDelta, mPaint);
    }

    private void updateMaskAnim(Canvas canvas) {
        drawMask(canvas);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius + mMaskAnimDelta, mPaint);//mRatio : 0 ~ mRatio * 1.5
        mPaint.setXfermode(null);

        textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(textPaint);
        textPaint.setXfermode(null);
    }

    private void startInterAnim(final int progress) {
        if (mInterAnim != null)
            mInterAnim.cancel();

        mInterAnim = ValueAnimator.ofFloat(0.f, mStrokeWidth);
        mInterAnim.setInterpolator(new DecelerateInterpolator());
        mInterAnim.setDuration(getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
        mInterAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInterDelta = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mInterAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (progress > 0)
                    startProgressAnim(0, progress);
            }
        });
        mInterAnim.start();
    }

    private void startProgressAnim(float from, float to) {
        if (mProgressAnimator != null)
            mProgressAnimator.cancel();

        final boolean isReverse = from > to;

        mProgressAnimator = ValueAnimator.ofFloat(from, to);
        mProgressAnimator.setInterpolator(new DecelerateInterpolator());
        mProgressAnimator.setDuration(mMediumAnimTime);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();

                if (0 < mProgress && mProgress < 100)
                    invalidate();
                else if (mProgress == 100 && !isReverse)
                    startMaskAnim();
            }
        });
        mProgressAnimator.start();
    }

    private void startMaskAnim() {
        if (mProgressAnimator != null)
            mProgressAnimator.cancel();

        ValueAnimator animator = ValueAnimator.ofFloat(0.f, mMaxMaskRadius);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(mMediumAnimTime);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMaskAnimRunning = true;
                mMaskAnimDelta = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mMaskAnimRunning = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mMaskAnimRunning = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mMaskAnimRunning = false;
            }
        });
        animator.start();
    }


    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        this.mProgressOval = null;
        invalidate();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
        this.mProgressOval = null;
        invalidate();
    }
    public int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
        mPaint.setColor(mMaskColor);
        invalidate();
    }

    public int getProgress() {
        return (int) mProgress;
    }

    public void setProgress(int progress,String mProgressStr) {
        setProgress(progress,mProgressStr,true);
    }

    public void setProgress(int progress,String mProgressStr, boolean animate) {
        progress = Math.min(Math.max(progress, 0), 100);
        this.mProgressStr = mProgressStr;
        Log.d(TAG, "setProgress: p:" + progress + ",mp:" + mProgress);

        if (Math.abs(progress - mProgress) > SMOOTH_ANIM_THRESHOLD && animate) {
            if (mProgress == 0) {
                startInterAnim(progress);
            } else {
                startProgressAnim(mProgress, progress);
            }
        } else if (progress == 100 && animate) {
            mProgress = 100;
            startMaskAnim();
        } else {
            mProgress = progress;

            if (mProgress == 0.f)
                mInterDelta = 0.f;

            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mIsSquare) {
            int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
            int size = measuredWidth == 0 ? MeasureSpec.getSize(heightMeasureSpec) : measuredWidth;
            setMeasuredDimension(size, size);
        }
    }
}