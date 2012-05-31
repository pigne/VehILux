package lu.uni.routegeneration.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class TextFileParser {
	
	public static List<String> readStringList(String fileName) {
		List<String> strings = new ArrayList<String>();
		DataInputStream in;
		try {
			in = new DataInputStream(new FileInputStream(fileName));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				strLine.trim();
				strings.add(strLine);
			}
			in.close();
			return strings;
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			return null;
		}
	}
	
	public static void writeStringList(List<String> strings, String fileName) {
		DataOutputStream out;
		try {
			out = new DataOutputStream(new FileOutputStream(fileName));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (String string : strings) {
				bw.write(string);
				bw.newLine();
			}
			bw.flush();
			out.close();
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}
