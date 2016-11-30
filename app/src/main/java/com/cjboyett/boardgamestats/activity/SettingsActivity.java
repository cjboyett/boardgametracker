package com.cjboyett.boardgamestats.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.model.changelog.ChangeLog;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.flask.colorpicker.ColorPickerPreference;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
{
	private static final int REQUEST_IMAGE = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new GeneralPreferenceFragment())
				.commit();
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context)
	{
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onIsMultiPane()
	{
		return isXLargeTablet(this);
	}

	/**
	 * This method stops fragment injection in malicious applications.
	 * Make sure to deny any unknown fragments here.
	 */
	protected boolean isValidFragment(String fragmentName)
	{
		return PreferenceFragment.class.getName().equals(fragmentName)
				|| GeneralPreferenceFragment.class.getName().equals(fragmentName);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_main);

			((CheckBoxPreference)findPreference(getString(R.string.generate_palette_preference)))
					.setChecked(Preferences.generatePalette(getActivity()));
			findPreference(getString(R.string.generate_palette_preference))
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue)
						{
							Preferences.setGeneratePalettePreference(getActivity(), (boolean) newValue);
							((CheckBoxPreference) preference).setChecked((boolean) newValue);
							return false;
						}
					});

			((CheckBoxPreference)findPreference(getString(R.string.allow_notifications_preference)))
					.setChecked(Preferences.showNotifications(getActivity()));
			findPreference(getString(R.string.allow_notifications_preference))
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue)
						{
							Preferences.setShowNotifications(getActivity(), (boolean) newValue);
							((CheckBoxPreference) preference).setChecked((boolean) newValue);
							return false;
						}
					});

			((ListPreference)findPreference(getString(R.string.thumbnail_size_preference)))
					.setValueIndex(Preferences.getThumbnailSize(getActivity()));
			findPreference(getString(R.string.thumbnail_size_preference))
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue)
						{
							int size = Preferences.THUMBNAIL_NORMAL;
							switch ((String) newValue)
							{
								case "Dovber":
									size = Preferences.THUMBNAIL_STUPID_SMALL;
									break;
								case "Large":
									size = Preferences.THUMBNAIL_LARGE;
									break;
								case "Normal":
									size = Preferences.THUMBNAIL_NORMAL;
									break;
								case "Small":
									size = Preferences.THUMBNAIL_SMALL;
									break;
								case "Tiny":
									size = Preferences.THUMBNAIL_TINY;
									break;
							}
							Preferences.setThumbnailSizePreference(getActivity(), size);
							((ListPreference) findPreference(getString(R.string.thumbnail_size_preference)))
									.setValueIndex(Preferences.getThumbnailSize(getActivity()));
							return false;
						}
					});


			((CheckBoxPreference)findPreference(getString(R.string.sort_winners_first_preference)))
					.setChecked(Preferences.sortWinnersFirst(getActivity()));
			findPreference(getString(R.string.sort_winners_first_preference))
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue)
						{
							Preferences.setSortWinnersFirstPreference(getActivity(), (boolean) newValue);
							((CheckBoxPreference) preference).setChecked((boolean) newValue);
							return false;
						}
					});

			((CheckBoxPreference)findPreference(getString(R.string.activity_transition_preference)))
					.setChecked(Preferences.useActivityTransitions(getActivity()));
			findPreference(getString(R.string.activity_transition_preference))
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue)
						{
							Preferences.setActivityTransitionPreference(getActivity(), (boolean) newValue);
							((CheckBoxPreference) preference).setChecked((boolean) newValue);
							if (!(boolean) newValue)
							{
								Preferences.setUseSwipesPreference(getActivity(), (boolean) newValue);
								((CheckBoxPreference) findPreference(getString(R.string.swipe_preference)))
										.setChecked((boolean) newValue);
							}
							return false;
						}
					});

			findPreference(getString(R.string.swipe_preference)).setDependency(getString(R.string.activity_transition_preference));
			((CheckBoxPreference)findPreference(getString(R.string.swipe_preference)))
					.setChecked(Preferences.useSwipes(getActivity()));
			findPreference(getString(R.string.swipe_preference))
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue)
						{
							Preferences.setUseSwipesPreference(getActivity(), (boolean) newValue);
							((CheckBoxPreference) preference).setChecked((boolean) newValue);
							return false;
						}
					});

			((EditTextPreference)findPreference(getString(R.string.display_name_preference)))
					.setText(Preferences.getUsername(getActivity()));
			findPreference(getString(R.string.display_name_preference))
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue)
						{
							Preferences.setUsername(getActivity(), (String) newValue);
							((EditTextPreference)findPreference(getString(R.string.display_name_preference)))
									.setText((String)newValue);
							return false;
						}
					});

			((EditTextPreference)findPreference(getString(R.string.threshold_preference)))
					.setText(Preferences.gamePlayThreshold(getActivity()) + "");
			findPreference(getString(R.string.threshold_preference))
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue)
						{
							if (NumberUtils.isParsable((String)newValue))
							{
								Preferences.setGamePlayThreshold(getActivity(), Integer.parseInt((String) newValue));
								((EditTextPreference)findPreference(getString(R.string.threshold_preference)))
										.setText((String)newValue);
							}
							return false;
						}
					});

			findPreference(getString(R.string.choose_theme_preference)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue)
				{
					Integer[] colors = (Integer[])newValue;
					setColorPreferences(colors[0], colors[1]);
					return false;
				}
			});

			findPreference(getString(R.string.pref_recalculate_stats)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					GamesDbHelper dbHelper = new GamesDbHelper(getActivity());
					PlayersDbUtility.populateAllPlayersTable(dbHelper);
					dbHelper.close();
					return false;
				}
			});

			findPreference(getString(R.string.email_preference)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					openEmail();
					return false;
				}
			});

			findPreference(getString(R.string.edit_avatar_preference)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					MultiImageSelector.create()
					                  .single()
					                  .start(getActivity(), REQUEST_IMAGE);

					return false;
				}
			});

			findPreference(getString(R.string.version_key)).setSummary("Version " + ChangeLog.entries[0].version);

			bindPreferenceSummaryToValue(findPreference(getString(R.string.theme_background_preference)));
			bindPreferenceSummaryToValue(findPreference(getString(R.string.theme_foreground_preference)));
			setColorPreferences(Preferences.getBackgroundColor(getActivity()), Preferences.getForegroundColor(getActivity()));
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference instanceof ColorPickerPreference)
			{
				String key = preference.getKey();
				String colorString = "#" + Integer.toHexString((int)newValue).substring(2).toUpperCase();
				switch (key)
				{
					case "com.cjboyett.boardgamestats.THEME_BACKGROUND_PREFERENCE":
						Preferences.setThemeBackgroundPreference(preference.getContext(), (int)newValue);
						findPreference(getString(R.string.theme_background_preference))
								.setSummary(colorString);
						break;
					case "com.cjboyett.boardgamestats.THEME_FOREGROUND_PREFERENCE":
						Preferences.setThemeForegroundPreference(preference.getContext(), (int) newValue);
						findPreference(getString(R.string.theme_foreground_preference))
								.setSummary(colorString);
						break;
					default:
						break;
				}
			}
			return true;
		}

		private void bindPreferenceSummaryToValue(Preference preference)
		{
			preference.setOnPreferenceChangeListener(this);
		}

		private void setColorPreferences(int backgroundColor, int foregroundColor)
		{
			String backgroundColorString = "#" + Integer.toHexString(backgroundColor).substring(2).toUpperCase();
			String foregroundColorString = "#" + Integer.toHexString(foregroundColor).substring(2).toUpperCase();

			((ColorPickerPreference)findPreference(getString(R.string.theme_background_preference)))
					.setValue(backgroundColor);
			findPreference(getString(R.string.theme_background_preference))
					.setSummary(backgroundColorString);
			((ColorPickerPreference)findPreference(getString(R.string.theme_foreground_preference)))
					.setValue(foregroundColor);
			findPreference(getString(R.string.theme_foreground_preference))
					.setSummary(foregroundColorString);
		}

		private void openEmail()
		{
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:")); // only email apps should handle this
			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"casey@cjboyett.com"});
			intent.putExtra(Intent.EXTRA_SUBJECT, "Games Tracker");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
			startActivity(intent);
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
//		ActivityUtilities.exitLeft(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_IMAGE){
			if(resultCode == RESULT_OK)
			{
				List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

				final GamesDbHelper dbHelper = new GamesDbHelper(this);
				final Activity activity = this;

				new AsyncTask<String, Void, Bitmap>()
				{
					@Override
					protected Bitmap doInBackground(String... params)
					{
						Bitmap bitmap = BitmapFactory.decodeFile(params[0]);
						BitmapFactory.Options options = new BitmapFactory.Options();

						if (bitmap.getByteCount() > 256 * 1024)
						{
							options.inSampleSize = (int)Math.ceil(Math.sqrt((bitmap.getByteCount() / 1024) / 256));
							bitmap = BitmapFactory.decodeFile(params[0], options);
						}

						PlayersDbUtility.setPlayerImageFilePath(dbHelper, "master_user", true);

						ImageController imageController = new ImageController(activity);
						imageController.setDirectoryName("avatars")
						               .setFileName("master_user.jpg")
						               .setFileType("JPG")
						               .save(bitmap);

						return bitmap;
					}

					@Override
					protected void onPostExecute(Bitmap bitmap)
					{
						ActivityUtilities.setDatabaseChanged(activity, true);
						dbHelper.close();
					}
				}.execute(paths.get(0));
			}
		}
	}

}
