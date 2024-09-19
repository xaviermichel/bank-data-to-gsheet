package fr.simple.edm.config;

import lombok.Builder;
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

	private Sheet smartInsertGuesserFormulaSource;

	private Sheet smartInsertGuesserFormulaDestination;

	private Sheet smartInsertSheetOutputRange;

	private Sheet nextMonthSheetPaste;

	private String nextMontSheetName; // will create next month sheet with the given name

	@Data
	@Builder
	public static class Sheet {

		private String name;

		private Integer firstRow;

		private Integer lastRow;

		private Integer firstColIndex;

		private Integer lastColIndex;

		private char getCharFromAlphabetPosition(Integer position) {
			return (char) ('A' + position);
		}

		public String getRange() {
			return name + "!" + getCharFromAlphabetPosition(firstColIndex) + firstRow + ":"
					+ getCharFromAlphabetPosition(lastColIndex) + lastRow;
		}

	}

}
