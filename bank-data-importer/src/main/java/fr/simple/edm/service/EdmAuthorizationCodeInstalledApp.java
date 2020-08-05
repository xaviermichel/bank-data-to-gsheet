package fr.simple.edm.service;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Preconditions;
import fr.simple.edm.GoogleSheetImporterConfiguration;
import org.apache.commons.lang3.StringUtils;

public class EdmAuthorizationCodeInstalledApp extends AuthorizationCodeInstalledApp {

	private final GoogleSheetImporterConfiguration googleSheetImporterConfiguration;

	private static final Logger LOGGER =
			Logger.getLogger(EdmAuthorizationCodeInstalledApp.class.getName());

	public EdmAuthorizationCodeInstalledApp(
			AuthorizationCodeFlow flow, VerificationCodeReceiver receiver, GoogleSheetImporterConfiguration googleSheetImporterConfiguration) {
		super(flow, receiver);
		this.googleSheetImporterConfiguration = googleSheetImporterConfiguration;
	}

	@Override
	protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
		browse(authorizationUrl.build(), googleSheetImporterConfiguration);
	}

	public static void browse(String url, GoogleSheetImporterConfiguration googleSheetImporterConfiguration) {
		Preconditions.checkNotNull(url);

		if (StringUtils.isNotEmpty(googleSheetImporterConfiguration.getOauthDevice())) {
			url = String.format("%s&device_id=%s&device_name=%s", url, googleSheetImporterConfiguration.getOauthDevice(), googleSheetImporterConfiguration.getOauthDevice());
		}

		// Ask user to open in their browser using copy-paste
		System.out.println("Please open the following address in your browser:");
		System.out.println("  " + url);
		// Attempt to open it in the browser
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Action.BROWSE)) {
					System.out.println("Attempting to open that address in the default browser now...");
					desktop.browse(URI.create(url));
				}
			}
		}
		catch (IOException e) {
			LOGGER.log(Level.WARNING, "Unable to open browser", e);
		}
		catch (InternalError e) {
			// A bug in a JRE can cause Desktop.isDesktopSupported() to throw an
			// InternalError rather than returning false. The error reads,
			// "Can't connect to X11 window server using ':0.0' as the value of the
			// DISPLAY variable." The exact error message may vary slightly.
			LOGGER.log(Level.WARNING, "Unable to open browser", e);
		}
	}
}

