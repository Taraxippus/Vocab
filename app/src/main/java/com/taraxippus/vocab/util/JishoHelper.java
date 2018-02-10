package com.taraxippus.vocab.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Html;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.Kanji;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.taraxippus.vocab.vocabulary.VocabularyType;

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
					//TransitionManager.beginDelayedTransition(findRootView(layout));
					
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
	
	public static void addExampleSentences(Context context, String kanji, final String[] meaning, final ViewGroup layout, final ViewGroup.LayoutParams params, final View progress)
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
						//TransitionManager.beginDelayedTransition(layout);
						
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

	public static void importKanji(Context context, Kanji kanji, final OnProcessSuccessListener listener)
	{
		if (!isInternetAvailable(context))
		{
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}

		try
		{
			new ImportKanjiTask(context, listener).execute(kanji);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void searchKanji(Context context, String query, final OnProcessSuccessListener listener)
	{
		if (!isInternetAvailable(context))
		{
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}

		try
		{
			new SearchKanjiTask(context, listener).execute(query);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void searchVocabulary(Context context, String query, final OnProcessSuccessListener listener)
	{
		if (!isInternetAvailable(context))
		{
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}

		try
		{
			new SearchVocabularyTask(context, listener).execute(query);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addReadingCompounds(Context context, char kanji, final ViewGroup layout, final ViewGroup.LayoutParams params, final View progress)
	{
		if (!isInternetAvailable(context))
		{
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}

		try
		{
			new FindReadingCompoundsTask(context, new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object... args)
					{
						TransitionManager.beginDelayedTransition(findRootView(layout));
						
						if (progress != null)
							progress.setVisibility(View.GONE);

						layout.addView((View) args[0], params);
					}
				}).execute(kanji);
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
	
	public static ViewGroup findRootView(ViewGroup v)
	{
		while (v.getId() != R.id.layout_content && v.getId() != android.R.id.content && v.getParent() instanceof ViewGroup)
		{
			v = (ViewGroup) v.getParent();
		}
		
		return v;
	}
	
	public static void importVocabulary(Context context, Vocabulary vocab, final OnProcessSuccessListener listener)
	{
		if (!isInternetAvailable(context))
		{
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}

		try
		{
			new ImportVocabularyTask(context, listener).execute(vocab);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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

				int index0, index1;
				index0 = page.indexOf("<audio id=\"" + id + "\">");
				index1 = index0 == -1 ? -1 : page.indexOf("<source src=\"", index0 + 13 + id.length());
				
				if (index1 != -1)
					v.soundFile = page.substring(index1 + 13, page.indexOf("\"", index1 + 13));
				
				else
					v.soundFile = "-";
				
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
				
			dbHelper.updateVocabularySoundFile(v.id, v.soundFile);
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
					
					searchSentences:
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
							index1 = index0;
							index0 = line.indexOf("class=\"clearfix\"", index0 + 1);
							
							if (index0 == -1)
								index0 = line.indexOf("class='clearfix'", index1 + 1);
							
							if (index0 == -1)
							{
								index1 = line.indexOf("</ul", index1);
								index2 = line.substring(0, index1).lastIndexOf(">");

								if (index1 - index2 > 1)
								{
									s = StringHelper.trim(line.substring(index2 + 1, index1));
									sb.append(s);
									if (!s.isEmpty() && sb2.length() != 0 && StringHelper.isKanaOrKanji(s.substring(0, 1)) && "・".equals(sb2.substring(sb2.length() - 1, sb2.length())))
										sb2.delete(sb2.length() - 1, sb2.length());
									sb2.append(s);
								}
								
								break;
							}
								
							index1 = line.indexOf("<li", index1);
							index2 = line.substring(0, index1).lastIndexOf(">");
							
							if (index1 - index2 > 1)
							{
								s = StringHelper.trim(line.substring(index2 + 1, index1));
								sb.append(s);
								if (!s.isEmpty() && sb2.length() != 0 && StringHelper.isKanaOrKanji(s.substring(0, 1)) && "・".equals(sb2.substring(sb2.length() - 1, sb2.length())))
									sb2.delete(sb2.length() - 1, sb2.length());
								sb2.append(s);
							}
							
							index1 = line.indexOf("class=\"furigana\"", index0);
							index2 = line.indexOf("class=\"unlinked\"", index0);
							if (index2 == -1)
							{
								index1 = line.indexOf("class='furigana'", index0);
								index2 = line.indexOf("class='unlinked'", index0);
							}
								
							if (index2 == -1)
							{
								index2 = line.indexOf(">", line.indexOf("<a", index0));
								if (index2 > line.indexOf("</ul>"))
									break;
								s = line.substring(index2 + 1, line.indexOf("</", index2));
							}
							else
								s = line.substring(index2 + 17, line.indexOf("</", index2));
							
							if (s.isEmpty())

								continue searchSentences;
								
							if (index1 != -1 && index1 < line.indexOf("</li", index0))
							{
								sb.append(s);
								sb2.append(StringHelper.replaceWithFurigana(s, line.substring(index1 + 17, line.indexOf("</", index1)), sb2.length() > 0 && StringHelper.isKanaOrKanji(sb2.substring(sb2.length() - 1)), true));
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
						v1.setOnClickListener(new OpenUriListener(s));
						v = (TextView) v1.findViewById(R.id.text_kanji);
						v.setTextLocale(Locale.JAPANESE);
						v.setText(StringHelper.trim(sb.toString()));
						v = (TextView) v1.findViewById(R.id.text_reading);
						v.setTextLocale(Locale.JAPANESE);
						s = StringHelper.trim(sb2.toString().replace("・・", "・"));
						if (s.endsWith("・"))
							s = s.substring(0, s.length() - 1);
						v.setText(s);
						if (s.isEmpty())
							v.setVisibility(View.GONE);
						v = (TextView) v1.findViewById(R.id.text_meaning);
						v.setText(StringHelper.trim(line.substring(index0 + 16, line.indexOf("</", index0))));
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
				v.setPadding(padding, padding, padding, padding);
				layout.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));	
			}
			
			if (layout.getChildCount() == 0)
			{
				v = new TextView(context);
				v.setPadding(padding, padding, padding, padding);
				v.setText("Couln't find any example sentences on jisho.org");
				layout.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));	
			}
			
			layout.setTag("sentences");
			
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
	
	public static class ImportKanjiTask extends AsyncTask<Kanji, Void, Kanji>
	{
		final OnProcessSuccessListener listener;
		final Context context;

		public ImportKanjiTask(Context context, OnProcessSuccessListener listener)
		{
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected Kanji doInBackground(Kanji...  p1)
		{
			Kanji kanji = p1[0];
			
			try
			{
				HttpClient httpclient = new DefaultHttpClient(); 
				HttpGet httpget = new HttpGet();
				HttpResponse response;
				HttpEntity entity;
				BufferedReader reader;
				StringBuilder sb = new StringBuilder();
				String line, page;
				int index, index0, index1, index2;
				String[] tmp;
				ArrayList<String> reading = new ArrayList<>();
				
				httpget.setURI(URI.create("http://jisho.org/search/" + URLEncoder.encode(kanji.kanji + " #kanji", "UTF-8").replace("+", "%20")));
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
				sb.setLength(0);
					
				index = page.indexOf("class=\"kanji-details__stroke_count\"");
				index0 = page.indexOf("<strong>", index);
				kanji.strokes = Integer.parseInt(page.substring(index0 + 8, page.indexOf("<", index0 + 1)));
				
				index = page.indexOf("class=\"radical_meaning\"", index);
				index0 = page.indexOf(">", index);
				index1 = page.indexOf("</span>", index);
				if (index != -1)
				{
					sb.append("Radical: ");
					sb.append(StringHelper.trim(page.substring(index0 + 1, index1)));
					sb.append(" ");
					sb.append(StringHelper.trim(page.substring(index1 + 7, page.indexOf("<", index1 + 1))));
					sb.append("\n");
				}
				
				index = page.indexOf("class=\"radicals\"", index);
				if (index != -1)
				{
					sb.append("Parts:");
					line = page.substring(index, page.indexOf("</dl>", index));
					index2 = 0;
					while (index2 != -1)
					{
						index2 = line.indexOf("<a", index2 + 1);
						if (index2 == -1)
							break;

						sb.append(" ");
						sb.append(line.substring(line.indexOf(">", index2) + 1, line.indexOf("</a>", index2)));
					}
					sb.append("\n");
				}
				
				index = page.indexOf("class=\"dictionary_entry variants\"", index);
				if (index != -1)
				{
					sb.append("Variants:");
					line = page.substring(index, page.indexOf("</dl>", index));
					index2 = 0;
					while (index2 != -1)
					{
						index2 = line.indexOf("<a", index2 + 1);
						if (index2 == -1)
							break;

						sb.append(" ");
						sb.append(line.substring(line.indexOf(">", index2) + 1, line.indexOf("</a>", index2)));
					}
					sb.append("\n");
				}
				
				index = page.indexOf("class=\"kanji-details__main-meanings\"", index);
				index0 = page.indexOf(">", index);
				line = Html.fromHtml(page.substring(index0 + 1, page.indexOf("</div>", index0))).toString();
				
				kanji.meaning = line.split(",");
				for (index2 = 0; index2 < kanji.meaning.length; ++index2)
				{
					kanji.meaning[index2] = StringHelper.trim(kanji.meaning[index2]);
					if (kanji.meaning[index2].isEmpty())
					{
						tmp = new String[kanji.meaning.length - 1];
						if (index2 > 0)
							System.arraycopy(kanji.meaning, 0, tmp, 0, index2);
						if (index2 < tmp.length)
							System.arraycopy(kanji.meaning, index2 + 1, tmp, 0, tmp.length -  index2);
						kanji.meaning = tmp;
						index2--;
					}
				}
				
				index0 = page.indexOf("class=\"kanji-details__main-readings\"", index);
				for (index1 = 0; index1 < 2; index1++)
				{
					if (index1 == 0)
						index = page.indexOf("class=\"dictionary_entry kun_yomi\"", index0);
					else
						index = page.indexOf("class=\"dictionary_entry on_yomi\"", index0);
						
					if (index == -1)
						continue;
						
					line = page.substring(index, page.indexOf("</dl>", index));
					index2 = 0;
					while (index2 != -1)
					{
						index2 = line.indexOf("<a", index2 + 1);
						if (index2 == -1)
							break;
							
						reading.add(line.substring(line.indexOf(">", index2) + 1, line.indexOf("</a>", index2)).replace(".", "・"));
					}
						
					if (index1 == 0)
						kanji.reading_kun = reading.toArray(new String[reading.size()]);
					else
						kanji.reading_on = reading.toArray(new String[reading.size()]);
					
					reading.clear();
				}
				
				sb.append("\n");
				index = page.indexOf("class=\"grade\"", index);
				if (index != -1)
				{
					sb.append(StringHelper.trim(page.substring(page.indexOf(">", index) + 1, page.indexOf("</div>", index)).replaceAll("</?strong>", "")));
					sb.append("\n");
				}
				
				index = page.indexOf("class=\"jlpt\"", index);
				if (index != -1)
				{
					sb.append(StringHelper.trim(page.substring(page.indexOf(">", index) + 1, page.indexOf("</div>", index)).replaceAll("</?strong>", "")));
					sb.append("\n");
				}
				
				if (kanji.reading_kun == null)
					kanji.reading_kun = new String[0];
				
				if (kanji.reading_on == null)
					kanji.reading_on = new String[0];
				
				kanji.notes = StringHelper.trim(sb.toString());
				kanji.imageFile = "";
				kanji.meaning_used = new int[kanji.meaning.length];
				kanji.timesChecked_reading = new int[kanji.reading_kun.length + kanji.reading_on.length];
				kanji.timesCorrect_reading = new int[kanji.reading_kun.length + kanji.reading_on.length];
				kanji.added = System.currentTimeMillis();
				kanji.nextReview = kanji.lastChecked + Vocabulary.getNextReview(kanji.category);
				kanji.category_history = new int[32];
				kanji.category_history[kanji.category_history.length - 1] = kanji.category;
				for (int i = 0; i < kanji.category_history.length - 1; ++i)
					kanji.category_history[i] = -1;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
			
			return kanji;
		}

		@Override
		protected void onPostExecute(Kanji kanji)
		{
			listener.onProcessSuccess(kanji);
		}
	}
	
	public static class FindReadingCompoundsTask extends AsyncTask<Character, Void, View>
	{
		final OnProcessSuccessListener listener;
		final Context context;

		public FindReadingCompoundsTask(Context context, OnProcessSuccessListener listener)
		{
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected View doInBackground(Character...  p1)
		{
			int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			TextView v;
			
			try
			{
				HttpClient httpclient = new DefaultHttpClient(); 
				HttpGet httpget = new HttpGet();
				HttpResponse response;
				HttpEntity entity;
				BufferedReader reader;
				StringBuilder sb = new StringBuilder();
				String line, page;
				int index, index1, index2;
				
				httpget.setURI(URI.create("http://jisho.org/search/" + URLEncoder.encode(p1[0] + " #kanji", "UTF-8").replace("+", "%20")));
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
				
				index = page.indexOf("class=\"row compounds\"");
				for (index1 = 0; index1 < 3; index1++)
				{
					sb.setLength(0);
					index = page.indexOf(index1 == 2 ? "class=\"dictionary_entry nanori\"" : "class=\"no-bullet\"", index + 1);
					
					if (index == -1)
						continue;

					line = page.substring(index, page.indexOf(index1 == 2 ? "</dl>" : "</ul>", index));
					index2 = 0;
					while (index2 != -1)
					{
						index2 = line.indexOf(index1 == 2 ? "<dd" : "<li", index2 + 1);
						if (index2 == -1)
							break;

						sb.append(StringHelper.trim(line.substring(line.indexOf(">", index2) + 1, line.indexOf(index1 == 2 ? "</dd>" : "</li>", index2))).replace("】", "】- "));
						sb.append("<br />");
					}

					line = StringHelper.trim(sb.toString());
					
					if (!line.isEmpty())
					{
						v = new TextView(context);
						v.setText(Html.fromHtml(sb.toString()));
						v.setPadding(padding, padding / 2, padding, padding);
						v.setTextLocale(Locale.JAPANESE);
						v.setTextIsSelectable(true);
						layout.addView(v, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));	
						
						v = new TextView(context);
						v.setText(index1 == 0 ? "On reading compounds" : index1 == 1 ? "Kun reading compounds" : "Japanese names");
						v.setPadding(padding, 0, padding, 0);
						v.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
						layout.addView(v, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));	
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();

				v = new TextView(context);
				v.setText(e.toString());
				v.setPadding(padding, padding, padding, padding);
				layout.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));	
			}
			
			if (layout.getChildCount() == 0)
			{
				v = new TextView(context);
				v.setPadding(padding, padding, padding, padding);
				v.setText("Couln't find any reading compounds on jisho.org");
				layout.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));	
			}
			
			layout.setTag("words");

			return layout;
		}

		@Override
		protected void onPostExecute(View v)
		{
			listener.onProcessSuccess(v);
		}
	}
	

	public static class SearchKanjiTask extends AsyncTask<String, Void, char[]>
	{
		final OnProcessSuccessListener listener;
		final Context context;

		public SearchKanjiTask(Context context, OnProcessSuccessListener listener)
		{
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected char[] doInBackground(String...  p1)
		{
			try
			{
				HttpClient httpclient = new DefaultHttpClient(); 
				HttpGet httpget = new HttpGet();
				HttpResponse response;
				HttpEntity entity;
				BufferedReader reader;
				StringBuilder sb = new StringBuilder();
				String page, line;
				String url = "http://jisho.org/search/" + URLEncoder.encode(p1[0] + " #kanji", "UTF-8").replace("+", "%20");
				int index, index0;
				ArrayList<Character> kanjiList = new ArrayList<>();
				
				while (true)
				{
					httpget.setURI(URI.create(url));
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
					while (true)
					{
						index = page.indexOf("class=\"character\"", index + 1);
						if (index == -1)
							break;

						index0 = page.indexOf(">", index);
						kanjiList.add(page.substring(index0 + 1, page.indexOf("<", index0)).charAt(0));
					}
					
					index = 0;
					while (true)
					{
						index = page.indexOf("class=\"character literal japanese_gothic\"", index + 1);
						if (index == -1)
							break;
							
						index0 = page.indexOf(">", page.indexOf("<a", index));
						kanjiList.add(page.substring(index0 + 1, page.indexOf("<", index0)).charAt(0));
					}
							
					index = page.indexOf("class=\"more\"");
					if (index == -1)
						break;
						
					index0 = page.indexOf("href=\"", index);
					url = page.substring(index0 + 6, page.indexOf("\"", index0 + 6));
					
					if (url.startsWith("//"))
						url = "http:" + url;
				}
				
				char[] kanji = new char[kanjiList.size()];
				for (int i = 0; i < kanji.length; ++i)
					kanji[i] = kanjiList.get(i);
				return kanji;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(char[] kanji)
		{
			listener.onProcessSuccess(kanji);
		}
	}
	
	public static class SearchVocabularyTask extends AsyncTask<String, Void, ArrayList<String>>
	{
		final OnProcessSuccessListener listener;
		final Context context;

		public SearchVocabularyTask(Context context, OnProcessSuccessListener listener)
		{
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected ArrayList<String> doInBackground(String...  p1)
		{
			try
			{
				HttpClient httpclient = new DefaultHttpClient(); 
				HttpGet httpget = new HttpGet();
				HttpResponse response;
				HttpEntity entity;
				BufferedReader reader;
				StringBuilder sb = new StringBuilder();
				String page, line;
				String url = "http://jisho.org/search/" + URLEncoder.encode(p1[0] + " #words", "UTF-8").replace("+", "%20");
				int index, index0;
				ArrayList<String> vocabularyList = new ArrayList<>();

				while (true)
				{
					httpget.setURI(URI.create(url));
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
					while (true)
					{
						index = page.indexOf("<span class=\"text\">", index + 1);
						index0 = page.indexOf("</div>", index);
						
						if (index == -1 || index0 == -1)
							break;
						
						vocabularyList.add(StringHelper.trim(Html.fromHtml(page.substring(index, index0)).toString()));
					}

					index = page.indexOf("class=\"more\"");
					if (index == -1)
						break;

					index0 = page.indexOf("href=\"", index);
					url = page.substring(index0 + 6, page.indexOf("\"", index0 + 6));
					if (url.startsWith("//"))
						url = "http:" + url;
				}

				return vocabularyList;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<String> vocabularies)
		{
			listener.onProcessSuccess(vocabularies);
		}
	}
	
	public static class ImportVocabularyTask extends AsyncTask<Vocabulary, Void, Vocabulary>
	{
		final OnProcessSuccessListener listener;
		final Context context;

		public ImportVocabularyTask(Context context, OnProcessSuccessListener listener)
		{
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected Vocabulary doInBackground(Vocabulary...  p1)
		{
			Vocabulary v = p1[0];

			try
			{
				HttpClient httpclient = new DefaultHttpClient(); 
				HttpGet httpget = new HttpGet();
				HttpResponse response;
				HttpEntity entity;
				BufferedReader reader;
				StringBuilder sb = new StringBuilder();
				String line, page;
				int index, index0, index1, index2, index3;
		
				httpget.setURI(URI.create("http://jisho.org/search/" + URLEncoder.encode(v.kanji + " #words", "UTF-8").replace("+", "%20")));
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
				sb.setLength(0);

				index = page.indexOf("class=\"furigana\"");
				index = page.indexOf(">", index) + 1;
				index0 = page.indexOf("<span class=\"text\">", index);
				v.reading = new String[] { StringHelper.trim(Html.fromHtml(page.substring(index, index0)).toString()) };
				index = page.indexOf("</div>", index0);
				v.kanji = StringHelper.trim(Html.fromHtml(page.substring(index0, index)).toString());
				v.reading[0] = StringHelper.replaceWithFurigana(v.kanji, v.reading[0], false, false);
				
				index = page.indexOf("class=\"concept_light-status", index);
				index0 = page.indexOf("class=\"concept_light-status_link\"", index);
				
				String tags = page.substring(page.indexOf("\"", index + 10) + 1, index0 - 3);
				if (tags.contains("Common"))
					v.notes = "Common Word";
				if (tags.contains("N5"))
					v.notes = (v.notes == null ? "" : v.notes + "\n") + "JLPT level N5";
				if (tags.contains("N4"))
					v.notes = (v.notes == null ? "" : v.notes + "\n") + "JLPT level N4";
				if (tags.contains("N3"))
					v.notes = (v.notes == null ? "" : v.notes + "\n") + "JLPT level N3";
				if (tags.contains("N2"))
					v.notes = (v.notes == null ? "" : v.notes + "\n") + "JLPT level N2";
				if (tags.contains("N1"))
					v.notes = (v.notes == null ? "" : v.notes + "\n") + "JLPT level N1";
				
				index2 = page.indexOf("class=\"light-details_link\"", index);
					
				String tmp[];
				boolean otherForms, notes, trans = false, intrans = false;
				ArrayList<String> meanings = new ArrayList<>();
				
				v.type = VocabularyType.NONE;
				v.additionalInfo = "";
				
				int timeout = 0;
				while (true)
				{
					otherForms = false;
					notes = false;
					
					if (timeout++ > 50)
						return null;
					
					index0 = page.indexOf("class=\"meaning-tags\"", index);
					index1 = index0 == -1 ? -1 : page.indexOf("<", index0);
					index3 = page.indexOf("class=\"meaning-meaning\"", index);
					
					if (index1 != -1 && index1 < index2 && (index3 == -1 || index3 > index0))
					{
						tmp = page.substring(index0 + 21, index1).split(", ");
						for (String tag : tmp)
						{
							if (tag.equalsIgnoreCase("Other Forms"))
								otherForms = true;
								
							else if (tag.equalsIgnoreCase("Notes"))
								notes = true;
								
							else if (tag.equalsIgnoreCase("Na-adjective"))
							{
								v.type = VocabularyType.NA_ADJECTIVE;
							}
							else if (tag.contains("Noun") && v.type == VocabularyType.NONE)
								v.type = VocabularyType.NOUN;
								
							else if (tag.equalsIgnoreCase("No-adjective") && v.type == VocabularyType.NONE)
							{
								v.type = VocabularyType.OTHER;
								v.additionalInfo = v.additionalInfo + "; No-adjective";
							}
							else if (tag.equalsIgnoreCase("Conjunction") && v.type == VocabularyType.NONE)
								v.type = VocabularyType.CONJUNCTION;
							
							else if (tag.equalsIgnoreCase("Expression") && v.type == VocabularyType.NONE)
								v.type = VocabularyType.EXPRESSION;
							
							else if (tag.equalsIgnoreCase("I-adjective"))
								v.type = VocabularyType.I_ADJECTIVE;
							
							else if (tag.equalsIgnoreCase("Adverb"))
								v.type = VocabularyType.ADVERB;
							
							else if (tag.equalsIgnoreCase("Counter") && v.type == VocabularyType.NONE)
								v.type = VocabularyType.COUNTER;
							
							else if (tag.equalsIgnoreCase("Particle") && v.type == VocabularyType.NONE)
								v.type = VocabularyType.PARTICLE;
								
							else if (tag.contains("'to' particle"))
							{
								v.type = VocabularyType.ADVERB;
								v.additionalInfo = v.additionalInfo + "; takes the 'to' particle";
							}
							else if (tag.contains("Godan verb"))
								v.type = VocabularyType.U_VERB;
								
							else if (tag.contains("Ichidan verb"))
								v.type = VocabularyType.RU_VERB;
								
							else if (tag.contains("Suru"))
								v.additionalInfo = v.additionalInfo + "; Suru verb";

							else if (tag.contains("Special class"))
							{
								v.type = VocabularyType.OTHER;
								v.additionalInfo = v.additionalInfo + "; special class";
							}
							else if (tag.contains("intransitive"))
								intrans = true;
								
							else if (tag.contains("Transitive"))
								trans = true;
								
							if (tag.contains("prefix"))
								v.additionalInfo = v.additionalInfo + "; used as prefix";
								
							else if (tag.contains("suffix"))
								v.additionalInfo = v.additionalInfo + "; used as suffix";
							
						}
						
						index = index1;
					}
					
					index0 = notes ? page.indexOf("class=\"\"", index) + 9 : index3 + 24;
					index1 = index0 <= 23 ? -1 : page.indexOf("</span>", index0);
					
					if (index1 == -1 || index1 > index2)
						break;
					 
					if (otherForms)
						v.notes = (v.notes == null ? "" : v.notes + "\n\n") + "Other forms:\n" + Html.fromHtml(page.substring(index0, page.indexOf("</div>", index0))).toString().replace("、", "\n");
					
					else if (notes)
						v.notes = (v.notes == null ? "" : v.notes + "\n\n") + Html.fromHtml(page.substring(index0, page.indexOf("</div>", index0))).toString().replace("、", "\n");
					
					else
					{
						tmp = Html.fromHtml(page.substring(index0, index1)).toString().split(";");
						
						for (String meaning : tmp)
							meanings.add(StringHelper.trim(meaning));
					}
					
					index = index1;
				}
				
				if (meanings.size() > 1 && StringHelper.similiarMeaning(meanings.get(meanings.size() - 1), meanings.get(meanings.size() - 2)))
					meanings.remove(meanings.size() - 1);
				
				v.meaning = meanings.toArray(new String[meanings.size()]);
				
				if (trans)
					v.additionalInfo = v.additionalInfo + "; transitive";
				
				if (intrans)
					v.additionalInfo = v.additionalInfo + "; intransitive";
				
				if (v.additionalInfo != null && v.additionalInfo.startsWith("; "))
					v.additionalInfo = v.additionalInfo.substring(2);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}

			return v;
		}

		@Override
		protected void onPostExecute(Vocabulary vocabulary)
		{
			listener.onProcessSuccess(vocabulary);
		}
	}
	
}
