(function($){
	'use strict';
	/*** onLoad: bind event handlers***/
	$(function(){
		$('.pdf-gen').on('click',onGeneratePDFClicked);
		$('#txtLineCount').change(onLineCountChanged).focus().select();
		$('#txtColumnCount').change(onColumnCountChanged);
	});
	/**On Change handler of lineCount text field.*/
	function onLineCountChanged(e){
		var MIN_LINES=1,
			MAX_LINES=300000,
			v=Number($(this).val());
		
		if(v < MIN_LINES){
			$(this).val(MIN_LINES);
		}
		if(v > MAX_LINES){
			$(this).val(MAX_LINES);
		}
	};
	
	/**On change handler for column count text field.*/
	function onColumnCountChanged(e){
		var MIN_COLUMNS=1,
			MAX_COLUMNS=6,
			v=Number($(this).val());
		
		if(v < MIN_COLUMNS){
			$(this).val(MIN_COLUMNS);
		}
		if(v > MAX_COLUMNS){
			$(this).val(MAX_COLUMNS);
		}
	};
	
	/*** On Click handler of PDF button. ***/
	function onGeneratePDFClicked(e){
		var lineCount=Math.min(Number($('#txtLineCount').val()),300000),
			columnCount=Math.max(1,Math.min(Number($('#txtColumnCount').val()),6)),
			mode=$(this).data('mode');
		
		window.open("pdf?lineCount=" + lineCount + "&columnCount=" + columnCount + '&mode=' + mode);
		return false;
	}
}(jQuery));