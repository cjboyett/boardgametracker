package com.cjboyett.boardgamestats;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.view.AdViewContainer;

import java.io.IOException;

public class PictureTestActivity extends AppCompatActivity {
	final static int REQUEST_CAMERA = 1;
	final static int SELECT_FILE = 2;

	private ImageView showImg;
	private int targetW, targetH;


	private ImageController imageController;
	private Uri imageUri;

	boolean canWrite, canUseCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_test);

//		showImg = (ImageView)findViewById(R.id.imageview_picture_test);
//		targetW = showImg.getWidth();
//		targetH = showImg.getHeight();

		imageController = new ImageController(this)
				.setDirectoryName("images")
				.setFileType("JPG")
				.setFileName("test.jpg")
				.setCompressionLevel(90);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
			if (!Preferences.hasAskedPermission(this)) {
				String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};
				requestPermissions(perms, 200);
				Preferences.setHasAskedPermission(this, true);
			}
		}

		canWrite = Preferences.canAccessStorage(this);
		canUseCamera = Preferences.canUseCamera(this);

		final AdViewContainer adViewContainer = (AdViewContainer) findViewById(R.id.ad_container);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (adViewContainer.getVisibility() == View.VISIBLE)
					adViewContainer.setVisibility(View.GONE);
				else
					adViewContainer.setVisibility(View.VISIBLE);
/*
				if (canUseCamera && canWrite)
				{
					final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

					AlertDialog.Builder builder = new AlertDialog.Builder(PictureTestActivity.this);
					builder.setTitle("Add Photo!");
					builder.setItems(items, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int item)
						{
							if (items[item].equals("Take Photo"))
							{
								File photoFile = null;
								try
								{
									photoFile = imageController.createFile();
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
								if (photoFile != null)
								{
									ContentValues values = new ContentValues();
									values.put(MediaStore.Images.Media.TITLE, photoFile.getAbsolutePath());
									values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
									imageUri = getContentResolver().insert(
											MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

									Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
											.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
									startActivityForResult(intent, REQUEST_CAMERA);
								}
							} else if (items[item].equals("Choose from Library"))
							{
								Intent intent = new Intent(
										Intent.ACTION_PICK,
										android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
								intent.setType("image*/
/*");
								startActivityForResult(
										Intent.createChooser(intent, "Select File"),
										SELECT_FILE);
							} else if (items[item].equals("Cancel"))
							{
								dialog.dismiss();
							}
						}
					});
					builder.show();
				}
*/
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == 200) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Preferences.setCanAccessStoragePreference(this, true);
			} else Preferences.setCanAccessStoragePreference(this, false);
			if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				Preferences.setCanUseCameraPreference(this, true);
			} else Preferences.setCanUseCameraPreference(this, false);

			canWrite = Preferences.canAccessStorage(this);
			canUseCamera = Preferences.canUseCamera(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("PHOTO", "It's here");
		if (resultCode == RESULT_OK) {
			Log.d("PHOTO", "Okay");
			if (requestCode == REQUEST_CAMERA) {
				String imageId = convertImageUriToFile(imageUri, this);

				//  Create and excecute AsyncTask to load capture image
				new LoadImagesFromSDCard().execute("" + imageId);

/*
				Log.d("PHOTO", "Whatever");

				File destination = imageController.createFile();
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				File f = new File("file:" + destination.getAbsolutePath());
				Uri contentUri = Uri.fromFile(f);
				mediaScanIntent.setData(contentUri);
				this.sendBroadcast(mediaScanIntent);

				int targetW = ((ImageView)findViewById(R.id.imageview_picture_test)).getWidth();
				int targetH = ((ImageView)findViewById(R.id.imageview_picture_test)).getHeight();
				Bitmap bm = imageController.load();//loadBitmapFromPath(destination.getAbsolutePath());
				Log.d("BITMAP", (bm == null) + "");

				Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, targetW, targetH, false);

				((ImageView)findViewById(R.id.imageview_picture_test)).setImageBitmap(scaledBitmap);

				try
				{
					Log.d("BITMAP BEFORE DELETE", (imageController.load() == null) + "");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				imageController.delete();
				try
				{
					Log.d("BITMAP AFTER DELETE", (imageController.load() == null) + "");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
*/
			} else if (requestCode == SELECT_FILE) {
				Uri selectedImageUri = data.getData();
				String[] projection = {MediaStore.MediaColumns.DATA};

				Cursor cursor = this.getContentResolver().query(selectedImageUri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
				cursor.moveToFirst();
				String selectedImagePath = cursor.getString(column_index);
				cursor.close();

				int targetW = findViewById(R.id.imageview_picture_test).getWidth();
				int targetH = findViewById(R.id.imageview_picture_test).getHeight();
				Bitmap bm = loadBitmapFromPath(selectedImagePath);

				Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, targetW, targetH, false);

				((ImageView) findViewById(R.id.imageview_picture_test)).setImageBitmap(scaledBitmap);
			}
		}
	}

	private Bitmap loadBitmapFromPath(String filePath) {
		Log.d("FILE LOCATION", filePath);
		Bitmap bm;
		int targetW = findViewById(R.id.imageview_picture_test).getWidth();
		int targetH = findViewById(R.id.imageview_picture_test).getHeight();

		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		bm = BitmapFactory.decodeFile(filePath, bmOptions);

		ExifInterface exifInterface = null;
		try {
			exifInterface = new ExifInterface(filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (exifInterface != null) {
			bm = rotateBitmap(bm,
							  exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
															ExifInterface.ORIENTATION_UNDEFINED));
		}

		return bm;
	}

	private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

		Matrix matrix = new Matrix();
		switch (orientation) {
			case ExifInterface.ORIENTATION_NORMAL:
				return bitmap;
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				matrix.setScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				matrix.setRotate(180);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.setRotate(90);
				break;
			case ExifInterface.ORIENTATION_TRANSVERSE:
				matrix.setRotate(-90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				matrix.setRotate(-90);
				break;
			default:
				return bitmap;
		}
		try {
			Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle();
			return bmRotated;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

	/************
	 * Convert Image Uri path to physical path
	 **************/

	public static String convertImageUriToFile(Uri imageUri, Activity activity) {

		Cursor cursor = null;
		int imageID = 0;

		try {

			/*********** Which columns values want to get *******/
			String[] proj = {
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID,
					MediaStore.Images.Thumbnails._ID,
					MediaStore.Images.ImageColumns.ORIENTATION
			};

			cursor = activity.managedQuery(

					imageUri,         //  Get data for specific image URI
					proj,             //  Which columns to return
					null,             //  WHERE clause; which rows to return (all rows)
					null,             //  WHERE clause selection arguments (none)
					null              //  Order-by clause (ascending by name)

			);

			//  Get Query Data

			int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
			int columnIndexThumb = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
			int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

			//int orientation_ColumnIndex = cursor.
			//    getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);

			int size = cursor.getCount();

			/*******  If size is 0, there are no images on the SD Card. *****/

			if (size == 0) {


//				imageDetails.setText("No Image");
			} else {

				int thumbID = 0;
				if (cursor.moveToFirst()) {

					/**************** Captured image details ************/

					/*****  Used to show image on view in LoadImagesFromSDCard class ******/
					imageID = cursor.getInt(columnIndex);

					thumbID = cursor.getInt(columnIndexThumb);

					String Path = cursor.getString(file_ColumnIndex);

					//String orientation =  cursor.getString(orientation_ColumnIndex);

					String CapturedImageDetails = " CapturedImageDetails : \n\n"
							+ " ImageID :" + imageID + "\n"
							+ " ThumbID :" + thumbID + "\n"
							+ " Path :" + Path + "\n";

					// Show Captured Image detail on activity
//					imageDetails.setText( CapturedImageDetails );

				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		// Return Captured Image ImageID ( By this ImageID Image will load from sdcard )

		return "" + imageID;
	}


	/**
	 * Async task for loading the images from the SD card.
	 *
	 * @author Android Example
	 */

	// Class with extends AsyncTask class

	public class LoadImagesFromSDCard extends AsyncTask<String, Void, Void> {

		private ProgressDialog Dialog = new ProgressDialog(PictureTestActivity.this);

		Bitmap mBitmap;

		protected void onPreExecute() {
			/****** NOTE: You can call UI Element here. *****/

			// Progress Dialog
			Dialog.setMessage(" Loading image from Sdcard..");
			Dialog.show();
		}


		// Call after onPreExecute method
		protected Void doInBackground(String... urls) {

			Bitmap bitmap = null;
			Bitmap newBitmap = null;
			Uri uri = null;


			try {

				/**  Uri.withAppendedPath Method Description
				 * Parameters
				 *    baseUri  Uri to append path segment to
				 *    pathSegment  encoded path segment to append
				 * Returns
				 *    a new Uri based on baseUri with the given segment appended to the path
				 */

				uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + urls[0]);

				/**************  Decode an input stream into a bitmap. *********/
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

				if (bitmap != null) {

					/********* Creates a new bitmap, scaled from an existing bitmap. ***********/

					int width = targetW > 0 ? targetW : 170;
					int height = targetH > 0 ? targetH : 170;
					newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

//					newBitmap = Bitmap.createScaledBitmap(bitmap, 170, 170, true);

					bitmap.recycle();

					if (newBitmap != null) {

						mBitmap = newBitmap;
					}
				}
			} catch (IOException e) {
				// Error fetching image, try to recover

				/********* Cancel execution of this task. **********/
				cancel(true);
			}

			return null;
		}


		protected void onPostExecute(Void unused) {

			// NOTE: You can call UI Element here.

			// Close progress dialog
			Dialog.dismiss();

			if (mBitmap != null) {
				// Set Image to ImageView

				showImg.setImageBitmap(mBitmap);
			}

		}

	}

	public static class ImageTestActivity extends AppCompatActivity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_image_test);

			View image = findViewById(R.id.imageview_test);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Log.d("NAME", image.getTransitionName());
				//			image.setTransitionName("test");
			}
		}

	}
}
