package com.twofingergest.pallavi.twofingtester;

import org.opencv.android.BaseLoaderCallback;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.opencv.core.Core.normalize;
import static org.opencv.core.CvType.CV_32S;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;


    double alpha=1;//contrast
    int beta=5;//brightness
    int viewstate;//0:none, 1:first view of hands, 2: first and subseq out of view, 3:view of hands
    Boolean click=false;
    Bitmap bm, fullbm, rotbm;
    Canvas canv, fcanv;
    Mat m, mblock;
    int dispwd, dispht;
    Mat src;
    Region[] regions;
    int regno, check_length=0, change_l, change_r;
    int frame_ct=0;
    int px, py, prepx, prepy,scx, scy;
    double area_l, area_r, area_c, total_l=0, ptotal_l=0, ptotal_r, total_r, lim_total_r, lim_total_l,diag_r=0, diag_l=0, diag_c=0, distx_c=0, disty_c=0, pdistx=0, pdisty=0, bclx=0, bcly=0, bcrx=0, bcry=0;
    double  parea_c=0, parea_l=0, parea_r=0,  precx=0, precy=0, pdiag_l=0, pdiag_r=0;
    double startarea_c=0,startarea_l=0,startarea_r=0, scale=0;
    double[] lengths, widths, bb_c, bb_l, bb_r  ;
    double[][] bbxy;
    double ratio_l, ratio_r, pratio_l=0, pratio_r=0, len_l=0, len_r=0, len_c=0, wid_l=0, wid_r=0, wid_c=0;
    double leftx, centerx, rightx, lefty,righty=0, centery, scalex=1, scaley=1;
    Paint tpaint;
    double startratio_l, startratio_r, gapwidth_l, gapwidth_r;
//    Mat whitemat=new Mat();
    int  changelevel=8, changect=0, changethresh=2500, clicktime=0, clickpersist=5;
    double[] areachange , areac_l, areac_r, arealist;
    int detected=0;
    float scaleratio=0.75f;
    int maxlistct=0;
    int boxsize=150;
    int stx, sty, rows, cols;
    double moveddist=0;
    int clickgap=7, clickct=0;
    Paint pt, qpt;
    long clickdwellstart=0, clickdwelltime=0,clickdwellgap=50;
    int calib2=0, check_len2=0, caliblen2=70;
    int qct=0, qlimit=20, ansct=0, mirror;
    int currdgt=0, precurrdgt=0;
    int currans=0, score=0;
    double endcx, endcy;
    long startTime, elapsedTime, completeTime, ansstarttime=0, anstime, traveltime;
    SensorManager mSensorManager;
    Sensor mSensor;
    float[] gravity, linear_acceleration;
    int calibrated=0, cbind=0;
    double cbbuff, fscale_l=1, fscale_r=1, pfscale_l=1, pfscale_r=1;
    int swing=0, swingtime=0, swinggap=10;
    int listct, l_ind, c_ind, r_ind;
    double finalscale=1, pfinalscale=1;
    double[][] centerlist;
    double[] c2_areac, c2_arear, c2_areal, c2_ratiol, c2_ratior, c2_diagl, c2_diagr;
    int orient=0;
    int fadedcol,activecol, dangercol, topCol,botCol,RCol,LCol;
    Paint ptop, pbot, pleft, pright;
    int strokewd;
    int filled=0, fulllistct=0, bgcount=0;

    String filename = "myfile";
    String fileContents = "Hello world!";
    FileOutputStream outputStream;
    File testFile;

    float deltaX = 0;
    float deltaY = 0;
    float deltaZ = 0;
    float deltaW = 0;
    float[] deltaxs, deltays, deltazs;
    int questions[]={5,1,6,7,9,3,2,4,8,2, 5,1,6,7,9,3,2,4,8, 2};

    int travelrec=0, viewtrigx=0, viewtrigy=0, viewtrigz=0;

    static SensorManager sensorService;

    Sensor sensor;
    float lastX=0, lastY=0, lastZ=0, lastW=0;
    int move=0, viewtrigger=0;

    int bpx, bpy, basksize=50, lookchance;
    String lookdir="";
    int quesorder;







    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity () {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        startTime= System.currentTimeMillis();
        ansstarttime=System.currentTimeMillis();


        //Initialise Variables


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dispwd=metrics.widthPixels;
        dispht=metrics.heightPixels;
        px=dispwd/2;
        py=dispht/2;

        areachange=new double[changelevel];
        areac_l=new double[changelevel];
        areac_r=new double[changelevel];
        arealist=new double[changelevel];
        centerlist=new double[changelevel][2];

        scx=px;
        scy=py;
        tpaint=new Paint();
        tpaint.setColor(Color.BLUE);
        tpaint.setTextSize(50);

        stx=dispwd/8;
        mirror=dispwd/2;
        sty=370;
        cols = 3;
        rows = 3;
        viewstate=0;
        prepx=stx+ (boxsize*rows);
        prepy=sty + (boxsize*cols);


        fadedcol=Color.parseColor("#909caf"); //grey
        activecol=Color.parseColor("#74e084"); //green
        dangercol=Color.parseColor("#af483d"); //red
        strokewd=15;
        ptop=new Paint();
        ptop.setStyle(Paint.Style.STROKE);
        ptop.setStrokeWidth(strokewd);

        pbot=new Paint();
        pleft=new Paint();
        pright=new Paint();

        pbot.setStyle(Paint.Style.STROKE);
        pbot.setStrokeWidth(strokewd);


        pleft.setStyle(Paint.Style.STROKE);
        pleft.setStrokeWidth(strokewd);
        pleft.setColor(LCol);

        pright.setStyle(Paint.Style.STROKE);
        pright.setStrokeWidth(strokewd);
        pright.setColor(RCol);

        c2_areac=new double[caliblen2];
        c2_arear=new double[caliblen2];
        c2_areal=new double[caliblen2];

        c2_ratior=new double[caliblen2];
        c2_ratiol=new double[caliblen2];

        c2_diagl=new double[caliblen2];
        c2_diagr=new double[caliblen2];

        deltaxs=new float[changelevel];
        deltays=new float[changelevel];
        deltazs=new float[changelevel];




    clickct=clickgap;
        Random randq=new Random();
        //quesorder = randq.nextInt(5)+1;
        quesorder=0;
        genQues();

        sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorService.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (sensor != null) {
            sensorService.registerListener(mySensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("Compass MainActivity", "Registerered for ORIENTATION Sensor");
        } else {
            Log.e("Compass MainActivity", "Registerered for ORIENTATION Sensor");
            Toast.makeText(this, "ORIENTATION Sensor not found",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        qpt=new Paint();


        try {
            // Creates a file in the primary external storage space of the
            // current application.
            // If the file does not exists, it is created.
             testFile = new File(this.getExternalFilesDir(null), "TestFile.txt");
            if (!testFile.exists())
                testFile.createNewFile();

            // Adds a line to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, true /*append*/));
            writer.write("This is a test file.");
            writer.close();
            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug the device to see the
            // latest changes. This is not necessary if the user should not modify
            // the files.
            MediaScannerConnection.scanFile(this,
                    new String[]{testFile.toString()},
                    null,
                    null);
        } catch (IOException e) {
            Log.e("ReadWriteFile", "Unable to write to the TestFile.txt file.");
        }



        int totalgridsize=rows*cols;
    }

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            deltaX = 0;
            deltaY = 0;
            deltaZ = 0;
            deltaW = 0;

            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West

            deltaX = Math.abs(lastX - event.values[0]);
            deltaY = Math.abs(lastY - event.values[1]);
            deltaZ = Math.abs(lastZ - event.values[2]);
            // deltaW = Math.abs(lastW - event.values[3]);

            // if the change is below 2, it is just plain noise
            float noisethresh=0.04f;
            if (deltaX < noisethresh)
                deltaX = 0;
            if (deltaY < noisethresh)
                deltaY = 0;
            if (deltaZ < noisethresh)
                deltaZ = 0;
            if (deltaW < noisethresh)
                deltaW = 0;
            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];
            //   lastW = event.values[3];

          if(deltaX!=0 || deltaY!=0 ||deltaZ!=0 ||deltaW!=0 )
           // if(deltaY!=0 )
              {
                  if(deltaX!=0 && deltaY==0 && deltaZ==0)
                  {
                      viewtrigx++;
                      viewtrigger++;
                  }

                  else if(deltaY!=0 && deltaX==0 && deltaZ==0)
                  {
                      viewtrigy++;
                      viewtrigger++;
                  }

                  else if(deltaZ!=0 && deltaY==0 && deltaX==0)
                  {
                      viewtrigz++;
                      viewtrigger++;
                  }


         //     Log.e("sensorvals", "\tValues:\t" + deltaX + "\t" + deltaY + "\t" + deltaZ + "\t" + deltaW);
            }
            // compassView.updateData(azimuth);
        }
    };

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    float alpha1;
   /* mSensorManager.registerListener(new SensorEventListener() {
        @Override
        public void onSensorChanged (SensorEvent event){
            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.

            alpha1 = 0.8f;

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha1 * gravity[0] + (1 - alpha1) * event.values[0];
            gravity[1] = alpha1 * gravity[1] + (1 - alpha1) * event.values[1];
            gravity[2] = alpha1 * gravity[2] + (1 - alpha1) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0];
            //- gravity[0];
            linear_acceleration[1] = event.values[1];
            //- gravity[1];
            linear_acceleration[2] = event.values[2];
            //- gravity[2];
            Log.e("analysis4", "lxy:\t" + linear_acceleration[0] + "\t" + linear_acceleration[1]);
        }
        @Override
        public void onAccuracyChanged(Sensor mSensor, int accuracy) {
        }
    }, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    */

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    int nrows, ncols, top_offset,left_offset,subwd, subht, wd, ht, nlabels;
    Rect rect1;
    List<Double> areasList = new ArrayList<Double>();
    double contourarea;

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        regno=0;
        Mat blank=new Mat();
        Mat rgbimg=inputFrame.rgba();
        Mat orig_img = inputFrame.gray();
        Mat img = new Mat(orig_img.size(), orig_img.type());
        orig_img.convertTo(img, -1, alpha, beta);

        src=rgbimg;






        nrows = img.height();//y
        ncols = img.width();//x

        top_offset = nrows / 2 + 20;
        left_offset = 0;
        subwd = ncols / 2 + 35;
        subht = nrows / 2 - 20;
     //   rect1 = new Rect(left_offset, top_offset, subwd, subht);
       rect1 = new Rect(0, 0, ncols, nrows);
        Mat subimg = new Mat(img, rect1);
        wd = subimg.width();
        ht = subimg.height();

        m=img;
       // List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> contourList = new ArrayList<MatOfPoint>();
        List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
        double mMinContourArea = 0.1;



       m=detectSkin(src);


       Mat m_Gray=new Mat();
        Mat m_Gray2=new Mat();
       Mat labeled;
        Mat cannyEdges = new Mat();
     // Imgproc.cvtColor(m, m_Gray2, Imgproc.COLOR_BGR2GRAY );


        Mat binarized=new Mat();
   Imgproc.threshold(m, binarized, 120, 255, Imgproc.THRESH_BINARY);


  // Imgproc.threshold(binarized, binarized, 0, 255, Imgproc.THRESH_BINARY);

  m=binarized;
    Imgproc.cvtColor(binarized, m_Gray, Imgproc.COLOR_BGR2GRAY );

        Imgproc.threshold(m_Gray, binarized, 150, 255, Imgproc.THRESH_BINARY);
        m=binarized;

        //Imgproc.adaptiveThreshold(m_Gray, binarized, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 15, 0);
       // m=binarized;

      //  Mat rectComponents=new Mat();
        //Mat centComponents=new Mat();

        labeled = new Mat(binarized.size(), binarized.type());

        // Extract components
        Mat rectComponents = Mat.zeros(new Size(0, 0), 0);
        Mat centComponents = Mat.zeros(new Size(0, 0), 0);
        java.util.List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy=new Mat();
        //= new java.util.List<MatOfPoint>();

        Mat bin2=new Mat();
        bin2= binarized.clone();

       nlabels=Imgproc.connectedComponentsWithStats(binarized, labeled, rectComponents, centComponents, 8, CV_32S );

        Log.e("nlabels:", "\n\t" + nlabels );
     //   for (int label = 1; label < nlabels; ++label){ //label  0 is the background

              //  labels_finals.push_back(label);
                //cout << "hola" << endl;
        //    }
        Imgproc.findContours(bin2,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_NONE);


     //   Imgproc.findContours(bin2, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        //matrix = ConnectedComponentsLabelling.twoPass(matrix);
        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
             contourarea = Imgproc.contourArea(contour);
            areasList.add(contourarea);
            Log.e("contours", "\n\tArea:  " + contourarea );

        }
        Log.e("contours", "\n\t-------------------------------------------"  );

        // Collect regions info
        int[] rectangleInfo = new int[5];
        double[] centroidInfo = new double[2];
         regions = new Region[rectComponents.rows() - 1];

        for(int i = 1; i < rectComponents.rows(); i++) {
            regno++;

            // Extract bounding box
            rectComponents.row(i).get(0, 0, rectangleInfo);
            Rect rectangle = new Rect(rectangleInfo[0], rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);

            // Extract centroids
            centComponents.row(i).get(0, 0, centroidInfo);
            Point centroid = new Point(centroidInfo[0], centroidInfo[1]);

            regions[i - 1] = new Region(rectangle, centroid);
            if(rectangleInfo[2]>2 && rectangleInfo[3] > 2 && (rectangleInfo[3]<720 || rectangleInfo[2]<1280))

            {
             //   Log.e("REGINFO", "\n\tx:" + centroidInfo[0] + "\ty:" + centroidInfo[1]);
              //  Log.e("REGINFO", "\n\twidth:" + rectangleInfo[2] + "\theight:" + rectangleInfo[3]);
            }
        }
       // if(rectangleInfo[2]>2 && rectangleInfo[3] > 2 && (rectangleInfo[3]<720 || rectangleInfo[2]<1280))
          //  Log.e("REGINFO", "------------"+regno+"\n\n");
        rectComponents.release();
        centComponents.release();



    bm = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
       Utils.matToBitmap(m, bm);
     //bm = Bitmap.createBitmap(m_Gray2.cols(), m_Gray2.rows(), Bitmap.Config.ARGB_8888);
 // Utils.matToBitmap(m_Gray2, bm);
      // bm = Bitmap.createBitmap(contours.cols(), contours.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(contours, bm);
        canv=new Canvas(bm);
        fullbm = Bitmap.createBitmap(dispwd, dispht, Bitmap.Config.ARGB_8888);

        fcanv=new Canvas(fullbm);

     //   ImageView fulliv = (ImageView) findViewById(R.id.imageView4);
//        fulliv.setImageBitmap(fullbm);

        runOnUiThread(new Runnable()
        {
            @Override
            public void run() {

                ImageView fulliv2 = (ImageView) findViewById(R.id.imageView5);
                fulliv2.setImageBitmap(fullbm);


                double cx, cy;
                double[] listcx, listcy;


                fulllistct=0;
                bgcount=0;

                px=0;
                py=0;
                int[] areas=new int[3];
                lengths=new double[3];
                widths=new double[3];
                listcx=new double[3];
                listcy=new double[3];
                bbxy=new double[3][2];
                bb_c=new double[2];
                bb_l=new double[2];
                bb_r=new double[2];
                 listct=0;

                Paint paint=new Paint();
                int wid, height, bwd, bht, rw, rh, r0,r1,r2,r3;
                wid=m.cols();
                height=m.rows();
                int l_bd, r_bd, t_bd, b_bd;


//
//               l_bd=wid/8;
//               r_bd=wid-l_bd;
//
//               t_bd=height/8;
//               b_bd=height-t_bd;
//
//
//                bwd=2*wid/3;
//                bht=2*height/3;


                l_bd=wid/8;
                r_bd=wid-l_bd;

                t_bd=height/8;
                b_bd=height-t_bd;


                bwd=wid;
                bht=height;



                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
               // canv.drawRect(wid/2-wid/3,height/2-height/3,wid/2+wid/3,height/2+height/3, paint);
                canv.drawRect(l_bd,t_bd,r_bd,b_bd, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.RED);
                //canv.drawCircle(500, 500, 30, paint);
               // fcanv.drawRect(0,0,dispwd,dispht,paint);

                fcanv.drawLine(mirror, 0, mirror,  dispht,paint);

                for(int i = 1; i < regno; i++)
                {
                    cx=regions[i-1].centroid.x;
                    cy=regions[i-1].centroid.y;

                    r0=regions[i-1].bounding.x;
                    r1=regions[i-1].bounding.y;
                    r2=regions[i-1].bounding.width;
                    r3=regions[i-1].bounding.height;



                  // if(between(cx,wid/2-wid/4,wid/2+wid/4) &&between(cy,height/2-height/4,height/2+height/4)  )
                    if((between(r2,20,300) && between(r3,20,300) && (r3<720 || r2<1280)) && (between(cx,l_bd,r_bd) &&between(cy,t_bd,b_bd)))
                    {

                        if(listct<3)
                        {
                            listcx[listct]=cx;
                            listcy[listct]=cy;
                            areas[listct]=r2*r3;
                            if(r2>r3)
                            {
                                lengths[listct]=r2;
                                widths[listct]=r3;
                            }
                            else
                            {
                                lengths[listct]=r3;
                                widths[listct]=r2;
                            }
                        }
                        listct++;
                      paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.BLUE);
                      canv.drawCircle((float)cx,(float)cy,10,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.RED);
                        canv.drawRect(regions[i-1].bounding.x,regions[i-1].bounding.y,regions[i-1].bounding.x+regions[i-1].bounding.width,regions[i-1].bounding.y + regions[i-1].bounding.height ,paint);
                        paint.setStyle(Paint.Style.FILL);
                    }
                    if((r2>10 && r3>10)  && (between(cx,l_bd,r_bd) &&between(cy,t_bd,b_bd)))
                        fulllistct++;
                    if((r2>300 && r3>300)  && (between(cx,l_bd,r_bd) &&between(cy,t_bd,b_bd)))
                        bgcount++;
                }


                area_l=0;
                area_r=0;
                area_c=0;
                ratio_l=0;
                ratio_r=0;
                leftx=100000;
                centerx=0;
                rightx=0;
                centery=0;
                c_ind=0;
                l_ind=0;
                r_ind=0;
                if(listct==3 && bgcount<=2) {

                    detected=1;
                    if(viewstate==0)
                        viewstate=1;
                    else
                        viewstate=3;

                    if(listcx[0]<listcx[1]) {
                        if (listcx[0] < listcx[2])
                        {
                            if (listcx[1] < listcx[2]) {
                                l_ind = 0;
                                c_ind = 1;
                                r_ind = 2;
                            } else {
                                l_ind = 0;
                                c_ind = 2;
                                r_ind = 1;
                            }
                        }
                        else
                        {
                            l_ind = 2;
                            c_ind = 0;
                            r_ind = 1;

                        }
                    }
                    else
                    {
                        if (listcx[1] < listcx[2])
                        {
                            if(listcx[0]<listcx[2])
                            {
                                l_ind = 1;
                                c_ind = 0;
                                r_ind = 2;
                            }
                            else
                            {
                                l_ind = 1;
                                c_ind = 2;
                                r_ind = 0;
                            }

                        }
                        else
                        {
                            l_ind = 2;
                            c_ind = 1;
                            r_ind = 0;
                        }

                    }

                    int k= l_ind;
                    leftx = listcx[l_ind];
                    lefty= listcy[l_ind];
                    area_l = areas[l_ind];
                    len_l=lengths[l_ind];
                    wid_l=widths[l_ind];


                k=r_ind;

                    rightx = listcx[k];
                    righty= listcy[k];
                    area_r = areas[k];
                    len_r=lengths[k];
                    wid_r=widths[k];


              k= c_ind;
                    centerx = listcx[k];
                    centery = listcy[k];
                    area_c=areas[k];
                    len_c=lengths[k];
                    wid_c=widths[k];




                    /*for (int k = 0; k < listct; k++) {



                        if (listcx[k] <=leftx) {
                            leftx = listcx[k];
                            lefty= listcy[k];
                            area_l = areas[k];
                            len_l=lengths[k];
                            wid_l=widths[k];

                        }

                        if (listcx[k] >= rightx) {
                            rightx = listcx[k];
                            righty= listcy[k];
                            area_r = areas[k];
                            len_r=lengths[k];
                            wid_r=widths[k];
                        }

                         if (listcx[k]<rightx && listcx[k]>leftx) {
                            centerx = listcx[k];
                            centery = listcy[k];
                            area_c=areas[k];
                            len_c=lengths[k];
                            wid_c=widths[k];
                        }

                    }*/




                    if (centerx != 0) {

                        if(area_l!=0)
                        ratio_l=area_c/area_l;
                        else
                            ratio_l=0;

                        if(area_r!=0)
                        ratio_r=area_c/area_r;
                        else
                            ratio_r=0;

                        diag_l=Math.sqrt((len_l*len_l)+(wid_l*wid_l));
                        diag_r=Math.sqrt((len_r*len_r)+(wid_r*wid_r));
                        diag_c=Math.sqrt((len_c*len_c)+(wid_c*wid_c));

                     //   bclx=leftx-diag_l/2;
                      //  bcly=lefty-

                        if(check_length<30)
                        {

                            check_length++;

                            //total_l=diag_l+diag_c + diag_c/2 ;
                            //total_r=diag_r+diag_c+ diag_c/2 ;
                            total_l=diag_l;
                            total_r=diag_r;
                            endcx=centerx;
                            endcy=centery;

                            startarea_c=area_c;
                            startarea_l=area_l;
                            startarea_r=area_r;

                            startratio_l=ratio_l;
                            startratio_r=ratio_r;

                            if(ptotal_l!=0 && check_length>30-5)
                            {
                                total_l=(total_l+ptotal_l)/2;
                                total_r=(total_r+ptotal_r)/2;

                                lim_total_r=total_r*scaleratio;
                                lim_total_l=total_l*scaleratio;

                                gapwidth_l=total_l-lim_total_l;
                                gapwidth_r=total_r-lim_total_r;

                                startarea_c=(area_c+parea_c)/2;
                                startarea_l=(area_l+parea_l)/2;
                                startarea_r=(area_r+parea_r)/2;
                            }

                            Log.e("totals:", "\n\tx " + total_l + "\ty " + total_r);
                            Log.e("totdist", "\n\tX" + distx_c + "\ty" + disty_c);
                            qpt.setColor(Color.BLACK);
                            qpt.setTextSize(80);
                            fcanv.drawText("Calibrating", 275, 300, qpt);
                            fcanv.drawText("Calibrating", 275 + mirror, 300, qpt);

                        }
                        else if(check_length==30 )
                        {


                            calibrated=1;
                        }
                        if(calibrated==1 && check_len2<caliblen2)
                        {

                            if(area_c!=0) {
                                c2_areac[check_len2]=area_c;
                                if(area_l!=0)
                                c2_ratiol[check_len2] = area_c / area_l;
                                else
                                    c2_ratiol[check_len2] =0;

                                if(area_r!=0)
                                c2_ratior[check_len2] = area_c / area_r;
                                else
                                    c2_ratior[check_len2] =0;


                                c2_diagl[check_len2]=diag_l;
                                c2_diagr[check_len2]=diag_r;

                                if(diag_l>total_l) {
                                    total_l = diag_l;
                                    lim_total_l=total_l*scaleratio;
                                    gapwidth_l=total_l-lim_total_l;
                                }
                                if(diag_r>total_r) {
                                    total_r = diag_r;
                                    lim_total_r=total_r*scaleratio;
                                    gapwidth_r=total_r-lim_total_r;
                                }

                                check_len2++;

                            }



                        }
                        else
                        if (check_len2 == caliblen2 && calib2==0) {
                            calib2 = 1;
                            ansstarttime=System.currentTimeMillis();
                        }

                        if(calib2==1) {
                          //  fcanv.drawText("Ready", 255 + mirror, 200, tpaint);
                        //    fcanv.drawText("Ready",255, 200, tpaint);
                        }
                        else if (calibrated==1)
                        {
                            fcanv.drawText("Testing Range", 275, 300, qpt);
                            fcanv.drawText("Testing Range", 275 + mirror, 300, qpt);
                        }

                       if(checkForChange())
                        {

//
//                            distx_c =(diag_l);
//                            disty_c=total_r-diag_r;
//
//                            if(diag_l>total_l)
//                                distx_c=total_l;
//                            if(diag_r>total_r)
//                                disty_c=0;


                           // diag_l=diag_l*scaleratio;
                           // diag_r=diag_r*scaleratio;
                            distx_c =(diag_l);
                            disty_c=diag_r;
//
//                            if(diag_l>lim_total_l)
//                               // lim_total_l=diag_l;
//                               distx_c=0;
//                            if(diag_r>lim_total_r)
//                                //lim_total_r=diag_r;
//                                disty_c=0;

//
                            //if(diag_l>lim_total_l)
                               // lim_total_l=diag_l;
                                //distx_c=0;
                         //   else
                               // distx_c =total_l-(diag_l);
                           // if(diag_r>lim_total_r)
                              // lim_total_r=diag_r;
                               // disty_c=lim_total_r;
                          //  else
                               // disty_c=diag_r;

                            pfinalscale=finalscale;
                            pfscale_l=fscale_l;
                            pfscale_r=fscale_r;
                            cbbuff=0.005;
                            cbind=-1;
                            for(int cb=0;cb<caliblen2;cb++)
                            {
                                if(between(ratio_l, c2_ratiol[cb] - cbbuff,  c2_ratiol[cb] + cbbuff))
                                    if(between(ratio_r, c2_ratior[cb] - cbbuff,  c2_ratior[cb] + cbbuff))
                                        cbind=cb;

                            }
                            if(cbind==-1) {
                                finalscale = pfinalscale;
                                fscale_l=pfscale_l;
                                fscale_r=pfscale_r;
                            }
                            else {
                                fscale_l=diag_l/c2_diagl[cbind];
                                fscale_r=diag_r/c2_diagr[cbind];
                            }

                            //  distx_c =total_l - (diag_l+diag_c + diag_c/2);
                       //   disty_c= total_r - (diag_r+diag_c + diag_c/2);
                            moveddist=calculateDistance(precx, precy, centerx,centery);

                           // distx_c = moveddist * ((centerx-precx)/(Math.abs(centerx-precx)));
                           // disty_c = moveddist * ((centery-precy)/(Math.abs(centery-precy)));

                         //   distx_c=(centerx-precx)/total_l;
                            //disty_c=(centery-precy)/total_r;


//                          distx_c =(diag_l+diag_c + diag_c/2);
//                        disty_c= (diag_r+diag_c + diag_c/2);

//                            px= (int) (diag_l+diag_c/2) * dispwd / (int) (total_l) ;
//                            py= (int) (diag_r+diag_c/2) * dispht / (int) (total_r) ;


                         //   px= (int) distx_c * dispwd / (int) (total_l) ;
                          //  py= (int) disty_c * dispht / (int) (total_r) ;
                            px = prepx;
                            py=prepy;
                            double buff3=0.2;
                            //if(Math.abs(ratio_l-startratio_l)<0.2 && Math.abs(ratio_r-startratio_r)<buff3)
                    ////////diagonal movement
//                            if(change_l==1)
//                            {
                                scale=diag_r/total_r;
                                py= sty + boxsize*rows - (int) ( (distx_c - (gapwidth_l*fscale_l)) * (boxsize*(rows)) /  (lim_total_l*fscale_l) );

                                if (py>sty+boxsize*rows)
                            py=sty+boxsize*rows;
                                else if(py<sty)
                                    py=sty;
//                                orient=2; //horiz
//
//                            }
//                          //  px=stx;
//
//                            if(change_r==1) {
                                scale=diag_l/total_l;
                                px = stx + (int) ((disty_c - (gapwidth_r*fscale_r)) * (boxsize * (cols)) / (lim_total_r*fscale_r));
                            if (px>stx+boxsize*cols)
                                px=stx+boxsize*cols;
                            else if (px<stx)
                                px=stx;
//                                orient=1; //vert
//                            }
                           //py=sty;
                            //px=stx;

                       //     px=  prepx + (int)( (distx_c-pdistx) * (boxsize*(rows+1))  / (int) (total_l));
                         //   py= prepy + (int)( (disty_c-pdisty) * (boxsize*(cols+1))  / (int) (total_r)) ;




                            //px= ( prepx+px )/2;
                          // py= ( prepy+py )/2;
                           // px=dispwd-px;
                         //   py=dispht-py;

                          //   px = (int) ((len_c+wid_c)/2 * (dispwd) / (int)(total_l)) ;
                           //  py = (int) ((len_c+wid_c)/2 * (dispht) / (int)(total_r)) ;
                        // px = (int) ( centerx * dispwd / (int)(wid));
                        // py = (int) ( centery * dispht / (int)(height));
//                            px = (int) centerx * dispwd / (wid);
//                            py = (int) centery * dispht / (height);
                           // Log.e("change","\nYes");


                        }
                      else
                        {
                            px = prepx;
                            py=prepy;
                           // Log.e("change","\nNoooooo");
                        }

                       // Log.e("analysis1","Pxy:\t"+px+"\t"+py);
                       // Log.e("analysis2","cxy:\t"+centerx+"\t"+centery);
                      //  Log.e("analysis3","lxy:\t"+linear_acceleration[0]+"\t"+linear_acceleration[1]);


                        if(calib2==4) {

                            Log.e("Analysis1ratios", "\n\t" + ratio_l + "\t" + ratio_r);
                            Log.e("Analysis1coords:", "\n\t" + px + "\t" + py);
                            Log.e("Analysis1distances1:", "\n\t" + distx_c + "\t" + disty_c);

                            Log.e("Analysis1distances2:", "\n\t" + (distx_c - (gapwidth_l*fscale_l)) + "\t" +  (disty_c - (gapwidth_r*fscale_r)));
                            Log.e("predist::", "\n\tmovedist" + moveddist);
                            Log.e("Analysis1areas", "\n\t" + area_c + "\t" + area_l + "\t" + area_r);
                            Log.e("areas2", "\n\tn1 " + areas[0] + "\tn2 " + areas[1] + "\tn3 " + areas[2]);

                         //   Log.e("Analysis1 ratio", "\n\tRatio L" + ratio_l + "\tR" + ratio_r);
                        }
                    }


                  //  fcanv.drawCircle(500, 500, 10, paint);
                 //   fcanv.drawCircle(500 + mirror, 500, 10, paint);

                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.RED);

                  //  fcanv.drawCircle(px , py, 30, paint);
                    // fcanv.drawCircle(px + mirror, py, 30, paint);

                    Log.e("REGINFO", "\n\tx:" + px + "\ty:" + py);
                }
                else if(listct!=3 && (fulllistct >4||fulllistct<3))
                {
                    if(viewstate!=0 && viewtrigger>10 && calib2==1)
                    {
                        viewstate=1;
                        viewtrigger = 0;
                    }
                    px = prepx;
                    py=prepy;
                }
                else
                {
                    px = prepx;
                    py=prepy;

                }

                ImageView fulliv = (ImageView) findViewById(R.id.imageView3);
                fulliv.setImageBitmap(bm);
               // fcanv.drawCircle(500, 500, 10, paint);
              //  fcanv.drawCircle(500 + mirror, 500, 10, paint);
                fcanv.drawText("L: "+change_l, dispwd-200, 25, tpaint);
                fcanv.drawText("R: "+change_r, dispwd-200, 60, tpaint);

                if(System.currentTimeMillis()-ansstarttime > 500 && System.currentTimeMillis()-clickdwellstart> 300) {
                    if (currdgt == 7 || currdgt == 8 || currdgt == 9|| currdgt == 1|| currdgt == 4) {
                        if ((listct == 2 || listct == 3) && viewstate != 2 && calib2 == 1)
                            detectClick();
                    } else

                    {
                        if (listct == 3 && viewstate != 2 && calib2 == 1)
                            detectClick();
                    }
                }

                if(viewstate!=2 )
                {
//                    if((listct!=3) && !(currdgt==7 || currdgt==8 || currdgt==9 ))
//                    {
//                        activecol=Color.parseColor("#4286f4"); //grey
//                    }
//                    else
//                    {
//                        activecol=Color.parseColor("#74e084"); //green
//                    }
                  //  if(swing==qct)
                      //  ansstarttime=System.currentTimeMillis();

                    swingtime++;
                    if(calib2==1) {
                        drawGrid(fcanv);

                        playGame(fcanv);
                    }

                }

           else if(viewstate==2 && calib2==1)
           {
               drawQuestion(fcanv);
               if(swing<qct)
               {
                   swing++;
                   travelrec=0;
                   Log.e("Swing","\t"+swingtime);
                   swingtime=0;
               }

           }

                if(detected==1) { ////cursor
                    fcanv.drawCircle(px, py, 25, paint);
                    fcanv.drawCircle(px + mirror, py, 25, paint);

//                    if(orient==1)
//                    {
//                        drawSlider(orient,py, fcanv);
//                    }
//                    else
//                    {
//                        drawSlider(orient,px, fcanv);
//                    }

                }
           //     fcanv.drawCircle((px + prepx)/2, (py+prepy)/2, 30, paint);




//                if(frame_ct!=0) {
//                    prepx=px;
//                    prepy=py;
//                }
//                else {
//
//                        frame_ct++;
//                    }

                assignPrevVals();


            }
            }

        );






        Imgproc.threshold(m, blank, 255,255, 1);

        return blank;
    }


    Mat detectSkin(Mat mRgb)
    {
        Mat hsv=new Mat();

        Mat img2=new Mat();


        Imgproc.cvtColor(mRgb, hsv, Imgproc.COLOR_RGB2YCrCb );

        List<Mat> channels = new ArrayList<Mat>(3);
        Core.split(mRgb, channels);
        Mat R = channels.get(0);
        Mat G = channels.get(1);
        Mat B = channels.get(2);

        Mat result1=new Mat();
        Mat result2=new Mat();
        Mat result3=new Mat();


        //Imgproc.threshold(G, result, 152 , 255 , 1);

        //Imgproc.threshold(G, result, 132 , 255 , 1);
        Imgproc.cvtColor(mRgb, img2, Imgproc.COLOR_BGR2YCrCb );

        List<Mat> channels_hsv = new ArrayList<Mat>(3);
        Core.split(img2, channels_hsv);

        Mat cy = channels_hsv.get(0);
        Mat cb = channels_hsv.get(1);
        Mat cr = channels_hsv.get(2);

        Imgproc.threshold(cr, result1, 135 , 173 , 1);
        Imgproc.threshold(cb, result2, 76 , 126 , 1);
        //Core.add(result1,result2,result3);
        List<Mat> listMat = Arrays.asList(cy, result2, result1);
        Core.merge(listMat, result3);
     //   Imgproc.threshold(result3, result3, 100 , 255 , 1);


//        Imgproc.cvtColor(result1, result1, Imgproc.COLOR_YCrCb2BGR );
  //      Imgproc.cvtColor(result2, result2, Imgproc.COLOR_YCrCb2BGR );
        Imgproc.cvtColor(result3, result3, Imgproc.COLOR_YCrCb2BGR );

     //   Imgproc.cvtColor(result3, result3, Imgproc.COLOR_BGR2GRAY );



        return result3;




        //Imgproc.adaptiveThreshold(G, result, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);
//        Mat src=mRgb;
//
//        Imgproc.blur( src, src, Size(3,3) );
//
//        Mat hsv=new Mat();
//        Imgproc.cvtColor(src, hsv, COLOR_BGR2HSV);
//        Mat bw;
//        inRange(hsv, Scalar(0, 10, 60), Scalar(20, 150, 255), bw);




    }

    public class Region {
        private Rect bounding;
        private Point centroid;

        public Region(Rect bounding, Point centroid) {
            this.bounding = bounding;
            this.centroid = centroid;
        }

        public Rect getBounding() {
            return bounding;
        }

        public Point getCentroid() {
            return centroid;
        }
    }

    public boolean between(double i, double minValueInclusive, double maxValueInclusive) {
        if (i >= minValueInclusive && i <= maxValueInclusive)
            return true;
        else
            return false;
    }
    public boolean between(int i, int minValueInclusive, int maxValueInclusive) {
        if (i >= minValueInclusive && i <= maxValueInclusive)
            return true;
        else
            return false;
    }
    public double calculateDistance(double a, double b, double c, double d)
    {
        double Sum = 0.0;
        double[] array1=new double[2];
        array1[0]=a;
        array1[1]=b;

        double[] array2=new double[2];
        array2[0]=c;
        array2[1]=d;


        for(int i=0;i<array1.length;i++) {
            Sum = Sum + Math.pow((array1[i]-array2[i]),2.0);
        }
        return Math.sqrt(Sum);
    }


    void assignPrevVals()
    {
        int c;
        precx=centerx;
        precy=centery;
        prepx=px;
        prepy=py;

        ptotal_l=total_l;
        ptotal_r=total_r;
        pratio_l=ratio_l;
        pratio_r=ratio_r;

        pdiag_l=diag_l;
        pdiag_r=diag_r;

      //  if(area_c-parea_c>changethresh)
       // {
            if (listct==3 && changect<changelevel)
            {
                areachange[changect]=area_c-parea_c;
                areac_l[changect]=area_l;
                areac_r[changect]=area_r;

                arealist[changect]= area_c;

                centerlist[changect][0]=centerx;
                centerlist[changect][1]=centery;
                changect++;



            }
            else
            {
                for(c=0;c<changelevel-1;c++)
                {
                    areachange[c]=areachange[c+1];
                    areac_l[c]=areac_l[c+1];
                    areac_r[c]=areac_r[c+1];
                    arealist[c]=arealist[c+1];
                    centerlist[c][0]=centerlist[c+1][0];
                    centerlist[c][1]=centerlist[c+1][1];

                }
                areachange[c]=area_c-parea_c;
                areac_l[c]=area_l;
                areac_r[c]=area_r;
                arealist[c]=area_c;
                centerlist[c][0]=centerx;
                centerlist[c][1]=centery;

            }

            if(listct==3)
        Log.e("Center x y","x:\t"+(centerx)+"\ty:\t"+(centery));
      //  }

        parea_c= area_c;
        parea_l=area_l;
        parea_r=area_r;

        pdistx=distx_c;
        pdisty=disty_c;

    }

    Boolean checkForChange()
    {
        double buff=0.4, buff2=0.005;
        change_l=0;
        change_r=0;
        double lchange=Math.abs(diag_l-pdiag_l);
        double rchange=Math.abs(diag_r-pdiag_r);
       // if(Math.abs(pratio_l-ratio_l)>=buff2)
        if(lchange>=rchange)
            change_l=1;
        else
       // if( Math.abs(pratio_r-ratio_r)>=buff2)
            change_r=1;

        if(Math.abs(pratio_l-ratio_l)>buff || Math.abs(pratio_r-ratio_r)>buff)
        {

            return true;
        }
        else
            return false;
    }

    void drawGrid(Canvas canv)
    {
        float leftx, boty;
        leftx=stx - 70;
        boty=sty + boxsize*rows + 70;
        float gridsize=strokewd+boxsize*rows;
        Paint p1= new Paint();
         pt= new Paint();

        pt.setColor(Color.BLACK);
        pt.setTextSize(75);

        p1.setStyle(Paint.Style.STROKE);
        p1.setColor(Color.parseColor("#427df4")); //BRIGHT BLUE
        int bx=0, by=0;
        int ty, tx, my=0, mx=0;
        ty=sty;
        tx=stx;
        if((listct==3)||((currdgt==7 || currdgt==8 || currdgt==9 ) && (listct==2))) {
            if (py > boty - 70 - boxsize / 2) {
                botCol = dangercol;
            } else {
                botCol = activecol;
            }
            if (px < leftx + 70 + boxsize / 2) {
                LCol = dangercol;
            } else {
                LCol = activecol;
            }
            topCol = activecol;
            RCol = activecol;

        }
        else
        {
            botCol=fadedcol;
            topCol=fadedcol;
            LCol=fadedcol;
            RCol=fadedcol;

            fcanv.drawText("Fingers out of view!", 275, 145, tpaint);
            fcanv.drawText("Fingers out of view!", 275 + mirror, 145, tpaint);
        }

        ptop.setColor(topCol);
        pbot.setColor(botCol);
        pleft.setColor(LCol);
        pright.setColor(RCol);


       // my=sty;
       // mx=stx + mirror;
        int gridno=0;





        for(int i=0; i<cols;i++)
        {
           // gridno++;
            tx=stx;
         //   canv.drawText(""+gridno,tx+boxsize/2,ty+boxsize/2,pt);
            for(int j=0;j<rows;j++)
            {
                gridno++;
                bx=tx+boxsize;
                by=ty+boxsize;

                mx=tx+boxsize + mirror;
                my=ty+boxsize;


                if(between(px,tx,bx) && between(py,ty,by))
                {
                    if(travelrec==0) {
                        traveltime = System.currentTimeMillis() - ansstarttime;
                        travelrec=gridno;
                        clickdwellstart=System.currentTimeMillis();

                    }

                    p1.setStyle(Paint.Style.FILL);
                    if(click==true && clickct>=clickgap)
                        //|| between(clicktime,1,clickpersist))
                    {
                        p1.setColor(Color.parseColor("#f7ff28")); //YELLOW
                        currans=gridno;
//                        if(qct-ansct==1) {
//                            ansct++;
//                            genQues();
//                        }
                        click=false;
                        clicktime++;
                        clickct=1;


                    }

                    else {



                        p1.setColor(Color.parseColor("#42f489")); //LIGHT GREEN
                    }

                    if(between(clickct,1,clickgap))
                    {
                        clickct++;
                    }
                }
                else
                {
                    if(travelrec==gridno)
                        travelrec=0;
                    p1.setStyle(Paint.Style.STROKE);
                    p1.setColor(Color.parseColor("#427df4")); //BRIGHT BLUE
                }

                canv.drawRect(tx,ty,bx,by,p1);
                canv.drawText(""+gridno,tx+boxsize/2,ty+boxsize/2,pt);

                canv.drawRect(tx + mirror,ty,mx,my,p1);
                canv.drawText(""+gridno,tx+boxsize/2 + mirror,ty+boxsize/2,pt);
           //   canv.drawText(""+gridno,tx+boxsize/2,ty+boxsize/2,pt);
                tx=tx+boxsize;
            }
            ty=ty+boxsize;
        }
        canv.drawLine(stx-strokewd,sty-strokewd/2,stx+gridsize,sty-strokewd/2,ptop);
        canv.drawLine(stx-strokewd,sty +gridsize-strokewd/2,stx+gridsize,sty+gridsize-strokewd/2,pbot);
        canv.drawLine(stx-strokewd/2,sty-strokewd,stx-strokewd/2,sty+gridsize,pleft);
        canv.drawLine(stx+gridsize-strokewd/2,sty-strokewd,stx+gridsize-strokewd/2,sty+gridsize,pright);

        canv.drawLine(stx-strokewd+ mirror,sty-strokewd/2,stx+gridsize+ mirror,sty-strokewd/2,ptop);
        canv.drawLine(stx-strokewd+ mirror,sty +gridsize-strokewd/2,stx+gridsize+ mirror,sty+gridsize-strokewd/2,pbot);
        canv.drawLine(stx-strokewd/2 + mirror,sty-strokewd,stx-strokewd/2 + mirror,sty+gridsize,pleft);
        canv.drawLine(stx+gridsize-strokewd/2 + mirror,sty-strokewd,stx+gridsize-strokewd/2 + mirror,sty+gridsize,pright);
      //  drawBorders(canv);



        if(currans!=0) {
            canv.drawText("" + currans, dispwd / 3, 150, pt);
            canv.drawText("" + currans, dispwd / 3 + mirror, 150, pt);
        }

        pt.setTextSize(70);

        lookchance=qct%3;
        canv.drawText("Type " + currdgt , 300, 200, pt);
        canv.drawText("Type " + currdgt  , 300 + mirror, 200, pt);



    }

    void drawQuestion(Canvas canv)
    {


        canv.drawText("Type "+currdgt,dispwd/7,500,qpt);
     //   canv.drawText("Your answer: "+currans,dispwd/6,400,qpt);

        canv.drawText("Type "+currdgt,dispwd/7 + mirror,500,qpt);
       // canv.drawText("Your answer "+currans,dispwd/6 + mirror,400,qpt);

    }
    void detectClick()
    {
       // traveltime=System.currentTimeMillis()-ansstarttime;

        if (changect>=changelevel) {

            if ( clickCondition()==true ) {
                {
                    click = true;
                    ansstarttime=System.currentTimeMillis();

                //    if(clicktime>=clickpersist)
                       // clicktime=0;

                }
            } else
                click= false;
        }


    }


/* Boolean clickCondition()
    {
        double buff=0.1, buff2=0.005;
        int lessThresh=changethresh/2;
        int changeflg=0;

  //     if(areachange[changelevel-1] - areachange[changelevel/2-1] < changethresh && areachange[changelevel/2-1] - areachange[0] > changethresh )
   // if(areachange[changelevel-1] - areachange[changelevel/2-1] < -changethresh && areachange[changelevel/2-1] - areachange[0] > changethresh )
      if(Math.abs(pratio_l-ratio_l)<buff && Math.abs(pratio_r-ratio_r)<buff) {

          for(int ac=0;ac<changelevel/2-1;ac++)
          {
              if (arealist[ac]<arealist[ac+1])
                  changeflg=1;
          }
          for(int ac=changelevel/2;ac<changelevel-1;ac++)
          {
              if (arealist[ac]>arealist[ac+1])
                  changeflg=1;
          }

          if(changeflg==0)
            if(checkratio()==true)
                if (swing == qct)
                   // if (areachange[changelevel - 1] - areachange[changelevel - 2] > changethresh)
                    return true;



//          if (areachange[changelevel - 1] - areachange[changelevel - 2] > changethresh) {
//              if (areac_l[changelevel - 1] - areac_l[changelevel - 2] > lessThresh)
//                  if (areac_r[changelevel - 1] - areac_r[changelevel - 2] > lessThresh)
//                      if (swing == qct)
//                          return true;

      }

        return false;
    }
    */



  //  Boolean clickCondition()
  //  {
//        double buff=0.4, buff2=0.005;
//        int lessThresh=10000;
//
//        //     if(areachange[changelevel-1] - areachange[changelevel/2-1] < changethresh && areachange[changelevel/2-1] - areachange[0] > changethresh )
//        // if(areachange[changelevel-1] - areachange[changelevel/2-1] < -changethresh && areachange[changelevel/2-1] - areachange[0] > changethresh )
//     if(Math.abs(pratio_l-ratio_l)<buff && Math.abs(pratio_r-ratio_r)<buff )
//            if(arealist[changelevel-3]<arealist[changelevel-2] && arealist[changelevel-2]> (arealist[changelevel-1]))
//         //   if(areachange[changelevel-1] - areachange[0] > changethresh )
//                if(arealist[changelevel-2] - arealist[0] > changethresh)
//            {
//               if(areac_l[changelevel-1] - areac_l[0] >lessThresh)
//               if(areac_r[changelevel-1] - areac_r[0] >lessThresh)
//                     if(swing==qct && swingtime>swinggap)
//                            return true;
//            }
//
//        return false;
//    }

 Boolean clickCondition()
    {

        int thresh=70;
        double updiffx, updiffy, dndiffx, dndiffy;
        int len=changelevel,half=changelevel/2;
        updiffx=centerlist[half-1][0]-centerlist[0][0];
        updiffy=centerlist[half-1][1]-centerlist[0][1];

        dndiffx=centerlist[len-1][0]-centerlist[half-1][0];
        dndiffy=centerlist[len-1][1]-centerlist[half-1][1];

       // if(checkForChange()==false)
        if(currdgt==travelrec)
                if(System.currentTimeMillis()-clickdwellstart>clickdwellgap)
                    if(checkratio()==true)
                return true;


        return false;
    }

    Boolean checkratio()
    {
        int i=0, wrongratio=0;
        double ratio_l=1, ratio_r=1, pratio_l=-1, pratio_r=-1, buff=0.55, firstratio_l=1, firstratio_r=1;

        if (arealist[0] != 0) {
            firstratio_l = arealist[0] / areac_l[0];
            firstratio_r = arealist[0] / areac_r[0];
        }
        for(i=0;i<changelevel;i++)
        {
            if (arealist[i] != 0)
            {
                ratio_l=arealist[i]/areac_l[i];
                ratio_r=arealist[i]/areac_r[i];
                if(pratio_l==-1)
                {
                    pratio_l=ratio_l;
                    pratio_r=ratio_r;
                }

                if(Math.abs(ratio_l-pratio_l) >buff || Math.abs(ratio_r-pratio_r) >buff)
                    wrongratio++;

            }
        }
        if(wrongratio<5 && (Math.abs(ratio_l-firstratio_l) <buff && Math.abs(ratio_r-firstratio_r) <buff))
            return true;

        else

        return false;
    }
    void genQues()
    {
        Random rand;
        rand = new Random();
        precurrdgt=currdgt;
        if(qct<qlimit)
            currdgt=questions[qct];
        else
            currdgt = rand.nextInt(9) + 1;
        quesorder++;
        if(currdgt>9 || currdgt<1)
        currdgt = rand.nextInt(9) + 1;
        qct++;
        currans=0;

        //fcanv.drawText("Calibrating", 300, 125, tpaint);

    }

  /*  void playGame(Canvas canv)
    {
        Random rand;
        rand = new Random();
        bpx = rand.nextInt(dispwd/2) + dispwd/10;
        bpy = rand.nextInt(dispht/2) + dispht/10;
        Paint baskpt=new Paint();
        baskpt.setStyle(Paint.Style.STROKE);
        baskpt.setStrokeWidth(20);
        baskpt.setColor(Color.BLUE);

        canv.drawRect(bpx-basksize/2, bpy-basksize/2, bpx+basksize/2, bpy+basksize/2,baskpt);
    }
    */
  void playGame(Canvas canv)
    {
//        Random rand;
//
//
//        if(qct==ansct && qct<qlimit )
//        {
//            rand = new Random();
//            currdgt = rand.nextInt(9) + 1;
//            qct++;
//            currans=0;
//        }


        canv.drawText(""+currdgt,dispwd/3,100,pt);
        canv.drawText(""+currans,dispwd/3,150,pt);
//
        canv.drawText(""+currdgt,dispwd/3 + mirror,100,pt);
        canv.drawText(""+currans,dispwd/3 + mirror,150,pt);



        if(currans!=0)
        {
            anstime=System.currentTimeMillis()-ansstarttime;
            ansct++;
            Log.e("log_peranstime","\tQ"+ansct+"\t"+"\tQ: "+currdgt+"\tA: "+currans+"\t"+anstime+"\t"+traveltime+"\tCorrect");

           if(currans==currdgt)
           { //if(qct<=qlimit)
               score++;
              // Log.e("log_peranstime","\tQ"+ansct+"\t"+"\tQ: "+currdgt+"\tA: "+currans+"\t"+anstime+"\t"+traveltime+"\tCorrect");
           }
           else
           {
              // Log.e("log_peranstime","\tQ"+ansct+"\t"+"\tQ: "+currdgt+"\tA: "+currans+"\t"+anstime+"\t"+traveltime+"\tWrong");
           }
            genQues();
        }
        if(ansct==qlimit)
        {
            completeTime= System.currentTimeMillis() - startTime;
            Log.e("log_score", "Score:\t\t" + score);
            Log.e("log_time", "Time:\t\t" + completeTime);
            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ansct++;
        }

//        canv.drawText("Score: "+score+"/"+qlimit,300,50,pt);
//        canv.drawText("Score: "+score+"/"+qlimit,300+mirror,50,pt);


//        canv.drawText("Q "+qct,dispwd-dispwd/4,50,pt);
//        canv.drawText("A "+ansct,dispwd-dispwd/4,120,pt);

    }

    void drawSlider(int orient, int coord, Canvas canv)
    {
        //1=vertical, 2=horizontal
        float w=30, l=50;
        float leftx, topy;
        leftx=stx - 70;
        topy=sty + boxsize*rows + 70;

        Paint sliderpt=new Paint();
        Paint normslider=new Paint();
        Paint warnslider=new Paint();
        Paint linept = new Paint();
        linept.setStyle(Paint.Style.STROKE);
        linept.setColor(Color.parseColor("#787878")); //Grey
        linept.setStrokeWidth(7);

        sliderpt.setStyle(Paint.Style.FILL_AND_STROKE);

        normslider.setStyle(Paint.Style.FILL_AND_STROKE);
        warnslider.setStyle(Paint.Style.FILL_AND_STROKE);


       // if((orient==1 && coord > topy-70-boxsize/2) || (orient==2 && coord > leftx+70+ boxsize*rows - boxsize/2))
            warnslider.setColor(Color.parseColor("#A14B4B")); //Red
       // else
            normslider.setColor(Color.parseColor("#71CD11")); //Green
        //A14B4B // red

        canv.drawLine(leftx+(w/2), sty, leftx+(w/2), topy+(w/2)+7, linept); //vert
        canv.drawLine(leftx+(w/2), topy + (w/2), leftx +(boxsize*cols) + 70, topy + (w/2), linept);

        if(orient==1) //vertical
        {
            if(coord > topy-70-boxsize/2)
                sliderpt=warnslider;
            else
                sliderpt=normslider;
            canv.drawRect(leftx,coord,leftx+w,coord+l,sliderpt);
            canv.drawRect(px,topy,px+l,topy+w,normslider);

        }
        else //horiz
        {
            if(coord < leftx+70+ boxsize/2)
                sliderpt=warnslider;
            else
                sliderpt=normslider;
            canv.drawRect(coord,topy,coord+l,topy+w,sliderpt);
            canv.drawRect(leftx,py,leftx+w,py+l,normslider);
        }
    }
    //void drawRect(float left, float top, float right, float bottom, Paint paint)

//    void drawRect(int x, int y, int w, int h)
//    {
//
//    }



}
