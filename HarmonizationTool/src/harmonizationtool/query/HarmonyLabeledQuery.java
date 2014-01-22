package harmonizationtool.query;

public class HarmonyLabeledQuery extends HarmonyQuery2Impl{
	private String label = null;

	public HarmonyLabeledQuery(String query, String label) {
		super(query);
		this.label = label;
	}
	public HarmonyLabeledQuery(String query, String parameterToken, String label){
		super(query, parameterToken);
		this.label = label;
	}
	public String getLabel() {
		return label;
	}

}
