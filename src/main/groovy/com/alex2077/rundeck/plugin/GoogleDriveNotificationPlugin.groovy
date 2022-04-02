package org.rundeck.plugin

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

/**
 * Created by Alex2077.
 */
@Plugin(service=ServiceNameConstants.Notification, name=GoogleDriveNotificationPlugin.SERVICE_NAME)
@PluginDescription(title=GoogleDriveNotificationPlugin.SERVICE_TITLE, description=GoogleDriveNotificationPlugin.SERVICE_DESCRIPTION)
class GoogleDriveNotificationPlugin implements NotificationPlugin {

    public static final String SERVICE_NAME        = "GoogleDriveNotificationPlugin"
    public static final String SERVICE_TITLE       = "Google Drive Notification"
    public static final String SERVICE_DESCRIPTION = "Upload a file to Google Drive"

    static final JsonFactory  JSON_FACTORY = GsonFactory.getDefaultInstance()
    static final List<String> SCOPES       = Collections.singletonList(DriveScopes.DRIVE)

    static final String CREDENTIALS_FILE_PATH = "credentialsFilePath"
    static final String FOLDER_ID             = "folderId"
    static final String FILE_TO_UPLOAD        = "fileToUpload"

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

    /*static Description DESCRIPTION = DescriptionBuilder.builder()
            .name(SERVICE_NAME)
            .title(SERVICE_TITLE)
            .description(SERVICE_DESCRIPTION)
            .property(PropertyBuilder.builder()
                    .string(CREDENTIALS_FILE_PATH)
                    .title("Credentials file path")
                    .description("Credentials file path.")
                    .required(true)
                    .build())
            .property(PropertyBuilder.builder()
                    .string(FOLDER_ID)
                    .title("Folder ID")
                    .description("Folder ID on Google Drive.")
                    .required(true)
                    .build())
            .property(PropertyBuilder.builder()
                    .string(FILE_TO_UPLOAD)
                    .title("File to upload")
                    .description("File to be uploaded to Google Drive.")
                    .required(true)
                    .build())
            .build()*/

    @Override
    boolean postNotification(String trigger, Map executionData, Map config) {

        //String credentialsFilePath = config.containsKey(CREDENTIALS_FILE_PATH) ? config.get(CREDENTIALS_FILE_PATH).toString() : null
        //String folderId = config.containsKey(FOLDER_ID) ? config.get(FOLDER_ID).toString() : null
        //String fileToUpload = config.containsKey(FILE_TO_UPLOAD) ? config.get(FILE_TO_UPLOAD).toString() : null

        boolean res = false

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        Credential credentials = getCredentials(HTTP_TRANSPORT, credentialsFilePath)
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(SERVICE_NAME)
                .build()

        java.io.File fileContent = new java.io.File(fileToUpload);
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

    private Credential getCredentials(final NetHttpTransport httpTransport, String credentialsPath) throws IOException  {

        InputStream is = new FileInputStream(new java.io.File(credentialsPath))
        if (!is) {
            throw new FileNotFoundException("Resource not found: " + credentialsFilePath)
        }

        return GoogleCredential.fromStream(is).createScoped(SCOPES)
    }
}
