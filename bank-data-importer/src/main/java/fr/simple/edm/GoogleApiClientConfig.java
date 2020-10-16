package fr.simple.edm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleApiClientConfig {

	private Set<String> googleOAuth2Scopes() {
		Set<String> googleOAuth2Scopes = new HashSet<>();
		googleOAuth2Scopes.add(SheetsScopes.SPREADSHEETS);
		return Collections.unmodifiableSet(googleOAuth2Scopes);
	}

	@Bean
	public GoogleCredential googleCredential(GoogleSheetImporterConfiguration googleSheetImporterConfiguration) throws IOException {
		InputStream in = new FileInputStream(new File(googleSheetImporterConfiguration.getCredentialsFilePath()));
		return GoogleCredential.fromStream(in).createScoped(googleOAuth2Scopes());
	}

	@Bean
	public NetHttpTransport netHttpTransport() throws GeneralSecurityException, IOException {
		return GoogleNetHttpTransport.newTrustedTransport();
	}

	@Bean
	public JacksonFactory jacksonFactory() {
		return JacksonFactory.getDefaultInstance();
	}
}
