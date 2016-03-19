package com.vann.validecodetextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

/**
 * author： bwl on 2016-03-18.
 * email: bxl049@163.com
 */
public class ValideCodeTextView extends View {

    //最大随机字符间隔
    private static final int MAX_PADDING = 16;

    //字体大小
    private float mCodeSize;
    // 验证码长度
    private int mCodeLength;
    //验证码
    private String mCode;
    //控件文本画笔、范围
    private Paint mPaint;
    private Rect mBound;
    //每个字符左边距距离
    private int paddingleft;
    private int paddingTop;

    private int mWidthMode;

    // 噪点画笔
    private Paint mPointPaint;
    // 干扰线画笔
    private Paint mPathPaint;

    private Context mContext;
    private Random mRandom = new Random();

    public ValideCodeTextView(Context context) {
        this(context, null);
    }

    public ValideCodeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ValideCodeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        //获得自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.valideCode);
        mCodeLength = ta.getInt(R.styleable.valideCode_codeLength, 1);
        mCodeSize = ta.getDimension(R.styleable.valideCode_codeSize, 0);
        ta.recycle();
        mCode = getRandomText();
        //文本画笔初始化
        mPaint = new Paint();
        mPaint.setTextSize(mCodeSize);
        mBound = new Rect();
        mPaint.getTextBounds(mCode, 0, mCodeLength, mBound);

        mPathPaint = new Paint();
        mPointPaint = new Paint();
        //添加单击时间
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCode = getRandomText();
                postInvalidate();
            }
        });
    }

    /**
     * 计算测量值、模式，设置控件大小
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        mWidthMode = widthMode;
        int width, height;
        if (MeasureSpec.EXACTLY == widthMode) {
            width = widthSize;
        } else {
            mPaint.setTextSize(mCodeSize);
            randomPaintStyle(mPaint);
            int tWidth = 0;
            Rect child;
            for (int i = 0; i < mCodeLength; i++) {
                child = new Rect();
                char c = mCode.charAt(i);
                mPaint.getTextBounds(c + "", 0, 1, child);
                float w = child.width();
                int dw = (int) (w + MAX_PADDING);
                tWidth += dw;
            }
            width = tWidth + getPaddingLeft() + getPaddingRight();
        }
        if (MeasureSpec.EXACTLY == heightMode) {
            height = heightSize;
        } else {
            mPaint.setTextSize(mCodeSize);
            randomPaintStyle(mPaint);
            mPaint.getTextBounds(mCode, 0, mCodeLength, mBound);
            float tHeight = mBound.height();
            int h = (int) (tHeight + getPaddingTop() + getPaddingBottom());
            height = h + MAX_PADDING;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        Rect rec;
        int lastWidth = 0;
        paddingleft = getPaddingLeft();
        for (int i = 0; i < mCodeLength; i++) {
            paddingTop = getHeight() / 2 + mRandom.nextInt(MAX_PADDING);
            randomPaintStyle(mPaint);
            char ch = mCode.charAt(i);
            rec = new Rect();
            mPaint.getTextBounds(ch + "", 0, 1, rec);
            lastWidth = rec.width();
            paddingleft += lastWidth + mRandom.nextInt(MAX_PADDING);
            canvas.drawText(mCode.charAt(i) + "", paddingleft, paddingTop, mPaint);
        }
        int dw = getMeasuredWidth();
        int w = paddingleft + lastWidth;
//        Toast.makeText(mContext,"控件长度："+dw+"验证码长度："+w,Toast.LENGTH_SHORT).show();
        // 当模式不为EXACTLY且验证码长度大于控件长度时，重新获取验证码并绘制
        if (MeasureSpec.EXACTLY != mWidthMode && paddingleft + lastWidth > dw) {
            mCode = getRandomText();
            postInvalidate();
            return;
        }

        for (int i = 0; i < 2; i++) {
            randomPathPaintStyle(mPathPaint);
            drawLine(canvas, mPathPaint);
        }
        for (int i = 0; i < 50; i++) {
            randomPointPaintStyle(mPointPaint);
            drawPoint(canvas, mPointPaint);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

    }

    /**
     * 绘制噪点
     *
     * @param canvas
     * @param mPointPaint
     */
    private void drawPoint(Canvas canvas, Paint mPointPaint) {
        int x = mRandom.nextInt(getWidth());
        int y = mRandom.nextInt(getHeight());
        canvas.drawPoint(x, y, mPointPaint);
    }

    /**
     * 绘制干扰线
     *
     * @param canvas
     * @param mPaint
     */
    private void drawLine(Canvas canvas, Paint mPaint) {
        int startX = mRandom.nextInt(getWidth());
        int startY = mRandom.nextInt(getHeight());
        int endX = mRandom.nextInt(getWidth());
        int endY = mRandom.nextInt(getHeight());
        canvas.drawLine(startX, startY, endX, endY, mPaint);
    }


    /**
     * 获取随机位数的数字与符号的组合字符串
     *
     * @return
     */
    private String getRandomText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mCodeLength; i++) {
            String ch = mRandom.nextInt(2) % 2 == 0 ? "char" : "num";
            if ("char".equals(ch)) {
                int lowerOrUp = mRandom.nextInt(2) % 2 == 0 ? 65 : 97;
                char val = (char) (lowerOrUp + mRandom.nextInt(26));
                sb.append(val);
            } else if ("num".equals(ch)) {
                sb.append(String.valueOf(mRandom.nextInt(10)));
            }
        }
        return sb.toString();
    }

    /**
     * 获取随机颜色
     *
     * @param rate
     * @return
     */
    private int getRandomColor(int rate) {
        int red = mRandom.nextInt(256) / rate;
        int green = mRandom.nextInt(256) / rate;
        int blue = mRandom.nextInt(256) / rate;
        return Color.rgb(red, green, blue);
    }

    /**
     * 设置文本画笔的随机样式
     *
     * @param paint
     */
    private void randomPaintStyle(Paint paint) {
        paint.setTextSize(mCodeSize);
        paint.setColor(getRandomColor(1));
        paint.setFakeBoldText(mRandom.nextBoolean());//是否粗体
        float skewx = mRandom.nextInt(11) / 10;
        skewx = mRandom.nextBoolean() ? skewx : -skewx;
        paint.setTextSkewX(skewx);// float类型参数，负数右倾，正数左倾

    }

    /**
     * 设置干扰线画笔的随机样式
     *
     * @param paint
     */
    private void randomPathPaintStyle(Paint paint) {
        paint.setColor(getRandomColor(1));
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);//设置画笔为空心
        paint.setStrokeCap(Paint.Cap.ROUND); //设置断点出为圆形
    }

    /**
     * 设置噪点画笔样式
     *
     * @param paint
     */
    private void randomPointPaintStyle(Paint paint) {
        paint.setColor(getRandomColor(1));
        paint.setStrokeWidth(2);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

}
