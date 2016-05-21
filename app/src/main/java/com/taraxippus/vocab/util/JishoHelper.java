package com.taraxippus.vocab.util;

import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.vocabulary.*;
import java.io.*;
import java.util.regex.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

public class JishoHelper
{
	MainActivity main;
	
	public JishoHelper(MainActivity main)
	{
		this.main = main;
	}
	
	public boolean isInternetAvailable() 
	{
		ConnectivityManager cm = (ConnectivityManager) main.getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo() != null;
    }
	
	public void playSoundFile(final Vocabulary v)
	{
		playSoundFile(v, null);
	}
	
	public void playSoundFile(final Vocabulary v, final OnProcessSuccessListener listener)
	{
		if (!isInternetAvailable())
		{
			Toast.makeText(main, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try
		{
			new AsyncTask()
			{
				@Override
				protected Object doInBackground(Object[] p1)
				{
					try
					{
						HttpClient httpclient = new DefaultHttpClient(); 
						HttpGet httpget = new HttpGet("http://jisho.org/search/" + v.kanji);
						HttpResponse response = httpclient.execute(httpget); 
						HttpEntity entity = response.getEntity();
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));

						StringBuilder sb = new StringBuilder();
						
						String line;
						while ((line = reader.readLine()) != null)
						{
							sb.append(line).append("\n");
						}
						
						reader.close();
						
						String page = sb.toString();
						
						String id = "audio_" + v.kanji + ":" + v.reading_trimed[0];
						
						Matcher m = Pattern.compile("<audio id=\"" + id + "\">(.*)</audio>").matcher(page);
						if (m.find())
						{
							Matcher m2 = Pattern.compile("<source src=\"(.*)\" type=\"audio/mpeg\"></source>").matcher(m.group(1));
							if (m2.find())
							{
								String file = m2.group(1);
								
								v.soundFile = file;
								
								if (listener != null)
									main.runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{		
												listener.onProcessSuccess();
											}
										});
							}
							else
							{
								v.soundFile = "-";
							}
						}
						else
						{
							v.soundFile = "-";
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						
						v.soundFile = "-";
					}
				
					return null;
				}
			}.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean offlineStrokeOrder()
	{
		return true;
	}
	
	public void createStrokeOrderView(final String[] kanjiHex, final OnProcessSuccessListener listener, final boolean accent, final boolean horizontal)
	{
		if (!offlineStrokeOrder() && !isInternetAvailable())
		{
			Toast.makeText(main, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try
		{
			new AsyncTask()
			{
				@Override
				protected Object doInBackground(Object[] p1)
				{
					final float animationSpeed = 0.01F;
					final float animationSpeed_stroke = 0.125F;
					final float animationSpeed_stroke_break = 0.125F;

					final int animationSpeed_back = 750;
					final int animationSpeed_back_break = 50;

					final StringBuilder html = new StringBuilder();

					html.append("<!DOCTYPE html><html>");
					html.append("<head>");
					html.append("</head>");
					html.append("<body onload=\"init()\">");
					html.append(	"<center><table>");

					TypedValue typedValue = new TypedValue();
					main.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
					int color = accent ? main.getResources().getColor(R.color.accent, main.getTheme()) : typedValue.data;
					
					if (horizontal)
						html.append("<tr>");
					
					for (String hex : kanjiHex)
					{
						if (!horizontal)
							html.append("<tr>");
							
						html.append("<td>");
						html.append(getSVG(hex, color));
						html.append("</td>");
						
						if (!horizontal)
							html.append("<tr>");
					}

					if (horizontal)
						html.append("</tr>");
					
					html.append(	"</table height=\"100℅\"></center>");
					html.append(	"<script>");

					html.append(	"function init()");
					html.append(	"{");

					for (String hex : kanjiHex)
					{
						html.append(	"document.getElementById('kvg:StrokePaths_" + hex + "').style['stroke-opacity'] = 1;");
						html.append(	"animateAll(\"" + hex + "\", false);");
					}
						
					html.append(	"}");

					html.append(	"function animateAll(hex, back)");
					html.append(	"{");
					html.append(		"var element;");
					html.append(		"if (back)");
					html.append(		"{");
					html.append(			"for (var i = 0; (element = document.getElementById(\"kvg:\" + hex + \"-s\" + (i + 1))); ++i)");
					html.append(			"{");
					html.append(				"animateBack(element);");
					html.append(			"}");
					html.append(			"setTimeout(animateAll, " + (animationSpeed_back + animationSpeed_back_break) + ", hex, false);");
					html.append(		"}");
					html.append(		"else");
					html.append(		"{");
					html.append(			"var delay = 0.0;");
					html.append(			"for (var i = 0; (element = document.getElementById(\"kvg:\" + hex + \"-s\" + (i + 1))); ++i)");
					html.append(			"{");
					html.append(				"delay = delay + animate(element, delay);");
					html.append(			"}");
					html.append(		"}");
					html.append(	"}");

					html.append(	"function animate(path, delay)");
					html.append(	"{");
					html.append(		"var length = path.getTotalLength();");
					html.append(		"path.style.transition = path.style.WebkitTransition = 'none';");
					html.append(		"path.style.strokeDasharray = length + ' ' + length;");
					html.append(		"path.style.strokeDashoffset = length;");
					html.append(		"path.getBoundingClientRect();");
					html.append(		"path.style.transition = path.style.WebkitTransition = 'stroke-dashoffset ' + (" + animationSpeed_stroke + " + length * " + animationSpeed + ") + 's ease-out';");
					html.append(		"path.style.strokeDashoffset = '0';");
					html.append(		"path.style['webkit-transition-delay'] = path.style['transition-delay'] = delay + 's';");
					html.append(		"return " + (animationSpeed_stroke + animationSpeed_stroke_break) + " + length * " + animationSpeed + ";");
					html.append(	"}");

					html.append(	"function animateBack(path)");
					html.append(	"{");
					html.append(		"var length = path.getTotalLength();");
					html.append(		"path.style.transition = path.style.WebkitTransition = 'stroke-dashoffset " + (animationSpeed_back / 1000F) + "s ease-in';");
					html.append(		"path.style.strokeDashoffset = length;");
					html.append(		"path.style['webkit-transition-delay'] = path.style['transition-delay'] = '0s';");
					html.append(	"}");

					html.append(	"</script>");
					html.append("</body>");
					html.append("</html>");

					main.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{		
								final WebView webView = new WebView(main);
								webView.setFocusable(false);
								webView.getSettings().setJavaScriptEnabled(true);
								webView.getSettings().setDomStorageEnabled(true);
								webView.setWebChromeClient(new WebChromeClient());
								webView.setBackgroundColor(0);
								webView.setVerticalScrollBarEnabled(!horizontal);
								webView.setHorizontalScrollBarEnabled(horizontal);
								webView.setScrollBarSize((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, main.getResources().getDisplayMetrics()));
								webView.loadDataWithBaseURL(null, html.toString(), "text/html; charset=UTF-8", "UTF-8", null);
								
								listener.onProcessSuccess(webView);
							}
						});

					return null;
				}
			}.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	

	public String getSVG(String kanjiHex, int color)
	{
		BufferedReader reader = null;
		
		try
		{
			if (offlineStrokeOrder())
			{
				File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
				
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath() + "/kanji/" + kanjiHex + ".svg")));
			}
			else
			{
				HttpClient httpclient = new DefaultHttpClient(); 
				HttpGet httpget = new HttpGet("http://d1w6u4xc3l95km.cloudfront.net/kanji-2015-03/" + kanjiHex + ".svg");
				HttpResponse response = httpclient.execute(httpget); 
				HttpEntity entity = response.getEntity();

				reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			}
			
			StringBuilder sb = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line).append("\n");
			}
			
			String colorHex = Integer.toHexString(color);
			
			while (colorHex.length() < 6 && colorHex.length() > 3)
				colorHex = "0" + colorHex;
			
			if (colorHex.length() > 6)
				colorHex = colorHex.substring(2);
			
			final String file = sb.toString();
			final String svg = Pattern.compile("<g id=\"kvg:StrokeNumbers.*</g>", Pattern.DOTALL).matcher(Pattern.compile("<.*<svg", Pattern.DOTALL).matcher(file)
				.replaceFirst("<svg")
				.replaceFirst("width=\".*\"", "height=\"50%\" viewbox=\"0 0 109 109\"")
				.replaceFirst("</svg>", "<rect style=\"fill-opacity: 0; stroke-opacity: 0;\" x=\"0\" y=\"0\" width=\"109\" height=\"109\" onclick=\"animateAll('" + kanjiHex + "', true)\" /></svg>")
				).replaceFirst("")
				.replaceFirst("stroke:#000000;stroke-width:3;stroke-linecap:round;stroke-linejoin:round;", "stroke:#" + colorHex + ";stroke-opacity:0;stroke-width:6;stroke-linecap:butt;stroke-linejoin:butt;");
				
			return svg;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public void search(String query)
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("http://jisho.org/search/" + query));
		main.startActivity(i);
	}
	
}
