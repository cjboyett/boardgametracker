package com.cjboyett.boardgamestats.utility.data;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Casey on 9/6/2016.
 */
public class FileController
{
	private String directoryName = "files";
	private String fileName = "file.txt";
	private String fileType = "TXT";
	private Context context;

	public FileController(Context context) {
		this.context = context;
	}

	public FileController setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public FileController setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
		return this;
	}

	public FileController setFileType(String fileType)
	{
		this.fileType = fileType;
		return this;
	}

	public boolean exists()
	{
		return createFile().exists();
	}

	public void save(String file) {
		save(file.getBytes());
	}

	public void save(byte[] bytes) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(createFile());
			fileOutputStream.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@NonNull
	private File createFile() {
		File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
		return new File(directory, fileName);
	}

	public File load() {
		return createFile();
	}

	public void delete()
	{
		try
		{
			createFile().delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
