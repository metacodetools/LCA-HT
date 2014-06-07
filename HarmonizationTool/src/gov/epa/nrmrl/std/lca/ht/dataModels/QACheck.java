package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//import org.eclipse.swt.widgets.TableColumn;

public class QACheck {
	private String description;
	private String explanation;
	private String suggestion;
	private Pattern pattern;
	private String replacement;
	private boolean patternMustMatch;

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public QACheck(String description, String explanation, String suggestion, Pattern pattern, String replacement, boolean patternMustMatch) {
		this.description = description;
		this.explanation = explanation;
		this.suggestion = suggestion;
		this.pattern = pattern;
		this.replacement = replacement;
		this.patternMustMatch = patternMustMatch;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public static List<QACheck> getGeneralQAChecks() {
		List<QACheck> qaCheckPack = new ArrayList<QACheck>();
		String d1 = "Bookend quotes";
		String e1 = "The text is surrounded by apparently superfluous double quote marks.";
		String s1 = "Remove these quote marks";
		Pattern p1 = Pattern.compile("^\"([^\"]*)\"$");
		String r1 = "$1";
		qaCheckPack.add(new QACheck(d1, e1, s1, p1, r1, false));

		String d2 = "Leading and trailing space(s)";
		String e2 = "At least one white space character occurs both before and after text.";
		String s2 = "Remove leading and trailing space(s)";
		Pattern p2 = Pattern.compile("^\\s+(.*?)\\s+$");
		String r2 = "$1";
		qaCheckPack.add(new QACheck(d2, e2, s2, p2, r2, false));

		String d3 = "Leading space(s)";
		String e3 = "At least one white space character occurs before text.";
		String s3 = "Remove leading space(s)";
		Pattern p3 = Pattern.compile("^\\s+(.*)$");
		String r3 = "$1";
		qaCheckPack.add(new QACheck(d3, e3, s3, p3, r3, false));

		String d4 = "Trailing space(s)";
		String e4 = "At least one white space character occurs after text.";
		String s4 = "Remove leading space(s)";
		Pattern p4 = Pattern.compile("^(.*?)\\s+$");
		String r4 = "$1";
		qaCheckPack.add(new QACheck(d4, e4, s4, p4, r4, false));
		return qaCheckPack;
	}

	public boolean isPatternMustMatch() {
		return patternMustMatch;
	}

	public void setPatternMustMatch(boolean patternMustMatch) {
		this.patternMustMatch = patternMustMatch;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	// public static void checkColumn(TableColumn column) {
	// Object data = column.getData();
	// System.out.println("data: " + data);
	// System.out.println("data.getClass(): " + data.getClass());
	// }

	// public static List<QACheck> getQAChecks(CsvTableViewerColumnType type) {
	// return getGeneralQAChecks();
	// }

}
