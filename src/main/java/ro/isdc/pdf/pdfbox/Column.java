package ro.isdc.pdf.pdfbox;

public class Column {

    private String name;
    private float width;
    private ColumnAlignment alignment;

    public Column(String name, float width) {
        this.name = name;
        this.width = width;
        this.alignment= ColumnAlignment.LEFT;
        
    }
    public Column(String name, float width,ColumnAlignment alignment) {
    	this.name = name;
    	this.width = width;
    	this.alignment=alignment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }
	public ColumnAlignment getAlignment() {
		return alignment;
	}
	public void setAlignment(ColumnAlignment alignment) {
		this.alignment = alignment;
	}
    
}
