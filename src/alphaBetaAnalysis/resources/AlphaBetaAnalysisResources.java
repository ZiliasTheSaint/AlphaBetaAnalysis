package alphaBetaAnalysis.resources;

import java.util.ListResourceBundle;

/**
 * The resource bundle for AlphaBetaAnalysis class. <br>
 * 
 * @author Dan Fulea, 06 Jul. 2011
 * 
 */
public class AlphaBetaAnalysisResources extends ListResourceBundle{
	/**
	 * Returns the array of strings in the resource bundle.
	 * 
	 * @return the resources.
	 */
	public Object[][] getContents() {
		// TODO Auto-generated method stub
		return CONTENTS;
	}

	/** The resources to be localised. */
	private static final Object[][] CONTENTS = {

			// displayed images..
			{ "form.icon.url", "/danfulea/resources/personal.png" },///jdf/resources/duke.png" },
			{ "icon.url", "/danfulea/resources/personal.png" },///jdf/resources/globe.gif" },
			
			{ "icon.package.url", "images/personal.png" },
			
			{ "img.zoom.in", "/danfulea/resources/zoom_in.png" },
			{ "img.zoom.out", "/danfulea/resources/zoom_out.png" },
			{ "img.pan.left", "/danfulea/resources/arrow_left.png" },
			{ "img.pan.up", "/danfulea/resources/arrow_up.png" },
			{ "img.pan.down", "/danfulea/resources/arrow_down.png" },
			{ "img.pan.right", "/danfulea/resources/arrow_right.png" },
			{ "img.pan.refresh", "/danfulea/resources/arrow_refresh.png" },

			{ "img.accept", "/danfulea/resources/accept.png" },
			{ "img.insert", "/danfulea/resources/add.png" },
			{ "img.delete", "/danfulea/resources/delete.png" },
			{ "img.delete.all", "/danfulea/resources/bin_empty.png" },
			{ "img.view", "/danfulea/resources/eye.png" },
			{ "img.set", "/danfulea/resources/cog.png" },
			{ "img.report", "/danfulea/resources/document_prepare.png" },
			{ "img.today", "/danfulea/resources/time.png" },
			{ "img.open.file", "/danfulea/resources/open_folder.png" },
			{ "img.open.database", "/danfulea/resources/database_connect.png" },
			{ "img.save.database", "/danfulea/resources/database_save.png" },
			{ "img.substract.bkg", "/danfulea/resources/database_go.png" },
			{ "img.close", "/danfulea/resources/cross.png" },
			{ "img.about", "/danfulea/resources/information.png" },
			{ "img.printer", "/danfulea/resources/printer.png" },
			
			{ "mode.ALPHA", "Alpha" },
			{ "mode.BETA", "Beta" },
			{ "common.NAME", " analysis" },
			
			{ "Application.NAME", "Alpha, Beta analysis" },
			{ "About.NAME", "About" },
			{ "DeadTimeFrame.NAME", " dead time computation (2 sources method)" },
			{ "Background.NAME", " background analysis" },
			{ "Efficiency.NAME", " efficiency evaluation" },
			{ "Sample.NAME", " sample analysis" },
			
			{ "Author", "Author:" },
			{ "Author.name", "Dan Fulea , fulea.dan@gmail.com" },

			{ "Version", "Version:" },
			{ "Version.name", "AlphaBeta analysis 1.2" },

			{
					"License",
					//"This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License (version 2) as published by the Free Software Foundation. \n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. \n" },
			"Copyright (c) 2014, Dan Fulea \nAll rights reserved.\n\nRedistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n\n1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.\n\n2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.\n\n3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.\n\nTHIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 'AS IS' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n" },
			
			{ "tabs.aux", "Auxiliary data" },
			{ "tabs.input", "Data" },
			{ "tabs.output", "Results" },
			
			{ "rb.mda.border", "MDA calculation mode:" },
			{ "pasternack.rb", "based on Pasternack theory" },
			{ "curie.rb", "based on Curie theory" },
			{ "default.rb", "accurate (default)" },
			{ "mdaCb", new String[] { "Pasternack", "Curie", "Default" } },

			{ "menu.file", "File" },
			{ "menu.file.mnemonic", new Character('F') },
			
			{ "menu.file.exit", "Close" },
			{ "menu.file.exit.mnemonic", new Character('C') },
			{ "menu.file.exit.toolTip", "Close the application" },

			{ "menu.help", "Help" },
			{ "menu.help.mnemonic", new Character('H') },

			{ "menu.help.about", "About..." },
			{ "menu.help.about.mnemonic", new Character('A') },
			{ "menu.help.about.toolTip",
					"Several informations about this application" },

			{ "menu.help.howTo", "How to..." },
			{ "menu.help.howTo.mnemonic", new Character('H') },
			{ "menu.help.howTo.toolTip",
					"Several hints and tips about this application" },

			{ "menu.help.LF", "Look and feel..." },
			{ "menu.help.LF.mnemonic", new Character('L') },
			{ "menu.help.LF.toolTip", "Change application look and feel" },

			
			{ "status.wait", "Waiting your action!" },
			{ "status.computing", "Computing..." },
			{ "status.done", "Done! " },
			{ "status.save", "Saved: " },
			{ "status.error", "Unexpected error!" },
			
			{ "number.error", "Insert valid positive numbers! " },
			{ "number.error.title", "Unexpected error" },
			
			{ "deadTime.error", "Please set a valid dead time first! " },
			{ "deadTime.error.title", "Unexpected error" },
			
			{ "data.error", "Please add counts data first! " },
			{ "data.error.title", "Unexpected error" },
			
			{ "aux.error", "Please select appropiate auxiliary data first! " },
			{ "aux.error.title", "Unexpected error" },
			
			{ "calculation.error", "Please perform analysis first! " },
			{ "calculation.error.title", "Unexpected error" },
						
			{ "dialog.exit.title", "Confirm..." },
			{ "dialog.exit.message", "Are you sure?" },
			{ "dialog.exit.buttons", new Object[] { "Yes", "No" } },
			
			{ "library.master.db", "ICRP38" },
			{ "library.master.db.indexTable", "ICRP38Index" },
			{ "library.master.db.radTable", "icrp38Rad" },

			{ "library.master.jaeri.db", "JAERI03" },
			{ "library.master.jaeri.db.indexTable", "JAERI03index" },
			{ "library.master.jaeri.db.radTable", "jaeri03Rad" },
			
			{ "data.load", "Data" },
			{ "sort.by", "Sort by: " },
			{ "records.count", "Records count: " },
			
			{ "stability.db", "Devices" },
			{ "deadtime.table.alpha", "alphadeadtimetable" },
			{ "deadtime.table.beta", "betadeadtimetable" },
			
			{ "main.db", "AlphaBeta" },
			{ "main.db.bkg.alphaTable", "bkgAlpha" },
			{ "main.db.bkg.alphaDetailsTable", "bkgAlphaDetails" },
			{ "main.db.bkg.betaTable", "bkgBeta" },
			{ "main.db.bkg.betaDetailsTable", "bkgBetaDetails" },

			{ "main.db.eff.alphaTable", "effAlpha" },
			{ "main.db.eff.alphaDetailsTable", "effAlphaDetails" },
			{ "main.db.eff.betaTable", "effBeta" },
			{ "main.db.eff.betaDetailsTable", "effBetaDetails" },
			{ "main.db.nuc.alphaTable", "nuclideAlpha" },
			{ "main.db.nuc.betaTable", "nuclideBeta" },
			
			{ "main.db.sample.alphaTable", "sampleAlpha" },
			{ "main.db.sample.alphaDetailsTable", "sampleAlphaDetails" },
			{ "main.db.sample.betaTable", "sampleBeta" },
			{ "main.db.sample.betaDetailsTable", "sampleBetaDetails" },

			{ "library.master.dbCb", new String[] { "ICRP38", "JAERI03" } },
			{ "library.master.dbLabel", "Database: " },
			{ "library.master.border", "Master library" },
			
			{ "difference.yes", "Yes" },
			{ "difference.no", "No" },

			//=======================
			{ "main.deadTime.border", "Dead time" },
			{ "main.analysis.border", "Analysis" },
			{ "main.deadTime.label", "sec." },
			{ "main.set", "Set" },
			{ "main.set.toolTip", "Set dead time" },
			{ "main.set.mnemonic", new Character('S') },
			{ "main.compute", "Compute..." },
			{ "main.compute.toolTip", "Compute dead time" },
			{ "main.compute.mnemonic", new Character('C') },
			{ "main.background", "Background..." },
			{ "main.background.toolTip", "Background analysis" },
			{ "main.background.mnemonic", new Character('B') },
			{ "main.efficiency", "Efficiency..." },
			{ "main.efficiency.toolTip", "Efficiency analysis" },
			{ "main.efficiency.mnemonic", new Character('E') },
			{ "main.sample", "Sample..." },
			{ "main.sample.toolTip", "Sample analysis" },
			{ "main.sample.mnemonic", new Character('S') },
			
			{ "dtf.method1Rb", " Method 1: " },
			{ "dtf.method2Rb", " Method 2: " },
			{ "dtf.r1Tf.label", "The observed (measured) counts rate for source 1 [CPS]: " },
			{ "dtf.r2Tf.label", "The observed (measured) counts rate for source 2 [CPS]: " },
			{ "dtf.a1diva2Tf.label", "The activities ratio A1/A2 (or real counts rate ratio R1/R2): " },
			{ "dtf.or.label", "OR: " },
			{ "dtf.r12Tf.label", "The observed (measured) counts rate for source1+source2 [CPS]: " },
			{ "dtf.deadtime.label", " Dead time [sec]= " },
			{ "dtf.compute", "Compute" },
			{ "dtf.compute.toolTip", "Compute dead time" },
			{ "dtf.compute.mnemonic", new Character('C') },
			//--------------
			{ "bkg.save", "Save" },
			{ "bkg.save.toolTip", "Save results" },
			{ "bkg.save.mnemonic", new Character('S') },
			{ "bkg.descriptionTf", "Description: " },
			{ "today", "Today" },
			{ "today.toolTip", "Set the date at today" },
			{ "today.mnemonic", new Character('y') },
			{ "day", "Day: " },
			{ "month", "Month: " },
			{ "year", "Year: " },
			{ "date.border", "Measurement date" },
			{ "bkg.measurementTime", "Single measurement time [sec.]: " },
			{ "bkg.counts", "Counts: " },
			{ "bkg.add", "Add" },
			{ "bkg.add.toolTip", "Add data" },
			{ "bkg.add.mnemonic", new Character('A') },
			{ "bkg.add.mnemonic2", new Character('d') },
			
			{ "bkg.remove", "Remove" },
			{ "bkg.remove.toolTip", "Remove from list" },
			{ "bkg.remove.mnemonic", new Character('R') },

			{ "bkg.clear", "Clear" },
			{ "bkg.clear.toolTip", "Clear list" },
			{ "bkg.clear.mnemonic", new Character('C') },
			
			{ "bkg.compute", "Perform analysis" },
			{ "bkg.compute.toolTip", "Perform analysis" },
			{ "bkg.compute.mnemonic", new Character('P') },

			{ "bkg.delete", "Delete" },
			{ "bkg.delete.toolTip", "Delete record" },
			{ "bkg.delete.mnemonic", new Character('D') },
			{ "bkg.delete.mnemonic2", new Character('e') },

			{ "bkg.report", "Report" },
			{ "bkg.report.toolTip", "Show report" },
			{ "bkg.report.mnemonic", new Character('e') },

			{ "bkg.longTimeAnalysis", "Long time analysis" },
			{ "bkg.longTimeAnalysis.toolTip", "Background long time analysis" },
			{ "bkg.longTimeAnalysis.mnemonic", new Character('L') },
			{ "records.border", "Records" },
			
			{ "bkg.list.nrcrt", "#" },
			{ "bkg.list.id", "ID: " },
			{ "bkg.list.counts", "counts: " },
			{ "bkg.list.time", "time[s]: " },
			{ "bkg.list.cpm", "cpm: " },
			{ "bkg.list.cps", "cps: " },
			//===========
			{ "eff.nuclide", "Nuclide: " },
			{ "eff.nuc.notes", "Description: " },
			{ "eff.nuc.activity", "Activity [Bq]: " },
			{ "eff.nuc.activityUnc", "Uncertainty [%]: " },
			{ "eff.nuc.border", "Nuclide selection" },
			{ "eff.bkg.border", "Background selection" },
			{ "eff.nuc.date.border", "Activity date" },
			{ "eff.manual.eff", "Efficiency [number]: " },
			{ "eff.manual.effUnc", "Uncertainty [number]: " },
			{ "eff.manual.border", "Manual efficiency settings" },
			
			{ "sample.eff.border", "Efficiency selection" },
			{ "sample.valueTf", "Value: " },
			{ "sample.uncTf", "Uncertainty: " },
			{ "sample.umTf", "Final activity measurement unit: " },
			{ "sample.um.default", "Bq" },
			{ "sample.numRb", "Numerator" },
			{ "sample.denRb", "Denominator" },
			{ "sample.aux.border", "Sample activity is derived from measured activity [Bq] by multiplying with a fraction (e.g. concentration, Bq/kg)" },
			{ "sample.list.value", "value: " },
			{ "sample.list.unc", "uncertainty: " },
			{ "sample.list.position", "numerator?: " },
			//==========
			{ "report.cps", "Counts rate [cps]: " },
			{ "report.err.gauss", "Experimental (Gauss) standard deviation of mean [cps]: " },
			{ "report.err.poisson", "Theoretical (Poisson) standard deviation of mean [cps]: " },
			{ "report.err.diff1", "TTEST: Are those deviations statistically different?: " },
			{ "report.err.diff2", "FTEST: Are those deviations statistically different?: " },
			{ "report.err.warning", "WARNING: Poisson deviation is greater than Gauss deviation! Input data must be re-evaluated!" },
			{ "report.err.fail1", "Error analysis (ttest) cannot be performed!" },
			{ "report.err.fail2", "Error analysis (ftest) cannot be performed!" },
			{ "report.err.diff.longTime", "FTEST: Did background changed significantly over time?: " },
			{ "diff.yes", "Yes" },
			{ "diff.no", "No" },
			{ "report.plusminus", " +/- " },
			
			{ "report.cps.net", "Net counts rate and its 1 sigma Gauss uncertainty[cps]: " },
			{ "report.ld", "Detection limit and its 1 sigma uncertainty[cps]: " },
			{ "report.cps.ld", "TTEST: Is net counts rate statistically greater than detection limit?: " },
			
			{ "report.nuclide", "Nuclide: " },
			{ "report.nuclide.a0", "	Initial nuclide activity [Bq]: " },
			{ "report.nuclide.a",  "	Nuclide activity at measurement time [Bq]: " },
			{ "report.eff", "Efficiency: " },
			{ "report.eff.extUnc.Gauss", "Gauss extended uncertainty: " },
			{ "report.eff.extUnc.Poisson", "Poisson extended uncertainty: " },
			{ "report.eff.extUnc.Gauss1", "Gauss 1 sigma uncertainty: " },
			{ "report.eff.extUnc.Poisson1", "Poisson 1 sigma uncertainty: " },

			{ "report.a0", "Measured activity [Bq]: " },
			{ "report.a0.extUnc.Gauss", "	Gauss extended uncertainty: " },
			{ "report.a0.extUnc.Poisson", "	Poisson extended uncertainty: " },
			{ "report.mda0", "Measured MDA [Bq]: " },
			
			{ "report.a", "Sample activity [" },
			{ "report.a.extUnc.Gauss", "	Gauss extended uncertainty: " },
			{ "report.a.extUnc.Poisson", "	Poisson extended uncertainty: " },
			{ "report.mda", "Sample MDA [" },			
			{ "report.]", "]: " },
			{ "report.final", "Activity (or MDA) final result [" },
			//===============
			{ "results.title", "RESULTS:" },
			{ "results.sample.name", "Sample name:" },
			{ "results.sample.date", "Measurement date:" },
			{ "results.sample.time", "Measurement time:" },
			{ "results.bkg.name", "Background name:" },
			{ "results.eff.name", "Efficiency name:" },
			{ "results.bkg.date", "Background measurement date:" },
			{ "results.eff.date", "Efficiency measurement date:" },
			//===============
			{ "pdf.metadata.title", "AlphaBetaAnalysis PDF" },
			{ "pdf.metadata.subject", "Alpha, Beta analysis results" },
			{ "pdf.metadata.keywords", "AlphaBetaAnalysis, PDF" },
			{ "pdf.metadata.author", "AlphaBetaAnalysis" },
			{ "pdf.content.title", "AlphaBetaAnalysis Simulation Report" },
			{ "pdf.content.subtitle", "Report generated by: " },
			{ "pdf.page", "Page " },
			{ "pdf.header", "AlphaBetaAnalysis output" },
			{ "file.extension", "pdf" },
			{ "file.description", "PDF file" },
			{ "file.extension.html", "html" },
			{ "file.description.html", "HTML file" },
			{ "dialog.overwrite.title", "Overwriting..." },
			{ "dialog.overwrite.message", "Are you sure?" },
			{ "dialog.overwrite.buttons", new Object[] { "Yes", "No" } },

			

	};
}
