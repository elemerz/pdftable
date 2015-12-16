<html>
	<head>
		<title>PDF Table Generetion POC with iText</title>
	</head>
	<style>
		ul {
			padding: 0;
		}
		ul,li {
			list-style:none;
			text-align: left;
			
		}
		li.ico {
			padding-left: 30px;
			line-height:24px;
			background: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAABiCAMAAAB9EONtAAABO1BMVEUAAAAAAACWuz2VvD2VuzyVvD2WvDyVuzyUuzsAAAAAAACszWCZvkKVuzyVvD0AAAAAAACoyVqZvUKUtj97mzKWujqTuzuUvTtigieqvz//WFu221ex2FH/IhKbwEX/PCDB4WL/Sij/lHWs1kz/uaC+32G63Vv/NhvKAAHEAADF34r/QyS93l7/MRimzUn/KhT/GQ/X66O422v/fmGgy0X/Tyu+AALa7KfU6aD/tJr/q5PE34PH4nzG4Xmy1mj/Vlqn0kqYwj//LRf/Ew6213XG4nK63XC22Wz/i2z/hWP/cFer0E//akb/PkP/ORzL4pn/po//n4v/noj/ooa923q42Hf/j27/h2u322H/dl7/TVDzTFD/VDP/VzH/CwvaBgeuAAD/zLSuz2T/iGL6S07jHB3lAAPuAAL3AADEslXNAAAAGnRSTlMABurOqb7bxpgiDf3tsjcbFPj3ul9OQCsnDLYpYaQAAAFtSURBVEjH7c/HUsJQGIbhE1S6gF0JEDoIoYSAoiBR6U16r3bv/wr8T4AZY+LGHTN5sjnzvZOG/s+gVAD1hSioktcgoBaFnTva66VTuxLBA9JSwW63/xGAIBhUCqDL4zvyB/i8d8YHZSPJMEze47F7aDrNMMlkY58PioLLYiHhk2j4LpK0uFyFvVWI4UBC8pKwQ4itQyYcDlerJK8K5/g6KLsBkCItWAofu6t3nOht4GWI96EOn/WnP/8j5gIxiR8s3oOCRMjEQVEqPABxIFS9XC7Xy6gJ4Uxojw9t4OicIH4Fk1Gj0RhNWmGAtIHECLhkMplMtkXqZhGO48x1xD27eTfgFjSbrVa73RmjsduJ9ful0mBQLrMsOxpVKpU6euNDia3VplOKony+UCgSsVq/0MTtZGuzhMMRDAYp6gnCI+zWTzTpzBIJ2AVzNPqB3ssO8ZzNLtDi1QGC+PEh2FfzfO5HS//a5cYVtkTb5Rv1kk1TZw6MBgAAAABJRU5ErkJggg==") no-repeat 0 0;
		}
		li.pros {
			background-position: 0 0;
		}
		li.cons {
			background-position: 0 -75px;
		}
	</style>
	<body>
	<h3>Generate Big PDF table from MySql employees table using iText (Max. 250000 lines)</h3>
	<fieldset>
		<legend>Liear Table with n rows m columns</legend>
		<input class="line-count mask-int" value="1000" maxlength="6" size="6"/>lines
		<input class="column-count mask-pint" value="3" maxlength="1" size="1"/>columns
		<div>
			<button class="pdf-gen" data-mode="i-text">Generate PDF Table with iText</button>
			<button class="pdf-gen" data-mode="flying-saucer">Generate a PDF Table with Flying Saucer</button>
		</div>
	</fieldset>	
	
	<fieldset>
		<legend>Table in Table (master-detail) with n rows</legend>
		<input class="line-count mask-int" value="1000" maxlength="6" size="6"/>lines
		<div>
			<button class="pdf-gen" data-mode="i-text-tt">
				<b>Generate Table-In-Table with iText</b>
				<ul>
					<li class="ico pros">Fast (10000 lines in ~2.5 seconds)</li>
					<li class="ico cons">Simple Styling from Java (Colors,borders)</li>
				</ul>
			</button>
			<button class="pdf-gen" data-mode="flying-saucer-tt">
				<b>Generate Table-In-Table with Flying Saucer</b>
				<ul>
					<li class="ico pros">Complex styling and layout (CSS 2.1)</li>
					<li class="ico cons">A bit slow (10000 lines ~14 seconds)</li>
				</ul>
			</button>
		</div>
	</fieldset>	
	
	<script type="text/javascript" src="js/jquery-2.1.4-min.js"></script>
	<script type="text/javascript" src="js/jquery.keyfilter-1.8.min.js"></script>
	<script type="text/javascript" src="js/pdf-gen.js"></script>
	</body>
</html>
