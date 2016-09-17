package com.taraxippus.vocab.util;
import java.util.*;

public class StringHelper
{
	public static String trim(String oldString) 
	{
		final int length = oldString.length();
		
		if (length == 0)
			return oldString;
		
		int offset = 0, offset2 = length - 1;
		
		int codePoint = oldString.codePointAt(offset);
		if (isWhitespace(codePoint))
			for (; offset < length ;)
			{
				codePoint = oldString.codePointAt(offset);

				if (!isWhitespace(codePoint))
					break;
		
				offset += Character.charCount(codePoint);
			}
		
		codePoint = oldString.codePointAt(offset2);
		
		if (isWhitespace(codePoint))
			for (; offset2 > offset ;)
			{
				codePoint = oldString.codePointAt(offset2);

				if (!isWhitespace(codePoint))
					break;

				offset2 -= Character.charCount(codePoint);
			}
		
		return offset == offset2 + 1 ? "" : oldString.substring(offset, offset2 + 1);
	}

	public static boolean isWhitespace(int c)
	{
		return c >= 0x0009 && c <= 0x000D
			|| c == 0x0085 || c == 0x2028 || c == 0x2029 || c == 0x0020 
			|| c == 0x3000 || c == 0x1680 || c >= 0x2000 && c <= 0x2006
			|| c >= 0x2008 && c <= 0x200A || c == 0x205F || c == 0x00A0 
			|| c == 0x2007 || c == 0x202F;
	}
	
	public static boolean isKana(String s)
	{
		String trimed = trim(s);

		char c;

		for (int i = 0; i < trimed.length(); ++i)
		{
			c = trimed.charAt(i);

			if (c != '・' && Character.UnicodeBlock.of(c) != Character.UnicodeBlock.HIRAGANA && Character.UnicodeBlock.of(c) != Character.UnicodeBlock.KATAKANA)
			{
				return false;
			}
		}

		return !s.isEmpty();
	}

	public static boolean isKana(char c)
	{
		return c == '・' || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA;
	}
	
	public static boolean isKanji(char c)
	{
		return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || c == '々';
	}
	
	public static boolean isKanaOrKanji(String s)
	{
		String trimed = trim(s);

		char c;

		for (int i = 0; i < trimed.length(); ++i)
		{
			c = trimed.charAt(i);

			if (c != '・' && Character.UnicodeBlock.of(c) != Character.UnicodeBlock.HIRAGANA && Character.UnicodeBlock.of(c) != Character.UnicodeBlock.KATAKANA && !isKanji(c))
			{
				return false;
			}
		}

		return !s.isEmpty();
	}
	

	public static char[] getKanji(String kanji_string)
	{
		final ArrayList<Character> kanji_list = new ArrayList<>();
		
		for (int i = 0; i < kanji_string.length(); ++i)
		{
			if (isKanji(kanji_string.charAt(i)))
			{
				kanji_list.add(kanji_string.charAt(i));
			}
		}
		
		final char[] kanji = new char[kanji_list.size()];
		
		for (int i = 0; i < kanji_list.size(); ++i)
		{
			kanji[i] = kanji_list.get(i);
		}
		
		return kanji;
	}
	
	public static String toHiragana(String s)
	{
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < s.length(); ++i)
			builder.append(toHiragana(s.charAt(i)));

		return builder.toString();
	}

	public static char toHiragana(char c) 
	{
		if (c == '・')
		{
			return c;
		}
		if (('\u30a1' <= c) && (c <= '\u30fe')) 
		{
			return (char) (c - 0x60);
		}
		else if (('\uff66' <= c) && (c <= '\uff9d'))
		{
			return (char) (c - 0xcf25);
		}
		return c;
	}

	public static boolean equalsKana(String s1, String s2)
	{
		return s1.replace("・", "").equals(s2.replace("・", ""));
	}
	
	public static boolean similiarMeaning(String s1, String s2)
	{
		if (s1.length() < 3 || Math.abs(s1.length() - s2.length()) > 1)
			return false;

		int match = 0;

		if (s1.length() > s2.length())
		{
			for (int i = 0; i < s2.length(); ++i)
			{
				if (s1.charAt(i) == s2.charAt(i))
				{
					match++;
				}
				else
					break;
			}

			for (int i = 0; i < s2.length(); ++i)
			{
				if (s1.charAt(s1.length() - i - 1) == s2.charAt(s2.length() - 1 - i))
				{
					match++;
				}
				else
					break;
			}

			return match >= s2.length();
		}
		else
		{
			for (int i = 0; i < s1.length(); ++i)
			{
				if (s2.charAt(i) == s1.charAt(i))
				{
					match++;
				}
				else
					break;
			}

			for (int i = 0; i < s1.length(); ++i)
			{
				if (s2.charAt(s2.length() - i - 1) == s1.charAt(s1.length() - 1 - i))
				{
					match++;
				}
				else
					break;
			}

			return match >= s1.length();
		}
	}
	
	public static String toRomaji(char hiragana)
	{
		switch (hiragana)
		{
			case 'す':
				return "su";
			case 'く':
				return "ku";
			case 'ぐ':
				return "gu";
			case 'む':
				return "mu";
			case 'ぶ':
				return "bu";
			case 'ぬ':
				return "nu";
			case 'る':
				return "ru";
			case 'う':
				return "u";
			case 'つ':
				return "tsu";
			default:
				return "" + hiragana;
		}
	}

	public static boolean similiarKanji(String s1, String s2)
	{
		if (isKana(s1))
		{
			return false;
		}

		int kanjiCount = 0;
		for (int i = 0; i < s2.length(); ++i)
		{
			if (isKanji(s2.charAt(i)))
			{
				if (!s1.contains("" + s2.charAt(i)))
					return false;

				kanjiCount++;
			}

		}

		int kanjiCount2 = 0;
		for (int i = 0; i < s1.length(); ++i)
		{
			if (isKanji(s1.charAt(i)))
			{
				if (!s2.contains("" + s1.charAt(i)))
					return false;

				kanjiCount2++;
			}

		}

		return kanjiCount > 0 && kanjiCount == kanjiCount2;
	}
	
	public static String replaceWithFurigana(String kanji, String furigana, boolean addSeperatorStart, boolean addSeperatorEnd)
	{
		int start = -1, end = kanji.length();
		for (int i = 0; i < kanji.length(); ++i)
		{
			if (start == -1 && !isKana(kanji.charAt(i)))
				start = i;
				
			else if (start != -1 && end == kanji.length() && isKana(kanji.charAt(i)))
				end = i;
			
			else if (start != -1 && end != kanji.length() && !isKana(kanji.charAt(i)))
				end = kanji.length();
		}
		
		StringBuilder sb = new StringBuilder();
		if (start != 0)
			sb.append(kanji.substring(0, start + 1));
		if (addSeperatorStart)
			sb.append("・");
		sb.append(furigana);
		if (addSeperatorEnd)
			sb.append("・");
		if (end != kanji.length())
			sb.append(kanji.substring(end, kanji.length()));
			
		return sb.toString();
	}
	
	private static final char seperator = '\\';
	private static final String seperatorString = "\\";
	private static final String seperatorRegex = "\\\\";
	
	public static String toString(String[] in)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(seperator);
		
		for (int i = 0; i < in.length; ++i)
		{
			sb.append(in[i].replace(seperator, '/'));
			sb.append(seperator);
		}
		
		return sb.toString();
	}
	
	public static String toString(ArrayList<?> in)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(seperator);
		for (int i = 0; i < in.size(); ++i)
		{
			sb.append(in.get(i).toString().replace(seperator, '/'));
			sb.append(seperator);
		}

		return sb.toString();
	}
	
	public static String toString(int[] in)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(seperator);
		
		for (int i = 0; i < in.length; ++i)
		{
			sb.append(in[i]);
			sb.append(seperator);
		}

		return sb.toString();
	}
	
	public static String toString(boolean[] in)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(seperator);
		
		for (int i = 0; i < in.length; ++i)
		{
			sb.append(in[i] ? '1' : '0');
			sb.append(seperator);
		}

		return sb.toString();
	}
	
	public static String[] toStringArray(String in)
	{
		if (in == null || in.startsWith(seperatorString) ? in.length() <= 2 : in.isEmpty())
			return new String[0];
		
		if (in.startsWith(seperatorString))
			in = in.substring(1, in.length() - 1);
			
		return in.split(seperatorRegex);
	}
	
	public static int[] toIntArray(String in)
	{
		final String[] array = toStringArray(in);
		final int[] out = new int[array.length];
		
		for (int i = 0; i < out.length; ++i)
			out[i] = Integer.parseInt(array[i]);
		
		return out;
	}
	
	public static boolean[] toBooleanArray(String in)
	{
		final String[] array = toStringArray(in);
		final boolean[] out = new boolean[array.length];

		for (int i = 0; i < out.length; ++i)
			out[i] = array[i].equals("1");

		return out;
	}
}
