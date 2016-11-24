package com.cjboyett.boardgamestats.activity;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.GameDownloadUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.ticker.Ticker;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends BaseActivity implements GestureDetector.OnGestureListener
{
	private Activity activity = this;
	private View view;
	private Ticker ticker;

	private AppCompatImageView achievementButton;
	private boolean animatingButton;

	private GestureDetectorCompat gestureDetector;

	private FirebaseAuth auth = FirebaseAuth.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		view = getLayoutInflater().inflate(R.layout.activity_main, null);
		setContentView(view);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		generateLayout();
		setColors();
		colorComponents();

		gestureDetector = new GestureDetectorCompat(this, this);

		Preferences.setSuperUser(this, true);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		setColors();
		colorComponents();

		String welcomeBack = "Welcome Back";
		String username = Preferences.getUsername(this);
		if (username != null && !username.equals("") && !username.equalsIgnoreCase("User")) welcomeBack += ", " + username + "!";
		else welcomeBack += "!";

		((TextView) findViewById(R.id.textview_welcome_back)).setText(welcomeBack);

		if (!Preferences.isFirstVisit(this)) ticker.start();

		if (Preferences.needAllPlayerTableUpgrade(this))
		{
			new AsyncTask<String, Void, Void>()
			{
				ProgressDialog progressDialog;

				@Override
				protected void onPreExecute()
				{
					progressDialog = new ProgressDialog(activity);
					progressDialog.setMessage("Updating database");
					progressDialog.setCancelable(false);
					progressDialog.show();
				}

				@Override
				protected void onPostExecute(Void aVoid)
				{
					progressDialog.dismiss();
				}

				@Override
				protected Void doInBackground(String... params)
				{
					GamesDbHelper dbHelper = new GamesDbHelper(activity);
					PlayersDbUtility.populateAllPlayersTable(dbHelper);
					Preferences.setNeedAllPlayerTableUpgrade(activity, false);
					dbHelper.close();

					return null;
				}
			}.execute("");
		}

		if (!animatingButton)
		{
			animatingButton = true;
//			animateAchievementButton();
		}

		Preferences.setSuperUser(this, false);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		ticker.pause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		ticker.stop();
//		auth.close(authStateListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (Preferences.useSwipes(this)) gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	void generateLayout()
	{
		ticker = (Ticker)findViewById(R.id.ticker);
		achievementButton = (AppCompatImageView)view.findViewById(R.id.imageview_achievements);

		view.findViewById(R.id.textview_add_game).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(v.getContext(), BoardGameListActivity.class));
				ActivityUtilities.exitLeft(activity);
			}
		});

		view.findViewById(R.id.textview_stats).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(v.getContext(), StatsTabbedActivity.class));
				ActivityUtilities.exitUp(activity);
			}
		});

		view.findViewById(R.id.textview_extras).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (auth.getCurrentUser() != null)
					startActivity(new Intent(v.getContext(), ExtrasActivity.class));
				else
					startActivity(new Intent(v.getContext(), LoginActivity.class));
				ActivityUtilities.exitRight(activity);
			}
		});

		view.findViewById(R.id.textview_add_game_play).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!Preferences.isTimerRunning(activity))
				{
					TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
					tempDataManager.initialize();
				}
				startActivity(new Intent(v.getContext(), AddGamePlayTabbedActivity.class)
						              .putExtra("EXIT", "UP"));
				ActivityUtilities.exitDown(activity);
			}
		});

		view.findViewById(R.id.imageview_settings).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(v.getContext(), SettingsActivity.class));
//				ActivityUtilities.exitRight(activity);
			}
		});

		view.findViewById(R.id.imageview_achievements).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(v.getContext(), AchievementsActivity.class));
//				ActivityUtilities.exitRight(activity);
			}
		});

		view.findViewById(R.id.imageview_help).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
						.setTitle("Help")
						.setMessage("This is where help will be placed in the future.")
						.setPositiveButton("Okay", null)
						.create();
				alertDialog.show();
			}
		});

		if (Preferences.isFirstVisit(this))
		{
			Preferences.setShowPopup(this, false);
			Preferences.setMetYancey(this, true);
			ViewUtilities.DialogBuilder syncBggDialogBuilder = new ViewUtilities.DialogBuilder(this);
			final EditText usernameEditText = syncBggDialogBuilder.getInput();
			final AlertDialog syncBggDialog = syncBggDialogBuilder.setTitle("Sync With BGG")
			                                                      .setMessage("Would you like me to download your collection and game plays from your Board Game Geek account?  This can also be done at any time through the Settings.")
			                                                      .setHintText("BGG Username")
			                                                      .withYancey(true)
			                                                      .setPositiveButton("Okay", new View.OnClickListener()
			                                                      {
				                                                      @Override
				                                                      public void onClick(View v)
				                                                      {
					                                                      String username = usernameEditText.getText()
					                                                                                        .toString();
					                                                      GameDownloadUtilities.syncWithBgg(v.getContext(), username);
				                                                      }
			                                                      })
			                                                      .setNegativeButton("Cancel", null)
			                                                      .create();

			final AlertDialog addToCollectionDialog = new ViewUtilities.DialogBuilder(this)
					.setTitle("Add a New Game?")
					.setMessage("If you have a Board Game Geek account I can download your game information.\n\n" +
					            "Or you can manually add a new game.")
					.withYancey(true)
					.setPositiveButton("Sync", new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							syncBggDialog.show();
						}
					})
					.setNeutralButton("Add a Game", new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							startActivity(new Intent(v.getContext(), AddBoardGameActivity.class));
							ActivityUtilities.exitDown(activity);
						}
					})
					.setNegativeButton("Later", null)
					.create();

			final ViewUtilities.DialogBuilder firstVisitDialog = new ViewUtilities.DialogBuilder(this);
			final EditText firstVisitInput = firstVisitDialog.getInput();
			firstVisitDialog.setTitle("Welcome!")
			                .setMessage("You may call me Yancey.  Would you like to tell me your name?  This will help me customize your experience.")
			                .setHintText("Name")
			                .withYancey(true)
			                .setPositiveButton("Okay", new View.OnClickListener()
			                {
				                @Override
				                public void onClick(View v)
				                {
					                String name = firstVisitInput.getText()
					                                             .toString();
					                if (name != null && !name.equals(""))
						                Preferences.setUsername(v.getContext(), name);
					                else
						                Preferences.setUsername(v.getContext(), "User");
					                Preferences.setFirstVisit(v.getContext(), false);
					                addToCollectionDialog.show();
				                }
			                });
			firstVisitDialog.create()
			                .show();
		}
	}

	@Override
	void colorComponents()
	{
		view.setBackgroundColor(backgroundColor);
//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.textview_add_game), foregroundColor);
		((TextView)view.findViewById(R.id.textview_add_game)).setTextColor(foregroundColor);
		if (Preferences.lightUI(this))
		{
			view.findViewById(R.id.textview_add_game).setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.textview_stats).setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.textview_extras).setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.textview_add_game_play).setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.imageview_settings).setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.imageview_achievements).setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.imageview_help).setBackgroundResource(R.drawable.main_button_background_dark);
		}
		else
		{
			view.findViewById(R.id.textview_add_game).setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.textview_stats).setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.textview_extras).setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.textview_add_game_play).setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.imageview_settings).setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.imageview_achievements).setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.imageview_help).setBackgroundResource(R.drawable.main_button_background_light);
		}

//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.textview_stats), foregroundColor);
		((TextView)view.findViewById(R.id.textview_stats)).setTextColor(foregroundColor);

//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.textview_extras), foregroundColor);
		((TextView)view.findViewById(R.id.textview_extras)).setTextColor(foregroundColor);

//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.textview_add_game_play), foregroundColor);
		((TextView)view.findViewById(R.id.textview_add_game_play)).setTextColor(foregroundColor);

		((TextView)view.findViewById(R.id.textview_welcome_back)).setTextColor(foregroundColor);

//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.imageview_settings), foregroundColor);
		ViewUtilities.tintImageView((AppCompatImageView)view.findViewById(R.id.imageview_settings), foregroundColor);

//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.imageview_achievements), foregroundColor);
		ViewUtilities.tintImageView((AppCompatImageView)view.findViewById(R.id.imageview_achievements), foregroundColor);

//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.imageview_help), foregroundColor);
		ViewUtilities.tintImageView((AppCompatImageView)view.findViewById(R.id.imageview_help), foregroundColor);

		ticker.setColors(foregroundColor);
//		ViewUtilities.tintLayoutBackground(ticker, foregroundColor);
	}

	private void animateAchievementButton()
	{
		achievementButton.animate()
		                 .scaleX(1.2f)
		                 .scaleY(1.2f)
		                 .setDuration(1000)
		                 .setListener(new Animator.AnimatorListener()
		                 {
			                 @Override
			                 public void onAnimationStart(Animator animation)
			                 {

			                 }

			                 @Override
			                 public void onAnimationEnd(Animator animation)
			                 {
				                 achievementButton.animate()
				                                  .scaleX(1f)
				                                  .scaleY(1f)
				                                  .setDuration(1000)
				                                  .setListener(new Animator.AnimatorListener()
				                                  {
					                                  @Override
					                                  public void onAnimationStart(Animator animation)
					                                  {

					                                  }

					                                  @Override
					                                  public void onAnimationEnd(Animator animation)
					                                  {
						                                  animateAchievementButton();
					                                  }

					                                  @Override
					                                  public void onAnimationCancel(Animator animation)
					                                  {

					                                  }

					                                  @Override
					                                  public void onAnimationRepeat(Animator animation)
					                                  {

					                                  }
				                                  })
				                                  .start();

			                 }

			                 @Override
			                 public void onAnimationCancel(Animator animation)
			                 {

			                 }

			                 @Override
			                 public void onAnimationRepeat(Animator animation)
			                 {

			                 }
		                 })
		                 .start();
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
//		Log.d(TAG, "onDown");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
//		Log.d(TAG, "onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
//		Log.d(TAG, "onSingleTapUp");
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
//		Log.d(TAG, "onScroll");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
//		Log.d(TAG, "onLongPress");
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		if (Math.abs(velocityX) > Math.abs(velocityY))
		{
			if (Math.abs(e1.getX() - e2.getX()) >= 200)
			{
				if (velocityX < -2000)
				{
					startActivity(new Intent(this, BoardGameListActivity.class));
					ActivityUtilities.exitLeft(activity);
					return true;
				}
				else if (velocityX > 2000)
				{
/*
					startActivity(new Intent(this, TestActivity.class)); //SettingsActivity.class));
					              ActivityUtilities.exitRight(activity);
*/
					if (auth.getCurrentUser() != null)
						startActivity(new Intent(this, ExtrasActivity.class));
					else
						startActivity(new Intent(this, LoginActivity.class));
					ActivityUtilities.exitRight(activity);

					return true;
				}
			}
		}
		else
		{
			if (Math.abs(e1.getY() - e2.getY()) >= 200)
			{
				if (velocityY < -2000)
				{
					startActivity(new Intent(this, StatsTabbedActivity.class));
					ActivityUtilities.exitUp(activity);
					return true;
				}
				else if (velocityY > 2000)
				{
					if (!Preferences.isTimerRunning(activity))
					{
						TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
						tempDataManager.initialize();
					}
					startActivity(new Intent(this, AddGamePlayTabbedActivity.class)
							.putExtra("EXIT", "UP"));
					ActivityUtilities.exitDown(activity);
					return true;
				}
			}
		}

		return false;
	}
}
