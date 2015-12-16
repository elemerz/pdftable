package ro.isdc.pdf.dto;

import java.io.Serializable;
import java.util.Date;

public class Role implements Serializable{
	
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
