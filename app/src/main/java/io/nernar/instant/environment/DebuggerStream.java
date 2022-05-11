package io.nernar.instant.environment;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.ScrollView;
import com.zhekasmirnov.horizon.runtime.logger.CoreLogger;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import java.io.IOException;
import android.widget.TextView;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.PrintStream;

public class DebuggerStream extends PrintStream {
	private final TextView out;
	private final ScrollView scroll;
	
	public DebuggerStream(TextView out, ScrollView scroll) {
		super(System.out);
		this.out = out;
		this.scroll = scroll;
	}
	
	public void flush() {}
	public void close() {}
	public boolean checkError() {
		return false;
	}
	protected void setError() {}
	protected void clearError() {}
	public void write(int b) {}
	public void write(byte[] buf, int off, int len) {}
	
	public void print(boolean b) {
		print(Boolean.toString(b));
	}
	
	public void print(char c) {
		print(Character.toString(c));
	}
	
	public void print(int i) {
		print(Integer.toString(i));
	}
	
	public void print(long l) {
		print(Long.toString(l));
	}
	
	public void print(float f) {
		print(Float.toString(f));
	}
	
	public void print(double d) {
		print(Double.toString(d));
	}
	
	public void print(char[] s) {
		print(String.valueOf(s));
	}
	
	private int resolveStatusColor(CharSequence where) {
		if (where.charAt(0) == '[') {
			String who = where.toString();
			int slashIndex = who.indexOf('/');
			int closeBracketIndex = who.indexOf(']');
			if (slashIndex != -1 && slashIndex < closeBracketIndex) {
				switch (who.substring(1, slashIndex)) {
					case "WARNING":
						return Color.YELLOW;
					case "ERROR":
						return Color.RED;
					case "DEBUG":
						return Color.GRAY;
					case "INFO":
						return Color.GREEN;
					case "MOD":
						return 0;
				}
				switch (who.substring(slashIndex + 1, closeBracketIndex)) {
					case "D":
						return Color.LTGRAY;
					case "I":
						return Color.GREEN;
					case "E":
						return Color.RED;
				}
			}
		}
		return 0;
	}
	
	public void print(String s) {
		if (s != null && s.length() > 0) {
			out.post(new Runnable() {
				@Override
				public void run() {
					int statusColor = resolveStatusColor(s);
					if (statusColor != 0) {
						SpannableString spannable = new SpannableString(s);
						spannable.setSpan(new ForegroundColorSpan(statusColor), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						out.append(spannable);
					} else {
						out.append(s);
					}
					scroll.scrollTo(0, out.getMeasuredHeight());
				}
			});
		}
	}
	
	public void print(Object obj) {
		print(obj != null ? obj.toString() : "null");
	}
	
	public void println() {
		print("\n");
	}
	
	public void println(boolean x) {
		println(Boolean.toString(x));
	}
	
	public void println(char x) {
		println(Character.toString(x));
	}
	
	public void println(int x) {
		println(Integer.toString(x));
	}
	
	public void println(long x) {
		println(Long.toString(x));
	}
	
	public void println(float x) {
		println(Float.toString(x));
	}
	
	public void println(double x) {
		println(Double.toString(x));
	}
	
	public void println(char[] x) {
		println(String.valueOf(x));
	}
	
	public void println(String x) {
		print(x + "\n");
	}
	
	public void println(Object x) {
		println(x != null ? x.toString() : "null");
	}
	
	public PrintStream append(CharSequence csq) {
		print(csq);
		return this;
	}
	
	public PrintStream append(CharSequence csq, int start, int end) {
		print(csq != null ? csq.subSequence(start, end) : "null");
		return this;
	}
	
	public PrintStream append(char c) {
		append(Character.toString(c));
		return this;
	}
}
