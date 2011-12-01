package edu.cmu.orientation;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Divya Vavili
 * 
 */
public class CameraActivity extends Activity
{
	
	@Override
	public void displayMainScreen()
	{
		try
		{
			//render the main screen
			String layoutClass = this.getPackageName()+".R$layout";
			String main = "main";
			Class clazz = Class.forName(layoutClass);
			Field field = clazz.getField(main);
			int screenId = field.getInt(clazz);
			this.setContentView(screenId);
			
			ListView view = (ListView)findViewById(R.id.preview);
		    this.setTitle("Cloud Camera App");
		    
		    ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
			
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("empty", "");
			map.put("title", "Take a Picture");
			mylist.add(map);
			
			int rowId = findLayoutId(R.id.layout);
			String[] rows = new String[]{"empty","title"};
			int[] rowUI = new int[] {ViewHelper.findViewId(this, "empty"), ViewHelper.findViewId(this, "title")};
			SimpleAdapter listAdapter = new SimpleAdapter(this, mylist, rowId, rows, rowUI);
		    view.setAdapter(listAdapter);
		    
		    view.setOnItemClickListener(new OnItemClickListener(){
		    	public void onItemClick(AdapterView<?> parent, View view, int position,
						long id)
		    	{
		    		try
					{
						CameraActivity.this.showPreviewScreen();
					}
					catch(Exception e)
					{
						e.printStackTrace(System.out);
					}
		    	}
		    });
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
	}
	
	private void showPreviewScreen() throws Exception
	{
		//render the main screen
		String layoutClass = this.getPackageName()+".R$layout";
		String main = "camera_preview";
		Class clazz = Class.forName(layoutClass);
		Field field = clazz.getField(main);
		int screenId = field.getInt(clazz);
		this.setContentView(screenId);
		
		//Setup the FrameLayout with the Camera Preview Screen
		final CameraSurfaceView cameraSurfaceView = new CameraSurfaceView(this);
		FrameLayout preview = (FrameLayout)findViewById(R.id.preview); 
		preview.addView(cameraSurfaceView);
		
		//Setup the 'Take Picture' button to take a picture
		Button takeAPicture = (Button)findViewById(R.id.buttonClick);
		takeAPicture.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				Camera camera = cameraSurfaceView.getCamera();
				camera.takePicture(null, null, new HandlePictureStorage());
			}
		});
		
		//Setup the 'Upload to Cloud' button to take a picture
		Button upload = (Button)findViewById(R.id.upload);
		upload.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View button) 
			{
				try
				{
					//Upload share me to the cloud
					List<CloudPhoto> toUpload = new ArrayList<CloudPhoto>();
					toUpload.add(CameraActivity.this.shareme);
					
					//Get the CloudCamera service
					CloudCamera cloudCamera = CloudCamera.getInstance();
					String cameraCommand = "/share/photo";
					cloudCamera.syncWithCloud(cameraCommand, toUpload);
					
					ViewHelper.getOkModal(CameraActivity.this, "Success", "Upload successfull").show();
				}
				catch(Exception e)
				{
					e.printStackTrace(System.out);
					ViewHelper.getOkModal(CameraActivity.this, "Error", "Upload to Cloud Failed").show();
				}
			}
		});
		
		//Setup the 'Done' button to go back
		Button done = (Button)findViewById(R.id.buttonClick);
		done.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				try
				{
					CameraActivity.this.displayMainScreen();
				}
				catch(Exception e)
				{
					e.printStackTrace(System.out);
				}
			}
		});
	}
	
	private class HandlePictureStorage implements PictureCallback
	{

		@Override
		public void onPictureTaken(byte[] picture, Camera camera) 
		{
			//The picture can be stored or do something else with the data
			//in this callback such sharing with friends, upload to a Cloud component etc
			
			//This is invoked when picture is taken and the data needs to be processed
			System.out.println("Picture successfully taken: "+picture);
			
			String fileName = "shareme.jpg";
			String mime = "image/jpeg";
			
		}
	}
}
