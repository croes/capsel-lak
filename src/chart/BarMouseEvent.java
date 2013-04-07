package chart;

public class BarMouseEvent {
	
	private final AbstractBarChart source;
	private final String title;
	private final int year;

	BarMouseEvent(AbstractBarChart source, String title, int year) {
		this.source = source;
		this.title = title;
		this.year = year;
	}
	
	public AbstractBarChart getSource() {
		return source;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getYear() {
		return year;
	}
	
}
