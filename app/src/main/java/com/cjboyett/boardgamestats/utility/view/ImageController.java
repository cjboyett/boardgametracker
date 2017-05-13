package com.cjboyett.boardgamestats.utility.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by Ilya Gazman on 3/6/2016.
 */
public class ImageController {
	private String directoryName = "images";
	private String fileName = "image.png";
	private String fileType = "PNG";
	private int compress = 100;
	private Context context;

	public ImageController(Context context) {
		this.context = context;
	}

	public ImageController setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public ImageController setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
		return this;
	}

	public ImageController setFileType(String fileType) {
		this.fileType = fileType;
		return this;
	}

	public ImageController setCompressionLevel(int compressionLevel) {
		if (compressionLevel > 0 && compressionLevel <= 100)
			compress = compressionLevel;
		return this;
	}

	public void save(Bitmap bitmapImage) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(createFile());
			switch (fileType) {
				case "JPG":
				case "JPEG":
					bitmapImage.compress(Bitmap.CompressFormat.JPEG, compress, fileOutputStream);
					break;
				case "WEBP":
					bitmapImage.compress(Bitmap.CompressFormat.WEBP, compress, fileOutputStream);
					break;
				default:
					bitmapImage.compress(Bitmap.CompressFormat.PNG, compress, fileOutputStream);
					break;
			}
		} catch (Exception e) {
			Timber.e(e);
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				Timber.e(e);
			}
		}
	}

	@NonNull
	public File createFile() {
		File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
		return new File(directory, fileName);
	}

	public Bitmap load() {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(createFile());
			return BitmapFactory.decodeStream(inputStream);
		} catch (Exception e) {
			Timber.e(e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				Timber.e(e);
			}
		}
		return null;
	}

	public void delete() {
		try {
			createFile().delete();
		} catch (Exception e) {
			Timber.e(e);
		}
	}

	public void close() {
		context = null;
	}
}