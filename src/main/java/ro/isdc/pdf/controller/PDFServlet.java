package ro.isdc.pdf.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import ro.isdc.pdf.dto.Employee;
import ro.isdc.pdf.dto.Role;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFServlet extends HttpServlet {

	/** Serialversion UID. */
	private static final long serialVersionUID = -205214872476343901L;
	private JdbcTemplate db = null;
	
	public void init() throws ServletException {
		String dbUrl = this.getInitParameter("dbUrl");
		String dbUser = this.getInitParameter("dbUser");
		String dbPassword = this.getInitParameter("dbPassword");
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
		ByteArrayOutputStream baos= new ByteArrayOutputStream(10000);
		WebContext ctx = new WebContext(request, response, getServletConfig().getServletContext(), request.getLocale());
		long startTime = System.currentTimeMillis();
		try {
			if ("i-text".equals(mode)) {
				baos=generatePDFIText(lineCount, columnCount);
			} else if ("flying-saucer".equals(mode)) {
				baos=generatePDFFlyingSaucer(lineCount, columnCount);
			} else if ("pdf-box".equals(mode)) {
				baos=generatePDFPDFBox(lineCount, columnCount);
			} else if ("i-text-tt".equals(mode)) {
				baos=generatePDFITextTabInTab(lineCount);
			} else if ("flying-saucer-tt".equals(mode)) {
				baos=generatePDFFlyingSaucerTabInTab(lineCount,ctx);
			} else if ("pdf-box-tt".equals(mode)) {
				//baos=generatePDFPDFBoxTabInTab(lineCount,ctx);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		long ellapsedTime = System.currentTimeMillis() - startTime;
		System.out.println(mode + " PDF generation with " + (lineCount * columnCount) + " cells took " + ellapsedTime + " milliseconds.");
		response.setContentType("application/pdf");
		response.setContentLength(baos.size());
		response.setHeader("Content-Disposition:", "attachment; filename=pdf-" + (lineCount*columnCount) + "-cells-" + ellapsedTime + "-ms.pdf");
		baos.writeTo(response.getOutputStream());
	}
	
	
	private ByteArrayOutputStream generatePDFIText(int lineCount, final int columnCount) throws DocumentException {
		ByteArrayOutputStream baos= new ByteArrayOutputStream(10000);
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
		PdfWriter.getInstance(document, baos);
		document.open();
		document.add(table);
		document.close();
		return baos;
	}
	
	private ByteArrayOutputStream generatePDFPDFBox(int lineCount, final int columnCount) throws DocumentException, IOException {
		ByteArrayOutputStream baos= new ByteArrayOutputStream(10000);
		String sqlAllEmployees = "SELECT e.emp_no,e.birth_date,e.first_name,e.last_name,e.gender,e.hire_date FROM employees.employees as e LIMIT " + lineCount;
		//final PdfPTable table = new PdfPTable(columnCount);
		final List<String[]> tableContent =	new ArrayList<String[]>();	
		this.db.query(sqlAllEmployees, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String[] row = new String[columnCount];
				tableContent.add(row);
				for (int i = 1; i <= columnCount; i++) {
					row[i-1]=rs.getString(i);
				}
			}
		});
		return new PDFBoxGenerator().generateSimple(lineCount,columnCount,tableContent);
		// Create a PDF document and put the table in it:
		//Document document = new Document();
		//PdfWriter.getInstance(document, baos);
		//document.open();
		//document.add(table);
		//document.close();
	}

	private ByteArrayOutputStream generatePDFITextTabInTab(final int lineCount) throws FileNotFoundException, DocumentException {
		ByteArrayOutputStream baos= new ByteArrayOutputStream(10000);
		String sqlAllEmployees ="SELECT t.title,e.emp_no, e.birth_date,e.first_name,e.last_name,e.gender,t.from_date, t.to_date FROM employees.employees As e INNER JOIN employees.titles as t ON e.emp_no=t.emp_no ORDER BY e.emp_no LIMIT " + lineCount;
		final PdfPTable table = new PdfPTable(5);
		final int[] prevEmpNo= {0};
		final PdfPCell headerCell = new PdfPCell();
		headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerCell.setBorderColor(BaseColor.GRAY);
		final PdfPCell dataCell = new PdfPCell();
		dataCell.setBackgroundColor(BaseColor.WHITE);
		dataCell.setBorderColor(BaseColor.GRAY);
		PdfPCell emptyCell=table.getDefaultCell();
		emptyCell.setBackgroundColor(BaseColor.WHITE);
		emptyCell.setBorderColor(BaseColor.WHITE);
		
		this.db.query(sqlAllEmployees, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				int crtEmpNo= rs.getInt("emp_no");
				if(prevEmpNo[0] != crtEmpNo){
					prevEmpNo[0]=crtEmpNo;
					//changed parent==> add heading rows for parent
					headerCell.setPhrase(new Phrase("First Name"));
					table.addCell(headerCell);
					headerCell.setPhrase(new Phrase("Last Name"));
					table.addCell(headerCell);
					headerCell.setPhrase(new Phrase("Birth Date"));
					table.addCell(headerCell);
					headerCell.setPhrase(new Phrase("Gender"));
					table.addCell(headerCell);
					table.completeRow();
					//==>Data rows for parent
					dataCell.setPhrase(new Phrase(rs.getString("first_name")));
					table.addCell(dataCell);
					dataCell.setPhrase(new Phrase(rs.getString("last_name")));
					table.addCell(dataCell);
					dataCell.setPhrase(new Phrase(rs.getString("birth_date")));
					table.addCell(dataCell);
					dataCell.setPhrase(new Phrase(rs.getString("gender")));
					table.addCell(dataCell);
					table.completeRow();
					//==>Header for child
					table.addCell("");
					table.addCell("");
					headerCell.setPhrase(new Phrase("Title From"));
					table.addCell(headerCell);
					headerCell.setPhrase(new Phrase("Title To"));
					table.addCell(headerCell);
					headerCell.setPhrase(new Phrase("Title"));
					table.addCell(headerCell);
					table.completeRow();
				}
				table.addCell("");
				table.addCell("");
				dataCell.setPhrase(new Phrase(rs.getString("from_date")));
				table.addCell(dataCell);
				dataCell.setPhrase(new Phrase(rs.getString("to_date")));
				table.addCell(dataCell);
				dataCell.setPhrase(new Phrase(rs.getString("title")));
				table.addCell(dataCell);
				table.completeRow();
			}
		});
		// Create a PDF document and put the table in it:
		Document document = new Document();
		PdfWriter.getInstance(document, baos);
		document.open();
		document.add(table);
		document.close();
		return baos;
	}

	private ByteArrayOutputStream generatePDFFlyingSaucer(int lineCount, int columnCount) throws IOException {
		String htmlContent = generateHTMLContent(lineCount, columnCount);
		return htmlToPDFBytes(htmlContent);
	}

	private ByteArrayOutputStream htmlToPDFBytes(String htmlContent) {
		ByteArrayOutputStream baos= new ByteArrayOutputStream(102400);
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocumentFromString(htmlContent);
		renderer.layout();
		try {
			renderer.createPDF(baos);
		} catch (com.lowagie.text.DocumentException e) {
			e.printStackTrace();
		}
		return baos;
	}

	private ByteArrayOutputStream generatePDFFlyingSaucerTabInTab(int lineCount, final WebContext ctx) throws IOException {
		List<Employee> employees = getEmployeesFromDB(lineCount);
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());

		// XHTML is the default mode, but we will set it anyway for better understanding of code
		templateResolver.setTemplateMode("XHTML");
		templateResolver.setPrefix("/html/");
		templateResolver.setSuffix(".html");
		templateResolver.setCacheTTLMs(3600000L);
		TemplateEngine templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		
		ctx.setVariable("css", getfileAsString("/css/report-style-tab-in-tab.css"));
		ctx.setVariable("employees", employees);
		String content=templateEngine.process("report-template-tt", ctx);
		return htmlToPDFBytes(content);
	}
	
	private List<Employee> getEmployeesFromDB(int lineCount) {
		String sqlAllEmployees ="SELECT t.title,e.emp_no, e.birth_date,e.first_name,e.last_name,e.gender,t.from_date, t.to_date FROM employees.employees As e INNER JOIN (   select emp_no   from employees.titles   group by emp_no   having count(1) > 1 ) as multititle on e.emp_no=multititle.emp_no INNER JOIN employees.titles as t ON e.emp_no=t.emp_no ORDER BY e.emp_no LIMIT " + lineCount;
		final int[] prevEmpNo= {0};
		final List<Employee> result= new ArrayList<Employee>();
		this.db.query(sqlAllEmployees, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				int crtEmpNo= rs.getInt("emp_no");
				if(prevEmpNo[0] != crtEmpNo){
					prevEmpNo[0]=crtEmpNo;
					result.add(new Employee(crtEmpNo, rs.getString("first_name"), rs.getString("last_name"), rs.getString("gender"), rs.getDate("birth_date")));
				}
				result.get(result.size()-1).getRoles().add(new Role(rs.getString("title"),rs.getDate("from_date"),rs.getDate("to_date")));
			}
		});
		
		return result;
	}

	private String generateHTMLContent(final int lineCount, final int columnCount) throws IOException {
		String css=getfileAsString("/css/report-style-simple.css"); 
		final StringBuilder html= new StringBuilder(10000);
		html.append("<html><head><style>"+css+"</style></head><body><table>");
		String sqlAllEmployees = "SELECT e.emp_no,e.birth_date,e.first_name,e.last_name,e.gender,e.hire_date FROM employees.employees as e LIMIT " + lineCount;
		this.db.query(sqlAllEmployees, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				html.append("<tr>");
				for (int i = 1; i <= columnCount; i++) {
					html.append("<td>").append(rs.getString(i)).append("</td>");
				}
				html.append("</tr>");
			}
		});
		html.append("</table></body></html>");
		return html.toString();
	}

	private String getfileAsString(String filePath) throws IOException {
		return IOUtils.toString(getServletContext().getResourceAsStream(filePath), StandardCharsets.UTF_8);
	}
}
