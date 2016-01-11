package ro.isdc.pdf.dto;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

public class Role implements Serializable{
	
	private static DateFormat formatter = DateFormat.getDateInstance();

	/**Serial Version ID.*/
	private static final long serialVersionUID = 1370434411321310714L;
	private String title="";
	private Date fromDate;
	private Date toDate;
	
	public Role(String title, Date fromDate, Date toDate) {
		this.title = title;
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
	
	public String getStrValueForTable(final int fieldIndex) {
		if (fieldIndex < 0 || fieldIndex > 2) {
			throw new IllegalArgumentException("fieldIndex for Role invalid");
		}
		switch (fieldIndex) {
		case 0:
			return formatter.format(fromDate);
		case 1:
			return formatter.format(toDate);
		case 2:
			return title;
		}
		return null;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	
	
}
