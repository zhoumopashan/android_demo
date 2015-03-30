package com.luo.demos.graphic.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

/**
 * 
 * @author Himi
 *
 */
public class PaintSurfaceView extends SurfaceView implements Callback, Runnable {
	// 用于控制SurfaceView
	private SurfaceHolder sfh;
	// 声明一条线程
	private Thread th;
	// 线程消亡的标识位
	private boolean flag;
	// 声明一个画布
	private Canvas canvas;
	// 声明屏幕的宽高
	private int screenW, screenH;
	private Paint mPaint;

	private static final int START_X = 50;
	private static final int TEXT_SIZE = 20;
	private static final int NEW_LINE_SIZE = TEXT_SIZE + 20;
	int x = START_X, y = 0;

	private void newLine() {
		y += NEW_LINE_SIZE;
	}

	private void newLine(int lineSize) {
		y += lineSize;
	}

	private void newTab() {
		x += 20;
	}

	private void newTab(int tab) {
		x += (tab + TEXT_SIZE);
	}

	private void newTab(String text) {
		x += (mPaint.measureText(text) + TEXT_SIZE);
	}

	private void newTab(Float textSize) {
		x += (textSize + TEXT_SIZE);
	}

	private void cleanScreen() {
		x = START_X;
		y = START_X;
	}

	private void backSpace() {
		x = START_X;
	}

	private void title(String text) {
		backSpace();
		newLine();
		canvas.drawText(text, x, y, mPaint);
		newTab(text);
	}

	private void tabTitle(String text) {
		newTab();
		canvas.drawText(text, x, y, mPaint);
		newTab(text);
	}

	private void tabTitle(String text, int tabSize) {
		newTab(tabSize);
		tabTitle(text);
	}

	private void title(String text, int newLine) {
		backSpace();
		newLine(newLine + NEW_LINE_SIZE);
		canvas.drawText(text, x, y, mPaint);
		newTab(text);
	}

	/**
	 * SurfaceView初始化函数
	 */
	public PaintSurfaceView(Context context) {
		super(context);
		// 实例SurfaceHolder
		sfh = this.getHolder();
		// 为SurfaceView添加状态监听
		sfh.addCallback(this);
		// 设置焦点
		setFocusable(true);

		// 实例一个画笔
		mPaint = new Paint();
		// 设置画笔颜色为白色
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize(TEXT_SIZE);
		mPaint.setAntiAlias(true);
	}

	/**
	 * SurfaceView视图创建，响应此函数
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		screenW = this.getWidth();
		screenH = this.getHeight();
		flag = true;
		// 实例线程
		th = new Thread(this);
		// 启动线程
		th.start();
	}

	/**
	 * 游戏绘图
	 */
	public void doDraw() {
		try {
			canvas = sfh.lockCanvas();
			if (canvas != null) {
				canvas.drawColor(Color.WHITE);
				cleanScreen();

				title("无抗矩齿:");
				Paint paint1 = new Paint();
				canvas.drawCircle(x, y, 20, paint1);
				tabTitle("有抗矩齿 setAntiAlias:", 20);
				paint1.setAntiAlias(true);
				canvas.drawCircle(x, y, 20, paint1);

				title("设置画笔的透明度 setAlpha:");
				Paint paint2 = new Paint();
				paint2.setAlpha(0x77); // setAlpha
				canvas.drawText("半透明度", x, y, paint2);

				title("绘制文本的锚点(align or gravity):");
				canvas.drawText("锚点", x, y, new Paint());
				title("绘制文本的锚点(align or gravity):");
				Paint paint3 = new Paint();
				paint3.setTextAlign(Paint.Align.CENTER);
				canvas.drawText("锚点", x, y, paint3);

				title("获取文本的长度 measureText: ");
				Paint paint4 = new Paint();
				float len = paint4.measureText("这是一个长文哦~:");
				canvas.drawText("这是一个长文哦~ 的长度是:" + len, x, y, new Paint());

				title("设置画笔样式:");
				canvas.drawRect(new Rect(x, y, x + 40, y + 30), new Paint());
				Paint paint5 = new Paint();
				title("不填充 setStyle(Style.STROKE): ", 40);
				paint5.setStyle(Style.STROKE);
				canvas.drawRect(new Rect(x, y, x + 40, y + 30), paint5);

				title("画笔颜色 setColor:", 40);
				Paint paint6 = new Paint();
				paint6.setColor(Color.RED);
				canvas.drawText("红色", x, y, paint6);

				// ------设置画笔的粗细程度
				title("画笔的粗细程度 setStrokeWidth:", 40);
				canvas.drawLine(x, y, x + 50, y, new Paint());
				Paint paint7 = new Paint();
				paint7.setStrokeWidth(7);
				newTab(70);
				canvas.drawLine(x, y, x + 50, y, paint7);

				// ------设置画笔绘制文本的字体粗细
				title("字体粗细 setTextSize:", 40);
				Paint paint8 = new Paint();
				paint8.setTextSize(20);
				canvas.drawText("文字尺寸", x, y, paint8);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}

	/**
	 * 触屏事件监听
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	/**
	 * 按键事件监听
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 游戏逻辑
	 */
	private void logic() {
	}

	@Override
	public void run() {
		while (flag) {
			long start = System.currentTimeMillis();
			doDraw();
			logic();
			long end = System.currentTimeMillis();
			try {
				if (end - start < 50) {
					Thread.sleep(50 - (end - start));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * SurfaceView视图状态发生改变，响应此函数
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	/**
	 * SurfaceView视图消亡时，响应此函数
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		flag = false;
	}
}
