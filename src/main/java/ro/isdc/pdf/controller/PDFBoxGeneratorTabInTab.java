package ro.isdc.pdf.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import ro.isdc.pdf.dto.Employee;
import ro.isdc.pdf.dto.Role;
import ro.isdc.pdf.pdfbox.Column;
import ro.isdc.pdf.pdfbox.ColumnAlignment;

public class PDFBoxGeneratorTabInTab {
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
	private static final float GAP_PARENT_CHILD = 5;
	private static final float GAP_NEXT_PARENT = 10;
	private static final float CELL_MARGIN = 2;

	//define column widths:
	private static final int COL_COUNT_PARENT = 4;
	private static final int COL_COUNT_CHILD = 3;
	private static final Column[] colParent= new Column[COL_COUNT_PARENT];
	private static final Column[] colChild= new Column[COL_COUNT_CHILD];
	static {
		colParent[0] = new Column("FirstName", 75, ColumnAlignment.RIGHT);
		colParent[1] = new Column("LastName", 150);
		colParent[2] = new Column("BirthDate",70);
		colParent[3] = new Column("Gender", 50);
		colChild[0] = new Column("Title From",70);
		colChild[1] = new Column("Title To", 70);
		colChild[2] = new Column("Title name", 120);
	}
	
	private static float[] vLineVerticesParent= new float[]{
		MARGIN,
		MARGIN + colParent[0].getWidth(),
		MARGIN + colParent[0].getWidth() + colParent[1].getWidth(),
		MARGIN + colParent[0].getWidth() + colParent[1].getWidth()+ colParent[2].getWidth(),
		MARGIN + colParent[0].getWidth() + colParent[1].getWidth()+ colParent[2].getWidth() + colParent[3].getWidth()
	};
	private static final float CHILD_TABLE_INDENT=130f;
	private static float[] vLineVerticesChild= new float[]{
		MARGIN + CHILD_TABLE_INDENT,
		MARGIN + CHILD_TABLE_INDENT + colChild[0].getWidth(),
		MARGIN + CHILD_TABLE_INDENT + colChild[0].getWidth() + colChild[1].getWidth(),
		MARGIN + CHILD_TABLE_INDENT + colChild[0].getWidth() + colChild[1].getWidth()+ colChild[2].getWidth()
	};

	/* instance fields*/
	private final PDResources PDF_PG_RES = new PDResources();
	private final PDExtendedGraphicsState GS_SEMITRAN = new PDExtendedGraphicsState();
	private final PDExtendedGraphicsState GS_OPAQUE = new PDExtendedGraphicsState();
	
	public ByteArrayOutputStream generateReport(List<Employee> employees, ServletContext servletContext) throws IOException {
		GS_SEMITRAN.setNonStrokingAlphaConstant(0.3f); // set the opacity of the graphics state
		GS_SEMITRAN.setLineWidth(0.25f);
		GS_OPAQUE.setNonStrokingAlphaConstant(1f); // set the opacity of the graphics state
		PDF_PG_RES.put(COSName.getPDFName("MySemiTransGState"), GS_SEMITRAN);
		PDF_PG_RES.put(COSName.getPDFName("MyOpaqueGState"), GS_OPAQUE);

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
			if (crtTextY- ROW_HEIGHT <= MARGIN) {
				/* header + data row togethet (without textwrap) do NOT fit ==> begin new Page */
				pageStream.close();
				pageStream = addPageToDoc(doc);
				crtLineY = tableTopY;
				crtTextY = startTextY;
			}
			
			rowHeight = drawParentTable(pageStream, employee, crtTextY,crtLineY);
			crtLineY = crtLineY - rowHeight - GAP_PARENT_CHILD;
			crtTextY = crtTextY - rowHeight - GAP_PARENT_CHILD;
			
			if (employee.getRoles() != null && employee.getRoles().size() > 0) {
				if (crtTextY - ROW_HEIGHT * (employee.getRoles().size() + 1) <= MARGIN) { 
					/* header + all child data rows (without textwrap) do NOT fit ==> begin new Page */
					pageStream.close();
					pageStream = addPageToDoc(doc);
					crtLineY = tableTopY;
					crtTextY = startTextY;
				}
				rowHeight = drawChildTable(pageStream, employee.getRoles(), crtTextY,crtLineY);
				crtLineY = crtLineY - rowHeight - GAP_NEXT_PARENT;
				crtTextY = crtTextY - rowHeight - GAP_NEXT_PARENT;
			}
			
		}
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

	/**
	 * Draw the Line content.
	 * @param crtLineY 
	 * 
	 * @return
	 * @throws IOException 
	 */
	private float drawParentTable(PDPageContentStream pageStream, Employee employeeDto, float crtTextY, float crtLineY) throws IOException {
		pageStream.setNonStrokingColor(0f);
		float crtX = MARGIN + CELL_MARGIN;
		float[] hLineVertices=new float[3];
		
		float maxHeightHeader = 0;
		float maxHeightData = 0;
		float cellHeight=0f;
		hLineVertices[0]=crtLineY;

		
//		pageStream.setNonStrokingColor(0.75f);
//		pageStream.addRect(vLineVerticesParent[0], crtLineY - ROW_HEIGHT, vLineVerticesParent[vLineVerticesParent.length-1] - vLineVerticesParent[0], ROW_HEIGHT);
//		pageStream.fill();
//		pageStream.closeAndStroke();
//		pageStream.setNonStrokingColor(0f);
		
		
		/* header row */
		for (int i=0; i<COL_COUNT_PARENT; i++) {
			//FistName,LastName,BirthDate,Gender
			cellHeight = writeCellContent(colParent[i],colParent[i].getName(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightHeader) {
				maxHeightHeader = cellHeight;
			}
			crtX += colParent[i].getWidth();
		}
		crtLineY-=maxHeightHeader;
		crtTextY-= maxHeightHeader;
						
		/* data row */
		hLineVertices[1]=crtLineY;
		crtX = MARGIN + CELL_MARGIN;
		for (int i=0; i<COL_COUNT_PARENT; i++) {
			//FistName,LastName,BirthDate,Gender
			cellHeight = writeCellContent(colParent[i], employeeDto.getStrValueForTable(i), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightData) {
				maxHeightData = cellHeight;
			}
			crtX += colParent[i].getWidth();
		}
		hLineVertices[2] = crtLineY - maxHeightData;

		/* draw table Grid aroung header and data row */
		drawGrid(pageStream,hLineVertices,vLineVerticesParent);
		return maxHeightHeader + maxHeightData;
	}

	/**
	 * Draw the Line content.
	 * @param crtLineY 
	 * 
	 * @return
	 * @throws IOException 
	 */
	private float drawChildTable(PDPageContentStream pageStream, List<Role> roleDtoList, float crtTextY, float crtLineY) throws IOException {
		pageStream.setNonStrokingColor(0f);
		float crtX = MARGIN + CHILD_TABLE_INDENT + CELL_MARGIN;
		float[] hLineVertices=new float[roleDtoList.size() + 2];
		
		float maxHeightHeader = 0;
		float cellHeight=0f;
		hLineVertices[0]=crtLineY;
		float result = 0; // the height of the child table with its header and all data rows 
		/* header row */
		for (int i=0; i<COL_COUNT_CHILD; i++) {
			//FistName,LastName,BirthDate,Gender
			cellHeight = writeCellContent(colChild[i],colChild[i].getName(), pageStream, crtX, crtTextY);
			if (cellHeight > maxHeightHeader) {
				maxHeightHeader = cellHeight;
			}
			crtX += colChild[i].getWidth();
		}
		crtLineY-=maxHeightHeader;
		crtTextY-= maxHeightHeader;
		result += maxHeightHeader;
		
		/* data rows in cycle */
		float maxHeightData;
		for (int roleIndex = 0, roleIndexLimit = roleDtoList.size(); roleIndex < roleIndexLimit; roleIndex++) {
			hLineVertices[roleIndex+1]=crtLineY;
			maxHeightData = 0;
			Role roleDto = roleDtoList.get(roleIndex);
			crtX = MARGIN + CHILD_TABLE_INDENT + CELL_MARGIN;
			for (int i=0; i<COL_COUNT_CHILD; i++) {
				//FistName,LastName,BirthDate,Gender
				cellHeight = writeCellContent(colChild[i], roleDto.getStrValueForTable(i), pageStream, crtX, crtTextY);
				if (cellHeight > maxHeightData) {
					maxHeightData = cellHeight;
				}
				crtX += colChild[i].getWidth();
			}
			crtLineY -= maxHeightData;
			crtTextY -= maxHeightData;
			result += maxHeightData;
		}
		hLineVertices[hLineVertices.length - 1] = crtLineY;

		/* draw table Grid around header and data row */
		drawGrid(pageStream,hLineVertices,vLineVerticesChild);
		return result;
	}

	
	
	private void drawGrid(PDPageContentStream pageStream, float[] hLines, float[] vLines) throws IOException {
		//horizontal lines

		pageStream.appendRawCommands("/MySemiTransGState gs\n");
		pageStream.setNonStrokingColor(0.75f);
		pageStream.addRect(vLines[0], hLines[1], vLines[vLines.length-1] - vLines[0], hLines[0] - hLines[1]);
		pageStream.fill();
		pageStream.setNonStrokingColor(0.25f);

		for (int i = 0; i < hLines.length; i++) {
			pageStream.addLine(vLines[0], hLines[i], vLines[vLines.length-1], hLines[i]);			
		}
		//vertical lines
		for (int i = 0; i < vLines.length; i++) {
			pageStream.addLine(vLines[i], hLines[0], vLines[i], hLines[hLines.length-1]);			
		}
		pageStream.closeAndStroke();	
		pageStream.appendRawCommands("/MyOpaqueGState gs\n");
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

	private int[] possibleWrapPoints(String text) {
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

		PDResources resources = page.getResources();
		if (resources == null) {
			page.setResources(PDF_PG_RES);
		} else {
			if (resources.getExtGState(COSName.getPDFName("MySemiTransGState")) == null) {
				resources.put(COSName.getPDFName("MySemiTransGState"), GS_SEMITRAN);
			}
			if (resources.getExtGState(COSName.getPDFName("MyOpaqueGState")) == null) {
				resources.put(COSName.getPDFName("MyOpaqueGState"), GS_OPAQUE);
			}
		}
		
		PDPageContentStream contentStream = new PDPageContentStream(doc, page, false, false);
		// User transformation matrix to change the reference when drawing.
		// This is necessary for the landscape position to draw correctly
		if (IS_LANDSCAPE) {
			contentStream.transform(new Matrix(0f, 1f, -1f, 0f, PAGE_SIZE.getWidth(), 0f));
		}
		contentStream.setFont(TEXT_FONT, FONT_SIZE);
		return contentStream;
	}
}
