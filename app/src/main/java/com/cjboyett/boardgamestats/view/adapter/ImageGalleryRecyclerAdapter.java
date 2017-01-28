package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cjboyett.boardgamestats.PictureTestActivity;
import com.cjboyett.boardgamestats.utility.view.ImageController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 9/10/2016.
 */
public class ImageGalleryRecyclerAdapter extends RecyclerView.Adapter<ImageGalleryRecyclerAdapter.ViewHolder> {
	private Context context;
	private ImageController imageController;
	private Map<String, Bitmap> imageList;
	private List<String> keys;

	public ImageGalleryRecyclerAdapter(Context context, List<String> keys) {
		this.context = context;
		this.keys = keys;
		imageList = new HashMap<>();

		imageController = new ImageController(context).setDirectoryName("thumbnails");

		for (int i = 0; i < keys.size(); i++) {
			String imagePath = keys.get(i);
			new AsyncTask<String, Void, Bitmap>() {
				@Override
				protected Bitmap doInBackground(String... params) {
					Bitmap image;
					String imagePath = params[0];
					image = imageController.setFileName(imagePath).load();
					imageList.put(imagePath, image);
					return image;
				}

				@Override
				protected void onPostExecute(Bitmap bitmap) {
					notifyDataSetChanged();
				}
			}.execute(imagePath);
		}
	}

	public ImageGalleryRecyclerAdapter(Context context, Map<String, Bitmap> imageList) {
		this.context = context;
		this.imageList = imageList;
		keys = new ArrayList<>(imageList.keySet());
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ImageView imageView = new ImageView(context);
		imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
															 ViewGroup.LayoutParams.WRAP_CONTENT));
		imageView.setMinimumHeight(60);
//		imageView.setMinimumWidth(60);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		ViewHolder viewHolder = new ViewHolder(imageView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		String key = keys.get(position);
		holder.setImage(imageList.get(key));
		holder.setKey(key);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			holder.view.setTransitionName("test");
		}

		holder.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					Log.d("NAME", v.getTransitionName());
				}
				ActivityOptionsCompat transitionActivityOptions =
						ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, v, "test");
				((Activity) context).startActivity(new Intent(context, PictureTestActivity.ImageTestActivity.class),
												   transitionActivityOptions.toBundle());
//				ViewCompat.setTransitionName(icon, convertView.getContext().getString(R.string.demo));
			}
		});
	}

	@Override
	public int getItemCount() {
		return imageList.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		public ImageView view;
		public String key;

		public ViewHolder(ImageView view) {
			super(view);
			view.setOnClickListener(this);
			this.view = view;
		}

		public void setImage(Bitmap image) {
			view.setImageBitmap(image);
		}

		public void setKey(String key) {
			this.key = key;
		}

		@Override
		public void onClick(View v) {
			Toast.makeText(v.getContext(), key, Toast.LENGTH_LONG).show();
		}
	}
}
