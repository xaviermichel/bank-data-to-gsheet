package fr.simple.edm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google.sheet")
@Data
public class GoogleSheetImporterConfiguration {

	private String credentialsFilePath;

	private String spreadsheetId;

	private Sheet smartInsertSheet;

	private Sheet nextMonthSheet;

	private Sheet smartInsertSheetFormulaSource;

	private Sheet smartInsertSheetFormulaDestination;

	private Sheet smartInsertSheetFullRangeCopy;

	private Sheet nextMonthSheetPaste;

	private String nextMontSheetName; // will create next month sheet with the given name

	@Data
	public static class Sheet {

		private String name;

		private String firstRow;

		private String lastRow;

		private String firstCol;

		private String lastCol;

		public String getRange() {
			return name + "!" + firstCol + firstRow + ":" + lastCol + lastRow;
		}

	}

}
