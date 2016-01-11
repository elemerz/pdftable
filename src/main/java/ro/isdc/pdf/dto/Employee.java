package ro.isdc.pdf.dto;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Employee {
	private static DateFormat formatter = DateFormat.getDateInstance();
	
	private int empNo=0;
	private String firstName="";
	private String lastName="";
	private Date birthDate=null;
	private String gender="";
	private List<Role> roles= new ArrayList<Role>(3);
	
	public Employee(int empNo, String firstName, String lastName, String gender, Date birthDate) {
		super();
		this.empNo = empNo;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.gender = gender;
	}
	
	public String getStrValueForTable(final int fieldIndex) {
		if (fieldIndex < 0 || fieldIndex > 3) {
			throw new IllegalArgumentException("fieldIndex for Employee invalid");
		}
		switch (fieldIndex) {
		case 0:
			return firstName;
		case 1:
			return lastName;
		case 2:
			return formatter.format(birthDate);
		case 3:
			return gender;
		}
		return null;
	}
	
	public int getEmpNo() {
		return empNo;
	}
	public void setEmpNo(int empNo) {
		this.empNo = empNo;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public List<Role> getRoles() {
		return roles;
	}
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
	
}
