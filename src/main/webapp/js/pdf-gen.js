(function($){
	'use strict';
	/*** onLoad: bind event handlers***/
	$(function(){
		$('.pdf-gen').on('click',onGeneratePDFClicked);
		$('.line-count').change(onLineCountChanged).focus().select();
		$('.column-count').change(onColumnCountChanged);
	});
	/**On Change handler of lineCount text field.*/
	function onLineCountChanged(e){
		var MIN_LINES=1,
			MAX_LINES=250000,
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
		var $btn=$(e.currentTarget),
			lineCount=Math.min(Number($btn.closest('fieldset').find('.line-count').val() || 0),250000),
			columnCount=Math.max(1,Math.min(Number($btn.closest('fieldset').find('.column-count').val() || 0),6)),
			mode=$(this).data('mode');
		
		window.open("pdf?lineCount=" + lineCount + "&columnCount=" + columnCount + '&mode=' + mode);
		return false;
	}
}(jQuery));