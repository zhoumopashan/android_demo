package com.luo.demos.graphic.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
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
public class CanvasSurfaceView extends SurfaceView implements Callback, Runnable {
	// 用于控制SurfaceView
	private SurfaceHolder sfh;
	// 声明一个画笔
	private Paint paint;
	// 声明一条线程
	private Thread th;
	// 线程消亡的标识位
	private boolean flag;
	// 声明一个画布
	private Canvas canvas;
	// 声明屏幕的宽高
	private int screenW, screenH;
	// 设置画布绘图无锯齿
	private PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	
	private static final int START_X = 50;
	private static final int TEXT_SIZE = 20;
	private static final int NEW_LINE_SIZE = TEXT_SIZE + 20;
	int x = START_X , y = 0;
	private void newLine(){
		y += NEW_LINE_SIZE;
	}
	private void newLine(int lineSize){
		y += lineSize;
	}
	private void newTab(){
		x += 20;
	}
	private void newTab(int tab){
		x += ( tab + TEXT_SIZE );
	}
	private void newTab(String text){
		x += ( paint.measureText(text) + TEXT_SIZE );
	}
	private void newTab(Float textSize){
		x += ( textSize + TEXT_SIZE );
	}
	private void cleanScreen(){
		x = START_X; y = START_X;
	}
	private void backSpace(){
		x = START_X;
	}
	private void title(String text){
		backSpace();
		newLine();
		canvas.drawText(text, x, y, paint);
		newTab(text);
	}
	private void title(String text , int newLine){
		backSpace();
		newLine(newLine + NEW_LINE_SIZE);
		canvas.drawText(text, x, y, paint);
		newTab(text);
	}

	/**
	 * SurfaceView初始化函数
	 */
	public CanvasSurfaceView(Context context) {
		super(context);
		// 实例SurfaceHolder
		sfh = this.getHolder();
		// 为SurfaceView添加状态监听
		sfh.addCallback(this);
		// 实例一个画笔
		paint = new Paint();
		// 设置画笔颜色为白色
		paint.setColor(Color.YELLOW);
		paint.setTextSize(TEXT_SIZE);
		// 设置焦点
		setFocusable(true);
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
				// 设置画布绘图无锯齿,无锯齿 drawFilter 对象
//				private PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
				canvas.setDrawFilter(pfd); // ----设置画布绘图无锯齿
				canvas.drawColor(Color.BLACK); // ----利用填充画布，刷屏
				cleanScreen();
				
				title("绘制文本:");
				canvas.drawText("此文本为 drawText绘制出", x, y, paint); 

				title("画一个点:");
				canvas.drawPoint(x, y, paint); 
				title("连续画两个点:");
				canvas.drawPoints(new float[] { x, y, x + 50, y }, paint); // ----绘制多个像素点

				title("绘制直线:");
				canvas.drawLine(x, y, x + 50, y, paint);
				title("绘制多条直线:");
				canvas.drawLines(new float[] { x, y, x + 50, y, x + 70, y , x + 100, y }, paint);
				
				title("绘制矩形:");
				canvas.drawRect(x, y, x + 50, y + 50, paint);
				Rect rect = new Rect( x + 80 , y, x + 160, y + 50);
				canvas.drawRect(rect, paint);

				title("绘制圆角矩形:" , 50);
				RectF rectF = new RectF(x, y, x + 50, y + 50);
				canvas.drawRoundRect(rectF, 10, 10, paint);

				title("绘制圆形:" , 40);
				canvas.drawCircle(x + 60, y, 20, paint); 
				
				title("绘制弧形:" , 30);
				canvas.drawArc(new RectF(x, y, x + 50, y + 50), 0, 230, true, paint); 
				newTab(50);
				canvas.drawArc(new RectF(x, y, x + 50, y + 50), 0, 230, false, paint);
				
				title("绘制椭圆:" , 50);
				canvas.drawOval(new RectF(x, y, x + 50, y + 50), paint);

				title("绘制指定路径图形:" , 50);
				Path path = new Path();
				path.moveTo(x, y);// 设置路径起点
				path.lineTo(x, y + 20);// 路线1
				path.lineTo(x + 30, y + 20);
				path.close();
				canvas.drawPath(path, paint);

				title("在指定路径图形上输出文字:" , 50);
				Path pathCircle = new Path();
				pathCircle.addCircle( x + 30,  y , 20, Path.Direction.CCW); // 添加一个圆形的路径
				paint.setTextSize(10);
				canvas.drawTextOnPath("环形文字爽不爽!", pathCircle, 10, 20, paint);
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
