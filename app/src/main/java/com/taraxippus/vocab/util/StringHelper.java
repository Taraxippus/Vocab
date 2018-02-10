package com.taraxippus.vocab.util;
import java.util.*;

public class StringHelper
{
	public static String trim(String oldString) 
	{
		if (oldString == null)
			return null;
		
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
	
	public static boolean containsOnly(String s, String charList)
	{
		for (int i = 0; i < s.length(); ++i)
			if (charList.indexOf(s.charAt(i)) == -1)
				return false;
				
		return true;
	}
	
	public static boolean isKana(String s)
	{
		if (s == null)
			return false;
			
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
			return c;
		
		if (('\u30a1' <= c) && (c <= '\u30fe')) 
			return (char) (c - 0x60);
		
		else if (('\uff66' <= c) && (c <= '\uff9d'))
			return (char) (c - 0xcf25);
			
		return c;
	}

	public static String toKatakana(String s)
	{
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < s.length(); ++i)
			builder.append(toKatakana(s.charAt(i)));

		return builder.toString();
	}

	public static char toKatakana(char c) 
	{
		if (c == '・')
			return c;
		
		if (('\u3041' <= c) && (c <= '\u309e')) 
			return (char) (c + 0x60);
			
		return c;
	}
	
	public static boolean equalsKana(String s1, String s2)
	{
		if (s1.length() != s2.length())
			return false;
			
		for (int i = 0; i < s1.length(); ++i)
			if (toHiragana(s1.charAt(i)) != toHiragana(s2.charAt(i)))
				return false;
		
		return true;
	}
	
	public static boolean equalsKanjiIgnoreKana(String s1, String s2)
	{
		int i1 = -1;
		s1:
		for (int i = 0; i < s1.length(); ++i)
		{
			if (!isKanji(s1.charAt(i)))
				continue;
			
			for (i1++; i1 < s2.length(); ++i1)
			{
				if (!isKanji(s2.charAt(i1)))
					continue;
					
				if (s1.charAt(i) == s2.charAt(i1))
					continue s1;
			}
			
			return false;
		}
		
		return true;
	}
	
	
	public static boolean similiarMeaning(String s1, String s2)
	{
		if (s1.length() < 3 || Math.abs(s1.length() - s2.length()) > 1)
			return false;

		int match = 0;

		if (s1.length() < s2.length())
		{
			String s3 = s2;
			s2 = s1;
			s1 = s3;
		}
			
		for (int i = 0; i < s2.length(); ++i)
		{
			if (Character.toLowerCase(s1.charAt(i)) == Character.toLowerCase(s2.charAt(i)))
			{
				match++;
			}
			else
				break;
		}
		
		for (int i = 0; i < s2.length(); ++i)
		{
			if (Character.toLowerCase(s1.charAt(s1.length() - i - 1)) == Character.toLowerCase(s2.charAt(s2.length() - 1 - i)))
			{
				match++;
			}
			else
				break;
		}

		return s2.length() > 10 ? match >= s2.length() - 1 : match >= s2.length();
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
		int start1 = -1, end1 = -1;
		for (int i = 0; i < kanji.length(); ++i)
		{
			if (start == -1 && !isKana(kanji.charAt(i)))
				start = i;
				
			else if (start != -1 && end == kanji.length() && isKana(kanji.charAt(i)))
			{
				if (start1 != -1 && end1 == -1)
					end1 = i - 1;
					
				end = i;
			}
			else if (start != -1 && end != kanji.length() && !isKana(kanji.charAt(i)))
			{
				start1 = end;
				end = kanji.length();
			}
		}
		
		if (start > 0 && furigana.startsWith(kanji.substring(0, start)) || end != kanji.length() && furigana.endsWith(kanji.substring(end)))
			return furigana;
			
		StringBuilder sb = new StringBuilder();
		if (start > 0)
			sb.append(kanji.substring(0, start + 1));
		if (addSeperatorStart && (start <= 0 || isKanaOrKanji(kanji.substring(start - 1, start))))
			sb.append("・");
		
		if (end1 != -1 && !furigana.contains(kanji.subSequence(start1, end1)))
		{
			sb.append(furigana.substring(0, furigana.length() / 2));
			if (addSeperatorStart || addSeperatorEnd)
				sb.append("・");
			sb.append(kanji.subSequence(start1, end1));
			if (addSeperatorStart || addSeperatorEnd)
				sb.append("・");
			sb.append(furigana.substring(furigana.length() / 2));
		}
		else
			sb.append(furigana);
			
		if (addSeperatorEnd && (end == kanji.length() || isKanaOrKanji(kanji.substring(end, end + 1))))
			sb.append("・");
		if (end != kanji.length())
			sb.append(kanji.substring(end, kanji.length()));
			
		return sb.toString();
	}
	
	public static final char seperator = '\\';
	public static final String seperatorString = "\\";
	public static final String seperatorRegex = "\\\\";
	
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
	
	public static int lengthOfArray(String in)
	{
		if (in == null || in.startsWith(seperatorString) ? in.length() <= 2 : in.isEmpty())
			return 0;

		int count = 0;
		if (in.startsWith(seperatorString))
			count = -1;

		for (int i = 0; i < in.length(); ++i)
			if (in.charAt(i) == seperator)
				count++;
			
		return count;
	}
}
