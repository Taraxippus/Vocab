package com.taraxippus.vocab.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.text.style.StyleSpan;
import android.text.Spannable;
import android.graphics.Typeface;
import android.text.style.URLSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.widget.Space;
import android.view.View.OnClickListener;
import java.util.ArrayList;

public final class JishoHelper
{
	public static final String KANJI_SVG_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/kanji/";
	
	public static final int MAX_SENTENCE_COUNT = 5;
	
	static final float animationSpeed = 0.01F;
	static final float animationSpeed_stroke = 0.125F;
	static final float animationSpeed_stroke_break = 0.125F;

	static final int animationSpeed_back = 750;
	static final int animationSpeed_back_break = 50;
	
	static final boolean offline;
	
	private JishoHelper() {}
	
	static
	{
		offline = new File(KANJI_SVG_PATH).exists();
	}
	
	public static boolean isInternetAvailable(Context context) 
	{
		return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }
	
	public static boolean offlineStrokeOrder()
	{
		return offline;
	}

	public static boolean isStrokeOrderAvailable(Context context)
	{
		return isInternetAvailable(context) || offlineStrokeOrder();
	}
	
	public static void findSoundFile(DBHelper dbHelper, Vocabulary v, OnProcessSuccessListener listener)
	{
		if (!isInternetAvailable(dbHelper.context))
		{
			Toast.makeText(dbHelper.context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try
		{
			new FindSoundFileTask(dbHelper, listener).execute(v);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addStrokeOrderView(Context context, String kanji, final ViewGroup layout, final ViewGroup.LayoutParams params, final View progress, boolean accent, boolean horizontal)
	{
		if (!isStrokeOrderAvailable(context))
		{
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}

		char[] kanjiChars = new char[kanji.length()];
		kanji.getChars(0, kanji.length(), kanjiChars, 0);
		String[] hex = new String[kanjiChars.length];

		for (int i = 0; i < kanjiChars.length; ++i)
		{
			hex[i] = Integer.toHexString(kanjiChars[i]);
			while (hex[i].length() < 5)
			{
				hex[i] = "0" + hex[i];
			}
		}

		createStrokeOrderView(context, hex, new OnProcessSuccessListener()
			{
				@Override
				public void onProcessSuccess(Object... args)
				{
					if (progress != null)
						progress.setVisibility(View.GONE);

					layout.addView((View) args[0], params);
				}
			}, accent, horizontal);
	}
	
	public static void createStrokeOrderView(Context context, String[] kanjiHex, final OnProcessSuccessListener listener, final boolean accent, final boolean horizontal)
	{
		if (!offlineStrokeOrder() && !isInternetAvailable(context))
		{
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try
		{
			new CreateStrokeOrderViewTask(context, accent, horizontal, listener).execute(kanjiHex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addExampleSentences(Context context, String kanji, String[] meaning, final ViewGroup layout, final ViewGroup.LayoutParams params, final View progress)
	{
		if (!isInternetAvailable(context))
		{
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}

		try
		{
			String[] args = new String[meaning.length + 1];
			args[0] = kanji;
			System.arraycopy(meaning, 0, args, 1, meaning.length);
			new FindSentencesTask(context, new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object... args)
					{
						if (progress != null)
							progress.setVisibility(View.GONE);

						layout.addView((View) args[0], params);
					}
				}).execute(args);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getSVG(String kanjiHex, int index, int color)
	{
		BufferedReader reader = null;
		
		try
		{
			if (offlineStrokeOrder())
			{
				try
				{
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(KANJI_SVG_PATH + kanjiHex + ".svg")));
				}
				catch (Exception e)
				{
					HttpClient httpclient = new DefaultHttpClient(); 
					HttpGet httpget = new HttpGet("http://d1w6u4xc3l95km.cloudfront.net/kanji-2015-03/" + kanjiHex + ".svg");
					HttpResponse response = httpclient.execute(httpget); 
					HttpEntity entity = response.getEntity();

					reader = new BufferedReader(new InputStreamReader(entity.getContent()));
				}
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
				sb.append(line).append("\n");
			
				
			float alpha = Color.alpha(color) / 255F;
			String colorHex = Integer.toHexString(Color.rgb((int) (Color.red(color) * alpha + 255 * (1 - alpha)), (int) (Color.green(color) * alpha + 255 * (1 - alpha)), (int) (Color.blue(color) * alpha + 255 * (1 - alpha))));
			
			while (colorHex.length() < 6 && colorHex.length() > 3)
				colorHex = "0" + colorHex;
			
			if (colorHex.length() > 6)
			{
				colorHex = colorHex.substring(2);
			}
				
			final String file = sb.toString();
			final String svg = Pattern.compile("<g id=\"kvg:StrokeNumbers.*</g>", Pattern.DOTALL).matcher(Pattern.compile("<.*<svg", Pattern.DOTALL).matcher(file)
				.replaceFirst("<svg")
				.replaceFirst("width=\".*\"", "height=\"100\" viewbox=\"0 0 109 109\"")
				.replaceFirst("</svg>", "<rect style=\"fill-opacity: 0; stroke-opacity: 0;\" x=\"0\" y=\"0\" width=\"109\" height=\"109\" onclick=\"animateAll('" + kanjiHex + "', true)\" /></svg>")
				).replaceFirst("")
				.replaceFirst("stroke:#000000;stroke-width:3;stroke-linecap:round;stroke-linejoin:round;", "stroke:#" + colorHex + ";stroke-opacity:0.5;stroke-width:6;stroke-linecap:butt;stroke-linejoin:butt;")
				.replace(kanjiHex, kanjiHex + "_" + index);
				
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
	
	public static void search(Context context, String query)
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		try
		{
			i.setData(Uri.parse("http://jisho.org/search/" + URLEncoder.encode(query, "UTF-8").replace("+", "%20")));
		}
		catch (UnsupportedEncodingException e)
		{
			i.setData(Uri.parse("http://jisho.org/search/Error"));
		}
		context.startActivity(i);
	}
	
	public static class FindSoundFileTask extends AsyncTask<Vocabulary, Void, Boolean>
	{
		final OnProcessSuccessListener listener;
		final DBHelper dbHelper;
		
		public FindSoundFileTask(DBHelper dbHelper, OnProcessSuccessListener listener)
		{
			this.listener = listener;
			this.dbHelper = dbHelper;
		}
	
		Vocabulary v;
		
		@Override
		protected Boolean doInBackground(Vocabulary...  p1)
		{
			v = p1[0];

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
					sb.append(line);
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
						return true;
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

			return false;
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			if (success)
				listener.onProcessSuccess();
				
			dbHelper.updateVocabularySoundFile(dbHelper.getId(v.kanji), v.soundFile);
		}
	}
	
	public static class CreateStrokeOrderViewTask extends AsyncTask<String, Void, String>
	{
		final Context context;
		final boolean accent, horizontal;
		final OnProcessSuccessListener listener;
		
		public CreateStrokeOrderViewTask(Context context, boolean accent, boolean horizontal, OnProcessSuccessListener listener)
		{
			this.context = context;
			this.accent = accent;
			this.horizontal = horizontal;
			this.listener = listener;
		}
		
		@Override
		protected String doInBackground(String... kanjiHex)
		{
			final StringBuilder html = new StringBuilder();

			html.append("<!DOCTYPE html><html>");
			html.append("<meta name=\"viewport\" content=\"height=100, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=0\"/>");
			html.append("<head>");
			html.append("</head>");
			html.append("<body style=\"margin: 0; padding: 0\" onload=\"init()\">");
			html.append(	"<center><table>");

			TypedValue typedValue = new TypedValue();
			context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
			int color = context.getResources().getColor(accent ? R.color.accent : typedValue.resourceId, context.getTheme());

			if (horizontal)
				html.append("<tr height=\"100\">");

			for (int i = 0; i < kanjiHex.length; ++i)
			{
				if (!horizontal)
					html.append("<tr width=\"100%\">");

				html.append("<td>");
				html.append(getSVG(kanjiHex[i], i, color));
				html.append("</td>");

				if (!horizontal)
					html.append("<tr>");
			}

			if (horizontal)
				html.append("</tr>");

			html.append(	"</table></center>");
			html.append(	"<script>");

			html.append(	"function init()");
			html.append(	"{");

			for (int i = 0; i < kanjiHex.length; ++i)
			{
				html.append(	"document.getElementById('kvg:StrokePaths_" + kanjiHex[i] + "_" + i + "').style['stroke-opacity'] = 1;");
				html.append(	"animateAll(\"" + kanjiHex[i] + "_" + i + "\", false);");
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

			return html.toString();
		}

		@Override
		protected void onPostExecute(String result)
		{
			final WebView webView = new WebView(context)
			{
				@Override
				public boolean onTouchEvent(MotionEvent event)
				{
					if (horizontal)
						requestDisallowInterceptTouchEvent(true);
					return super.onTouchEvent(event);
				}          
			};
			webView.setTag("stroke_order");
			webView.setFocusable(false);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setUseWideViewPort(true);
			webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			webView.getSettings().setAppCacheEnabled(false);
			webView.setBackgroundColor(0);
			webView.setVerticalScrollBarEnabled(!horizontal);
			webView.setHorizontalScrollBarEnabled(horizontal);
			webView.setNestedScrollingEnabled(true);
			webView.setScrollBarSize((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics()));
			webView.loadDataWithBaseURL(null, result, "text/html; charset=UTF-8", "UTF-8", null);
			webView.setPadding(0, 0, 0, 0);
			webView.setForegroundGravity(Gravity.CENTER);
			webView.setWebViewClient(new WebViewClient() 
				{
					@Override
					public void onPageFinished(WebView view, String url) 
					{                  
						listener.onProcessSuccess(webView);
					}
				});
		}
	}

	public static class FindSentencesTask extends AsyncTask<String, Void, View>
	{
		final OnProcessSuccessListener listener;
		final Context context;
		
		public FindSentencesTask(Context context, OnProcessSuccessListener listener)
		{
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected View doInBackground(String...  p1)
		{
			int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			TextView v;
			View v1;
			
			try
			{
				HttpClient httpclient = new DefaultHttpClient(); 
				HttpGet httpget = new HttpGet();
				HttpResponse response;
				HttpEntity entity;
				BufferedReader reader;
				StringBuilder sb = new StringBuilder(), sb2 = new StringBuilder();
				String s, line, page;
				int index, index0, index1, index2, i, i1, count;
				ArrayList<String> list = new ArrayList<>();
				boolean needsSpace = false;
				
				for (i = 0; i < p1.length; ++i)
				{
					httpget.setURI(URI.create("http://jisho.org/search/" + URLEncoder.encode(p1[0] + " " + (i == p1.length - 1 ? "" : p1[1 + i]) + " #sentences", "UTF-8").replace("+", "%20")));
					response = httpclient.execute(httpget); 
					entity = response.getEntity();
					
					reader = new BufferedReader(new InputStreamReader(entity.getContent()));
					sb.setLength(0);

					while ((line = reader.readLine()) != null)
					{
						sb.append(line);
					}

					reader.close();
					page = sb.toString();
					index = 0;
					count = 0;
					
					for (i1 = 0; count < MAX_SENTENCE_COUNT; ++i1)
					{
						sb.setLength(0);
						sb2.setLength(0);

						index = page.indexOf("class=\"sentence_content\"", index + 1);
						if (index == -1)
							break;
						line = page.substring(index, page.indexOf("</div>", index));
						
						index0 = 0;
						while (index0 != -1)
						{
							index0 = line.indexOf("class=\"clearfix\"", index0 + 1);
							if (index0 == -1)
								break;
							index1 = line.indexOf("class=\"furigana\"", index0);
							index2 = line.indexOf("class=\"unlinked\"", index0);
							s = line.substring(index2 + 17, line.indexOf("</", index2));
							
							if (index1 != -1 && index1 < line.indexOf("</li", index0))
							{
								sb.append(s);
								sb2.append(StringHelper.replaceWithFurigana(s, line.substring(index1 + 17, line.indexOf("</", index1)), sb2.length() > 0, true));
							}
							else
							{
								sb.append(s);
								sb2.append(s);
							}
						}
						
						index0 = line.indexOf("class=\"english\"");
						index1 = line.indexOf("href=\"");
						s = line.substring(index1 + 6, line.indexOf("\"", index1 + 7));
						if (list.contains(s))
							continue;
							
						list.add(s);
						count++;
						
						needsSpace = true;
						v1 = LayoutInflater.from(context).inflate(R.layout.item_sentence, layout, false);
						v = (TextView) v1.findViewById(R.id.text_kanji);
						v.setTextLocale(Locale.JAPANESE);
						v.setText(sb.toString());
						v = (TextView) v1.findViewById(R.id.text_reading);
						v.setTextLocale(Locale.JAPANESE);
						v.setText(sb2.toString().replace("・・", "・"));
						v = (TextView) v1.findViewById(R.id.text_meaning);
						v.setText(line.substring(index0 + 16, line.indexOf("</", index0)));
						v1.setOnClickListener(new OpenUriListener(s));
						layout.addView(v1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
					}
					
					if (needsSpace)
						layout.addView(new Space(context), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, padding));
					
					needsSpace = false;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();

				v = new TextView(context);
				v.setText(e.toString());
				layout.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));	
			}
			
			if (layout.getChildCount() == 0)
			{
				v = new TextView(context);
				v.setText("Couln't find any example sentences on jisho.org");
				layout.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));	
			}
			
			return layout;
		}

		@Override
		protected void onPostExecute(View v)
		{
			listener.onProcessSuccess(v);
		}
		
		private class OpenUriListener implements View.OnClickListener
		{
			Uri uri;
			public OpenUriListener(String uri)
			{
				this.uri = Uri.parse(uri);
			}
			
			@Override
			public void onClick(View p1)
			{
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(uri);
				context.startActivity(i);
			}
		}
	}
}
