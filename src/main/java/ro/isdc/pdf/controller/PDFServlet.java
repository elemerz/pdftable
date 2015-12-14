package ro.isdc.pdf.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFServlet extends HttpServlet {

	/** Serialversion UID. */
	private static final long serialVersionUID = -205214872476343901L;
	private JdbcTemplate db = null;

	public void init() throws ServletException {
		String dbUrl = this.getInitParameter("dbUrl");
		String dbUser = this.getInitParameter("dbUser");
		;
		String dbPassword = this.getInitParameter("dbPassword");
		;

		this.db = createJdbcTemplate(dbUrl, dbUser, dbPassword);
	}

	private JdbcTemplate createJdbcTemplate(final String dbUrl, final String dbUser, final String dbPassword) {
		Driver dbDriver;
		JdbcTemplate jdbcTemplate = null;
		try {
			dbDriver = new com.mysql.jdbc.Driver();
			SimpleDriverDataSource dataSource = new SimpleDriverDataSource(dbDriver, dbUrl, dbUser, dbPassword);
			jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.setDataSource(dataSource);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return jdbcTemplate;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int lineCount = Integer.valueOf(request.getParameter("lineCount"));
		int columnCount = Integer.valueOf(request.getParameter("columnCount"));
		String mode = request.getParameter("mode");

		// response.setContentType("application/pdf");
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition:", "attachment; filename=table-" + lineCount + "-lines-" + columnCount + "-cols.pdf");
		try {
			if ("i-text".equals(mode)) {
				generatePDFIText(response.getOutputStream(), lineCount, columnCount);
			} else if ("flying-saucer".equals(mode)) {
				generatePDFFlyingSaucer(response.getOutputStream(), lineCount, columnCount);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private void generatePDFFlyingSaucer(ServletOutputStream outputStream, int lineCount, int columnCount) {
		String htmlContent = generateHTMLContent(lineCount, columnCount);
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocumentFromString(htmlContent);
		renderer.layout();
		try {
			renderer.createPDF(outputStream);
		} catch (com.lowagie.text.DocumentException e) {
			e.printStackTrace();
		}
	}
	
	private String generateHTMLContent(int lineCount, int columnCount) {
		return "<html><body><h6>Table comes here</h6></body></html>";
	}

	private void generatePDFIText(OutputStream out, final int lineCount, final int columnCount) throws FileNotFoundException, DocumentException {
		long startTime = System.currentTimeMillis();
		String sqlAllEmployees = "SELECT e.emp_no,e.birth_date,e.first_name,e.last_name,e.gender,e.hire_date FROM employees.employees as e LIMIT " + lineCount;
		final PdfPTable table = new PdfPTable(columnCount);
		this.db.query(sqlAllEmployees, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				for (int i = 1; i <= columnCount; i++) {
					table.addCell(rs.getString(i));
				}
			}
		});
		// Create a PDF document and put the table in it:
		Document document = new Document();
		PdfWriter.getInstance(document, out);
		document.open();
		document.add(table);
		document.close();
		System.out.println("PDF generation with " + lineCount + " took " + (System.currentTimeMillis() - startTime) + " milliseconds.");
	}
}
