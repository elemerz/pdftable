package ro.isdc.pdf.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import ro.isdc.pdf.dto.Employee;
import ro.isdc.pdf.pdfbox.Column;
import ro.isdc.pdf.pdfbox.ColumnAlignment;

public class PDFBoxGeneratorTabInTab {
	private static Column[] columns = new Column[6];
	private static float[] rightEdgePos = new float[6];
	static {
		columns[0] = new Column("EmpNo", 50, ColumnAlignment.RIGHT);
		columns[1] = new Column("BirthDate", 70);
		columns[2] = new Column("FirstName", 75, ColumnAlignment.RIGHT);
		columns[3] = new Column("LastName", 150);
		columns[4] = new Column("Gender", 50);
		columns[5] = new Column("HireDate", 70);
		float crtEdge=0f;
		for (int i=0; i<6; i++) {
			crtEdge += columns[i].getWidth();
			rightEdgePos[i] = crtEdge;
		}
	}
	// Page configuration
	private static final PDRectangle PAGE_SIZE = PDRectangle.A4;
	private static final boolean IS_LANDSCAPE = false;
	private static final float MARGIN = 20;
	private static final float tableTopY = IS_LANDSCAPE ? PAGE_SIZE.getWidth() - MARGIN : PAGE_SIZE.getHeight() - MARGIN;
	// Font configuration
	private static final PDFont TEXT_FONT = PDType1Font.HELVETICA;
	private static final float FONT_SIZE = 10;
	private static final float TEXT_LINE_HEIGHT = TEXT_FONT.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * FONT_SIZE;
	// Table configuration
	private static final float ROW_HEIGHT = 15;
	private static final float CELL_MARGIN = 2;

	//define column widths:
	private static final Column COL_FIRST_NAME=new Column("FirstName", 75, ColumnAlignment.RIGHT);
	private static final Column COL_LAST_NAME=new Column("LastName", 150);
	private static final Column COL_BIRTH_DATE=new Column("BirthDate",70);
	private static final Column COL_GENDER = new Column("Gender", 50);
	private static final float CHILD_TABLE_INDENT=250f;
	private static final Column COL_FROM=new Column("Title From",70);
	private static final Column COL_TO = new Column("Title To", 70);
	private static final Column COL_TITLE=new Column("Title", 120);
	private static float[] vLineVertices= new float[]{
		MARGIN,
		MARGIN + COL_FIRST_NAME.getWidth(),
		MARGIN + COL_FIRST_NAME.getWidth() + COL_LAST_NAME.getWidth(),
		MARGIN + COL_FIRST_NAME.getWidth() + COL_LAST_NAME.getWidth()+ COL_BIRTH_DATE.getWidth(),
		MARGIN + COL_FIRST_NAME.getWidth() + COL_LAST_NAME.getWidth()+ COL_BIRTH_DATE.getWidth() + COL_GENDER.getWidth()
	};

	public ByteArrayOutputStream generateReport(List<Employee> employees, ServletContext servletContext) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Calculate center alignment for text in cell considering font height
		final float startTextY = tableTopY - (ROW_HEIGHT / 2) - (TEXT_LINE_HEIGHT / 4);
		PDDocument doc = new PDDocument();
		PDPageContentStream pageStream = addPageToDoc(doc);
		//add image
		float rowHeight=addLogo(servletContext,doc,pageStream);
		float crtLineY = tableTopY - rowHeight;
		float crtTextY = startTextY - rowHeight;

		// draw the lines
		for (Employee employee : employees) {
			if (crtTextY- ROW_HEIGHT <= MARGIN) {//header + data row togethet do NOT fit==> begin new Page
				//drawVerticalLines(pageStream, columnCount, crtLineY);
				pageStream.close();
				// start new Page
				pageStream = addPageToDoc(doc);
				crtLineY = tableTopY;
				crtTextY = startTextY;
			}
			
			rowHeight = drawParentTable(pageStream, employee, crtTextY,crtLineY);
			crtLineY -= rowHeight;
			crtTextY -= rowHeight;
		}
		
		//drawVerticalLines(pageStream, columnCount, crtLineY);
		
		pageStream.close();
		doc.save(baos);
		doc.close();
		return baos;
	}
	
	private float addLogo(ServletContext servletContext, PDDocument doc, PDPageContentStream pageStream) throws IOException {
		// add an image
		float scale=1.0f;
		String logoPath=servletContext.getRealPath("/img/famed-logo.png");
		PDImageXObject pdImage = PDImageXObject. createFromFile(logoPath, doc);
        try {
            float imageLeft= (IS_LANDSCAPE ? PAGE_SIZE.getHeight() : PAGE_SIZE.getWidth()) - pdImage.getWidth()*scale - MARGIN;

            pageStream.drawImage(pdImage, imageLeft, tableTopY-pdImage.getHeight()*scale, pdImage.getWidth()*scale, pdImage.getHeight()*scale);            
        } catch (FileNotFoundException fnfex) {
            System.out.println("No image for you");
        }		
        return pdImage.getHeight()*scale;
	}

	/**Draw vertical separator lines between cells.
	 * @throws IOException */
	private void drawVerticalLines(PDPageContentStream pageStream, int columnCount,float crtLineY) throws IOException{
		pageStream.addLine(MARGIN, tableTopY, MARGIN, crtLineY);
		for (int i = 0; i < columnCount; i++) {
			float crtY = MARGIN + rightEdgePos[i];
			pageStream.addLine(crtY, tableTopY, crtY, crtLineY);
		}
		pageStream.closeAndStroke();
	}
	
	/**
	 * Draw the Line content.
	 * @param crtLineY 
	 * 
	 * @return
	 * @throws IOException 
	 */
	private float drawParentTable(PDPageContentStream pageStream, Employee employee, float crtTextY, float crtLineY) throws IOException {
		float crtX = MARGIN + CELL_MARGIN;
		float[] hLineVertices=new float[3];
		float maxHeightHeader = 0;
		float maxHeightData = 0;
		float cellHeight=0f;
		
			hLineVertices[0]=crtLineY;
			//FistName
			cellHeight = writeCellContent(COL_FIRST_NAME,COL_FIRST_NAME.getName(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightHeader) {
				maxHeightHeader = cellHeight;
			}
			crtX += COL_FIRST_NAME.getWidth();
			//LastName
			cellHeight = writeCellContent(COL_LAST_NAME,COL_LAST_NAME.getName(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightHeader) {
				maxHeightHeader = cellHeight;
			}
			crtX += COL_LAST_NAME.getWidth();
			//BirthDate
			cellHeight = writeCellContent(COL_BIRTH_DATE,COL_BIRTH_DATE.getName(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightHeader) {
				maxHeightHeader = cellHeight;
			}
			crtX += COL_BIRTH_DATE.getWidth();
			//Gender
			cellHeight = writeCellContent(COL_GENDER,COL_GENDER.getName(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightHeader) {
				maxHeightHeader = cellHeight;
			}
			crtX += COL_GENDER.getWidth();
			
			crtLineY-=maxHeightHeader;
			crtTextY-= maxHeightHeader;
			hLineVertices[1]=crtLineY;
			crtX = MARGIN + CELL_MARGIN;
			
			//data row
			//FistName
			cellHeight = writeCellContent(COL_FIRST_NAME,employee.getFirstName(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightData) {
				maxHeightData = cellHeight;
			}
			crtX += COL_FIRST_NAME.getWidth();
			//LastName
			cellHeight = writeCellContent(COL_LAST_NAME,employee.getLastName(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightData) {
				maxHeightData = cellHeight;
			}
			crtX += COL_LAST_NAME.getWidth();
			//BirthDate
			cellHeight = writeCellContent(COL_BIRTH_DATE,employee.getBirthDate().toLocaleString(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightData) {
				maxHeightData = cellHeight;
			}
			crtX += COL_BIRTH_DATE.getWidth();
			//Gender
			cellHeight = writeCellContent(COL_GENDER,employee.getGender(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightData) {
				maxHeightData = cellHeight;
			}
			crtX += COL_GENDER.getWidth();
			hLineVertices[2]=crtLineY-maxHeightData;
			//draw Grid
			drawGrid(pageStream,hLineVertices,vLineVertices);
						
		return maxHeightHeader + maxHeightData;
	}

	private void drawGrid(PDPageContentStream pageStream, float[] hLines, float[] vLines) throws IOException {
		//horizontal lines
		for (int i = 0; i < hLines.length; i++) {
			pageStream.addLine(vLines[0], hLines[i], vLines[vLines.length-1], hLines[i]);			
		}
		//vertical lines
		for (int i = 0; i < vLines.length; i++) {
			pageStream.addLine(vLines[i], hLines[0], vLines[i], hLines[hLines.length-1]);			
		}
		pageStream.closeAndStroke();		
	}

	private float writeCellContent(Column col, String text, PDPageContentStream pageStream, float crtX, float startTextY) throws IOException {
		float cellHeight = ROW_HEIGHT;
		float cellInnerWidth = col.getWidth() - 2 * CELL_MARGIN;
		float textWidth = TEXT_FONT.getStringWidth(text) / 1000 * FONT_SIZE;
		if (textWidth <= cellInnerWidth) {// line fits in cell
			pageStream.beginText();
			if (col.getAlignment() == ColumnAlignment.LEFT) {
				pageStream.newLineAtOffset(crtX, startTextY);
			} else {
				float textLeftPos = crtX + cellInnerWidth - textWidth;
				pageStream.newLineAtOffset(textLeftPos, startTextY);
			}
			pageStream.showText(text != null ? text : "");
			pageStream.endText();
		} else {//line must be wrapped
			int start = 0;
			int end = 0;
			float crtCellY = startTextY;
			String currentToken="";
			for (int i : possibleWrapPoints(text)) {
				float width = TEXT_FONT.getStringWidth(text.substring(start, i)) / 1000 * FONT_SIZE;
				if (start < end && width > cellInnerWidth) {
					currentToken=text.substring(start, end);
					// Draw partial text and increase height
					pageStream.beginText();
					if (col.getAlignment() == ColumnAlignment.LEFT) {
						pageStream.newLineAtOffset(crtX, crtCellY);
					}else{
						float currentTokenWidth = TEXT_FONT.getStringWidth(currentToken) / 1000 * FONT_SIZE;
						float textLeftPos = crtX + cellInnerWidth - currentTokenWidth;
						pageStream.newLineAtOffset(textLeftPos, crtCellY);
					}
					pageStream.showText(currentToken);
					pageStream.endText();
					crtCellY -= TEXT_LINE_HEIGHT;
					start = end;
					cellHeight+=TEXT_LINE_HEIGHT;
				}
				end = i;
			}
			// Last piece of text
			pageStream.beginText();
			currentToken=text.substring(start);
			if (col.getAlignment() == ColumnAlignment.LEFT) {
				pageStream.newLineAtOffset(crtX, crtCellY);
			}else{
				float currentTokenWidth = TEXT_FONT.getStringWidth(currentToken) / 1000 * FONT_SIZE;
				float textLeftPos = crtX + cellInnerWidth - currentTokenWidth;
				pageStream.newLineAtOffset(textLeftPos, crtCellY);
			}
			pageStream.showText(currentToken);
			pageStream.endText();
		}

		return cellHeight;
	}

	int[] possibleWrapPoints(String text) {
		String[] split = text.split("(?<=\\W)");
		int[] ret = new int[split.length];
		ret[0] = split[0].length();
		for (int i = 1; i < split.length; i++)
			ret[i] = ret[i - 1] + split[i].length();
		return ret;
	}

	private PDPageContentStream addPageToDoc(PDDocument doc) throws IOException {
		PDPage page = new PDPage();
		page.setMediaBox(PAGE_SIZE);
		page.setRotation(IS_LANDSCAPE ? 90 : 0);
		doc.addPage(page);
		PDPageContentStream contentStream = new PDPageContentStream(doc, page, false, false);
		// User transformation matrix to change the reference when drawing.
		// This is necessary for the landscape position to draw correctly
		if (IS_LANDSCAPE) {
			contentStream.transform(new Matrix(0f, 1f, -1f, 0f, PAGE_SIZE.getWidth(), 0f));
		}
		contentStream.setFont(TEXT_FONT, FONT_SIZE);
		contentStream.setLineWidth(0.25f);
		return contentStream;
	}
}
