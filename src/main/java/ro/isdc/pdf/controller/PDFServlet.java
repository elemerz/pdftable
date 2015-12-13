package ro.isdc.pdf.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFServlet extends HttpServlet {

	/** Serialversion UID. */
	private static final long serialVersionUID = -205214872476343901L;
	private JdbcTemplate db=null;
	
	public void init() throws ServletException {
		String dbUrl=this.getInitParameter("dbUrl");
		String dbUser=this.getInitParameter("dbUser");;
		String dbPassword=this.getInitParameter("dbPassword");;
		
		this.db=createJdbcTemplate(dbUrl,dbUser,dbPassword);
	}

	private JdbcTemplate createJdbcTemplate(final String dbUrl,final String dbUser,final String dbPassword) {
		Driver dbDriver;
		JdbcTemplate jdbcTemplate=null;
		try {
			dbDriver = new com.mysql.jdbc.Driver();
			SimpleDriverDataSource dataSource = new SimpleDriverDataSource(dbDriver,dbUrl,dbUser,dbPassword);
			jdbcTemplate = new JdbcTemplate(dataSource);		
			jdbcTemplate.setDataSource(dataSource);		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return jdbcTemplate;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/pdf");
		try {
			generatePDF(response.getOutputStream());
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	private void generatePDF(OutputStream out) throws FileNotFoundException, DocumentException {
		String sqlAllEmployees = "SELECT e.emp_no,e.birth_date,e.first_name,e.last_name,e.gender,e.hire_date FROM employees.employees as e";
		final PdfPTable table = new PdfPTable(3);
        this.db.query(sqlAllEmployees, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
            	table.addCell(rs.getString("emp_no"));
            	table.addCell(rs.getString("first_name"));
            	table.addCell(rs.getString("last_name"));
            }
        });
        //Create a PDF document and put the table in it:
		Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(table);
        document.close();	}
}
