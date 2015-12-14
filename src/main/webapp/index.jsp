<html>
	<head>
		<title>PDF Table Generetion POC with iText</title>
	</head>
	<body>
	<h1>Generate Big PDF table from MySql employees table using iText (Max. 300000 lines)</h1>
	<fieldset>
		<legend>Table parameters</legend>
		<input id="txtLineCount" class="mask-int" value="1000" maxlength="6" size="6"/>lines
		<input id="txtColumnCount" class="mask-pint" value="3" maxlength="1" size="1"/>columns
	</fieldset>	
	<button class="pdf-gen" data-mode="i-text">Generate a PDF with iText</button>
	<button class="pdf-gen" data-mode="flying-saucer">Generate a PDF with Flying Saucer</button>
	
	
	<script type="text/javascript" src="js/jquery-2.1.4-min.js"></script>
	<script type="text/javascript" src="js/jquery.keyfilter-1.8.min.js"></script>
	<script type="text/javascript" src="js/pdf-gen.js"></script>
	</body>
</html>
