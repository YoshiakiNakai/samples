import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CanvasBasicView extends View {

    private Paint mPaint = new Paint();
    //private Resources res = this.getContext().getResources();   // R値の取得
    //private final Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.imp0079h);

    ArrayList<Bitmap> bmp = new ArrayList<>();
    ArrayList<Integer> bx = new ArrayList<>();
    ArrayList<Integer> by = new ArrayList<>();


    public CanvasBasicView(Context context) {
        super(context);
        //mPaint.setARGB(200,255,255,255);
        //mPaint.setTextSize(30);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int i=0; i<bmp.size(); i++) {
            canvas.drawBitmap(bmp.get(i), bx.get(i), by.get(i), mPaint);
        }
    }

    public void resetCanvas(){
        for(int i=0; i<bmp.size(); i++) {
            this.bmp.remove(0);
            this.bx.remove(0);
            this.by.remove(0);
        }
    }


    public void setBitmap(Bitmap bmp, int x, int y){
        this.bmp.add(bmp);
        this.bx.add(x);
        this.by.add(y);
    }

    public void draw(){
        invalidate();   // canvasの初期化と onDraw()の実行
    }


}
