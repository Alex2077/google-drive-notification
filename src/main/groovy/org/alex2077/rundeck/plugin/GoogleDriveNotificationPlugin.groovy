package org.alex2077.rundeck.plugin

import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import groovy.util.logging.Slf4j

import java.nio.file.Files

/**
 * Created by Alex2077.
 */
@Slf4j
@Plugin(service=ServiceNameConstants.Notification, name=GoogleDriveNotificationPlugin.SERVICE_NAME)
@PluginDescription(title=GoogleDriveNotificationPlugin.SERVICE_TITLE, description=GoogleDriveNotificationPlugin.SERVICE_DESCRIPTION)
class GoogleDriveNotificationPlugin implements NotificationPlugin {

    public static final String SERVICE_NAME        = "GoogleDriveNotificationPlugin"
    public static final String SERVICE_TITLE       = "Google Drive Notification"
    public static final String SERVICE_DESCRIPTION = "Upload a file to Google Drive"

    static final JsonFactory  JSON_FACTORY = GsonFactory.getDefaultInstance()
    static final List<String> SCOPES       = Collections.singletonList(DriveScopes.DRIVE)

    @PluginProperty(
            title = "Credentials file path",
            description = "Credentials file path.",
            required = true
    )
    String credentialsFilePath

    @PluginProperty(
            title = "Folder ID",
            description = "Folder ID on Google Drive.",
            required = true
    )
    String folderId

    @PluginProperty(
            title = "File to upload",
            description = "File to be uploaded to Google Drive.",
            required = true
    )
    String fileToUpload

    @Override
    boolean postNotification(String trigger, Map executionData, Map config) {

        boolean res = false

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        Credential credentials = getCredentials(credentialsFilePath)
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(SERVICE_NAME)
                .build()

        java.io.File fileContent = new java.io.File(fileToUpload);

        log.debug("File to upload: ${fileToUpload}")
        log.debug("File exists: ${fileContent.exists()}")
        log.debug("File can read: ${fileContent.canRead()}")
        log.debug("File si readable: "+ Files.isReadable(Paths.get("fileToUpload")))
        log.debug("File exists (Files): "+ Files.exists(fileContent))

        /*if (!fileContent.exists()) {
            throw new FileNotFoundException("Resource not found: " + fileToUpload)
        }*/
        String fileName = fileContent.getName();

        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setDescription(fileName);
        fileMetadata.setMimeType("text/plain");
        fileMetadata.setParents(Collections.singletonList(folderId));
        FileContent mediaContent = new FileContent("text/plain", fileContent);
        File file = service.files().create(fileMetadata, mediaContent)
                .execute();

        if (file.getId()) {
            res = true
        }

        return res
    }

    private Credential getCredentials(final String credentialsPath) throws IOException  {

        InputStream is = new FileInputStream(new java.io.File(credentialsPath))
        if (!is) {
            throw new FileNotFoundException("Resource not found: " + credentialsFilePath)
        }

        return GoogleCredential.fromStream(is).createScoped(SCOPES)
    }
}
