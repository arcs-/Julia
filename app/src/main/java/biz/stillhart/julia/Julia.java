package biz.stillhart.julia;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;

public class Julia extends Activity {

    private Bitmap mBitmap;
    private ImageView mDisplayView;

    private ScriptC_julia mJuliaScriptInterface;
    private Allocation mInPixelsAllocation;
    private Allocation mOutPixelsAllocation;

    private int mWidth;
    private int mHeight;

    public static float map(float val,float a1,float a2, float b1, float b2) {
        return b1 + (val - a1) * (b2 - b1) / (a2 - a1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        Bitmap.Config conf = Bitmap.Config.ARGB_4444;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, conf);

        mDisplayView = (ImageView) findViewById(R.id.display);
        mDisplayView.setImageBitmap(mBitmap);

        RenderScript mJuliaScript = RenderScript.create(this);
        mInPixelsAllocation = Allocation.createFromBitmap(mJuliaScript, mBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        mOutPixelsAllocation = Allocation.createFromBitmap(mJuliaScript, mBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        mJuliaScriptInterface = new ScriptC_julia(mJuliaScript);

        mJuliaScriptInterface.set_height(mHeight - 160);
        mJuliaScriptInterface.set_width(mWidth);

        mJuliaScriptInterface.set_precision(40);

        /*
        Ideas for even more crazy sets
        https://en.wikipedia.org/wiki/Julia_set
         */

        renderJulia(-0.8f, 0.156f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {

            case (MotionEvent.ACTION_MOVE):
                float x = event.getAxisValue(MotionEvent.AXIS_X);
                float y = event.getAxisValue(MotionEvent.AXIS_Y);
                float cx = map(x,0,mWidth,-2,2);
                float cy = map(y,0,mHeight,-2,2);
                renderJulia(cx, cy);
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    private void renderJulia(float cx, float cy) {
        Log.d("debug", "(" + cx + "," + cy + ")");

        mJuliaScriptInterface.set_cx(cx);
        mJuliaScriptInterface.set_cy(cy);
        mJuliaScriptInterface.forEach_root(mInPixelsAllocation, mOutPixelsAllocation);
        mOutPixelsAllocation.copyTo(mBitmap);

        mDisplayView.invalidate();
    }

}
