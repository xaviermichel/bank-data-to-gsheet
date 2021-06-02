package fr.simple.edm.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleApiClientConfig {

	private Set<String> googleOAuth2Scopes() {
		return Set.of(SheetsScopes.SPREADSHEETS);
	}

	@Bean
	public GoogleCredentials googleCredential(GoogleSheetImporterConfiguration googleSheetImporterConfiguration) throws IOException {
		return GoogleCredentials
				.fromStream(new FileInputStream(googleSheetImporterConfiguration.getCredentialsFilePath()))
				.createScoped(googleOAuth2Scopes());
	}

}
