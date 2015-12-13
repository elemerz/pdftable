<html>
	<head>
		<title>PDF Table Generetion POC with iText</title>
	</head>
	<body>
	<h1>Generate Big PDF table from MySql employees table using iText (Max. 300000 lines)</h1>
	<button id="btnPDF">Generate a PDF having</button>
	<input id="txtLineCount" class="mask-int" value="1000" maxlength="6" size="6"/>lines
	<input id="txtColumnCount" class="mask-pint" value="3" maxlength="1" size="1"/>columns
	
	
	<script type="text/javascript" src="js/jquery-2.1.4-min.js"></script>
	<script type="text/javascript" src="js/jquery.keyfilter-1.8.min.js"></script>
	<script type="text/javascript" src="js/pdf-gen.js"></script>
	</body>
</html>
