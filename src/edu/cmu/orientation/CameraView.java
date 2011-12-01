package edu.cmu.orientation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import edu.cmu.image.filters.AndroidUtils;
import edu.cmu.image.filters.ContrastFilter;
import edu.cmu.image.filters.LaplaceFilter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class CameraView extends Activity implements SurfaceHolder.Callback, OnClickListener {
	static final int FOTO_MODE = 0;
	private static final String TAG = "CameraTest";
	Camera mCamera;
	Button exitBtn;
	TextView orientationText;
	public static Handler guiHandler;
	boolean mPreviewRunning = false;
	private Context mContext = this;
	MyLocationService myLocService;

	int frontFacingCameraId;
	List<Size> mSupportedPreviewSizes;
	Size mPreviewSize;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Log.e(TAG, "onCreate");

		Bundle extras = getIntent().getExtras();

		setContentView(R.layout.main);
		exitBtn = (Button) findViewById(R.id.exit);
		exitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopCamera();
				finish();
				System.exit(0);
			}
		});
		orientationText = (TextView)findViewById(R.id.orientation);
		createGUIHandler();
		myLocService = new MyLocationService((LocationManager)getSystemService(Context.LOCATION_SERVICE));
		mSurfaceView = (SurfaceView) findViewById(R.id.camera_view);
		mSurfaceView.setOnClickListener(this);
		
		//Initiate the Surface Holder properly
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		getOrientation();
	}

	private void getOrientation() {
		new Thread() {
			public void run() {
				while(true){
					try {
						Float trueNorth = myLocService.getTrueNorthAngleDiff();
						sendMsg(trueNorth.toString());
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		
	}

	public static void sendMsg(String msg) {
		Message m = new Message();
		m.obj = msg;
		guiHandler.sendMessage(m);
	}
	
	private void createGUIHandler() {
		guiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				orientationText.setText("Orientation: " + msg.obj.toString());
			}
		};
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {

			if (imageData != null) {

				Intent mIntent = new Intent();

				StoreByteImage(mContext, imageData, 50, "ImageName");
				mCamera.startPreview();

				setResult(FOTO_MODE, mIntent);
				// finish();

			}
		}
	};

	@Override
	protected void onPause() {
		stopCamera();
		super.onPause();
	}

	private void stopCamera() {
		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mCamera.stopPreview();
			mPreviewRunning = false;
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onDestroy() {
		stopCamera();
		super.onDestroy();
	}

	protected void onResume() {
		Log.e(TAG, "onResume");
		mCamera = openFrontFacingCamera();
		super.onResume();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected void onStop() {
		stopCamera();
		Log.e(TAG, "onStop");
		super.onStop();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "surfaceCreated");
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			if (mSupportedPreviewSizes != null) {
//				Camera.Parameters p = mCamera.getParameters();
//				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
//						mSurfaceView.getWidth(), mSurfaceView.getHeight());
//				p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//				mCamera.setParameters(p);				
		        mCamera.setDisplayOrientation(90);
				try {
					mCamera.setPreviewDisplay(holder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Camera openFrontFacingCamera() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
					frontFacingCameraId = camIdx;
				} catch (RuntimeException e) {
					Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}

		return cam;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.e(TAG, "surfaceChanged");
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();
//		mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, mSurfaceView.getHeight(),mSurfaceView.getWidth());
		p.setPreviewSize(w, h);
		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "surfaceDestroyed");
		if (mCamera != null) {
			mCamera.stopPreview();
			mPreviewRunning = false;
			mCamera.release();
		}
	}

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	public void onClick(View arg0) {
		mCamera.takePicture(null, mPictureCallback, mPictureCallback);
	}

	public boolean StoreByteImage(Context mContext, byte[] imageData, int quality, String expName) {

//		File sdImageMainDirectory = getApplicationContext().getFilesDir();
		File sdImageMainDirectory = Environment.getExternalStorageDirectory();
		FileOutputStream fileOutputStream = null;
		String nameFile;
		try {

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 5;

			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

			fileOutputStream = new FileOutputStream(sdImageMainDirectory.toString() + "/image.jpg");

			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
			LaplaceFilter laplaceFilter = new LaplaceFilter();
			int[] edgeImage = laplaceFilter.filter(AndroidUtils.bitmapToIntArray(myImage), myImage.getWidth(), myImage.getHeight());
			ContrastFilter contrastFilter = new ContrastFilter();
			contrastFilter.setContrast(1);
			int[] contrastImage = contrastFilter.filter(edgeImage, myImage.getWidth(), myImage.getHeight());
			Bitmap edgeBitmap = Bitmap.createBitmap(contrastImage, myImage.getWidth(), myImage.getHeight(), Config.ARGB_8888);
//			Canvas c = new Canvas(Bitmap.createBitmap(edgeImage, myImage.getWidth(), myImage.getHeight(), Config.ARGB_8888));
//			mSurfaceView.draw(c);
			String filenm = sdImageMainDirectory.toString() + "/image.jpg";
			orientationText.setText("saved in " + filenm);
			myImage.compress(CompressFormat.JPEG, quality, bos);
			bos.flush();
			bos.close();

		} catch(RuntimeException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
}