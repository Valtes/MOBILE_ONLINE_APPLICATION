package ph.com.valtes.mobileapplication;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import ph.com.valtes.mobileapplication.R;

public class CaptureSignature extends Activity {

    LinearLayout mContent;
    signature mSignature;
    Button mClear, mGetSign, mCancel;
    public static String tempDir;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File mypath;

    private String uniqueId;
    private String encodedSignature;

    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.signature);

        tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.getSignature) + "/";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(getResources().getString(R.string.getSignature), Context.MODE_PRIVATE);

        prepareDirectory();
        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".jpg";
        mypath= new File(directory,current);


        mContent = (LinearLayout) findViewById(R.id.linearLayout);
        mSignature = new signature(this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mClear = (Button)findViewById(R.id.clear);
        mGetSign = (Button)findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button)findViewById(R.id.cancel);
        mView = mContent;

        mClear.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            Log.v("log_tag", "Panel Cleared");
            mSignature.clear();
            mGetSign.setEnabled(false);
            }
        });

        mGetSign.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            Log.v("log_tag", "Panel Saved");
            mView.setDrawingCacheEnabled(true);
            mSignature.save(mView);
            Bundle b = new Bundle();
            b.putString("status", "done");
            Intent intent = new Intent();
            intent.putExtras(b);
            setResult(RESULT_OK,intent);
            finish();
            }
        });

        mCancel.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            Log.v("log_tag", "Panel Canceled");
            Bundle b = new Bundle();
            b.putString("status", "cancel");
            Intent intent = new Intent();
            intent.putExtras(b);
            setResult(RESULT_OK,intent);
            finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        Log.w("GetSignature", "onDestroy");
        super.onDestroy();
    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate =     (c.get(Calendar.YEAR) * 10000) +
                ((c.get(Calendar.MONTH) + 1) * 100) +
                (c.get(Calendar.DAY_OF_MONTH));
        Log.w("DATE:",String.valueOf(todaysDate));
        return(String.valueOf(todaysDate));

    }

    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();
        int currentTime = (c.get(Calendar.HOUR_OF_DAY) * 10000) +
                (c.get(Calendar.MINUTE) * 100) +
                (c.get(Calendar.SECOND));
        Log.w("TIME:",String.valueOf(currentTime));
        return(String.valueOf(currentTime));
    }


    private boolean prepareDirectory()
    {
        try
        {
            if (makedirs())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Could not initiate File System.. Is Sdcard mounted properly?", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean makedirs()
    {
        File tempdir = new File(tempDir);
        if (!tempdir.exists()) {
            tempdir.mkdirs();
        }

        if (tempdir.isDirectory())
        {
            File[] files = tempdir.listFiles();
            for (File file : files)
            {
                if (!file.delete())
                {
                    System.out.println("Failed to delete " + file);
                }
            }
        }
        return (tempdir.isDirectory());
    }

    public class signature extends View
    {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v)
        {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());
            if(mBitmap == null)
            {
                mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);;
            }
            Canvas canvas = new Canvas(mBitmap);
            try
            {
                FileOutputStream mFileOutStream = new FileOutputStream(mypath);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    return;
                }

                String signatureUrl = Images.Media.insertImage(getContentResolver(), mBitmap, current, null);
                Log.v("log_tag","url: " + signatureUrl);

                //In case you want to delete the file
                //boolean deleted = mypath.delete();
                //Log.v("log_tag","deleted: " + mypath.toString() + deleted);
                //If you want to convert the image to string use base64 converter
                encodedSignature = getEncodeData();

            }
            catch(Exception e)
            {
                Log.v("log_tag", e.toString());
            }
            appendLog("test");
            return;
        }

        public void clear()
        {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++)
                    {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string){
        }

        private void expandDirtyRect(float historicalX, float historicalY)
        {
            if (historicalX < dirtyRect.left)
            {
                dirtyRect.left = historicalX;
            }
            else if (historicalX > dirtyRect.right)
            {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top)
            {
                dirtyRect.top = historicalY;
            }
            else if (historicalY > dirtyRect.bottom)
            {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY)
        {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

    private String getEncodeData() throws IOException {
        String encodedSignature = null;
        RegistrationActivity registrationActivity = new RegistrationActivity();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        encodedSignature = Base64.encodeToString(byteArray, Base64.DEFAULT);
        registrationActivity.injectSignatureString(encodedSignature);
        return encodedSignature;
    }

    public void appendLog(String text)
    {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
