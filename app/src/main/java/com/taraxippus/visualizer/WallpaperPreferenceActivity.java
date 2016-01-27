package com.taraxippus.visualizer;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import java.io.*;
import android.media.session.*;

public class WallpaperPreferenceActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(com.taraxippus.visualizer.R.xml.preference);
	
		Preference filePicker = findPreference("filePicker");
		filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.setType("image/*");
				
				startActivityForResult(i, 0);
				return true;
			}
		});
		
		Preference setWallpaper = findPreference("setWallpaper");
		setWallpaper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					try
					{
						Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
						intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(WallpaperPreferenceActivity.this, com.taraxippus.visualizer.OpenGLESWallpaperService.class));
						startActivity(intent);
					}
					catch(Exception e)
					{
						Intent intent = new Intent();
						intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
						startActivity(intent);
					}
					
					return true;
				}
			});
			
		Preference crash = findPreference("crash");
		crash.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					
					throw new RuntimeException("Debug crash");
				}
			});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch (requestCode)
		{
			case 0:		
				if (resultCode != RESULT_OK)
					break;
				
				Uri content_describer = data.getData();
				
				InputStream in = null;
				OutputStream out = null; 
				
				try 
				{
					in = getContentResolver().openInputStream(content_describer);
					out = new FileOutputStream(getFilesDir().getPath() + "wallpaper");
				
					byte[] buffer = new byte[1024];
					int len;
					while ((len = in.read(buffer)) != -1) 
					{
						out.write(buffer, 0, len);
					}
					
					SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
					SharedPreferences.Editor editor = customSharedPreference.edit();
					editor.putString("wallpaper", content_describer.getPath());
					editor.commit();
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				} 
				finally 
				{
					try
					{
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
				}
				
				return;
			default:
				break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}
