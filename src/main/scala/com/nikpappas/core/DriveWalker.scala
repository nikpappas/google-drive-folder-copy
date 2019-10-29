package com.nikpappas.core

import java.io.FileNotFoundException
import java.security.GeneralSecurityException

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.nikpappas.file.FileTools

import scala.collection.JavaConverters.asScala
import scala.collection.mutable.Buffer


class DriveWalker(fileTools: FileTools) {

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */




  def main(): Unit = {
//    val name = fileTools.findFile("0ByYdChTWQFFZbmtJdXdWSDFiRkE").getName
//    System.out.println(name)


    //?    val myFolder = "Comparch Build a Computer"
//    val myTEstFolder = "FinalProjectForComparch"
//    val myTEstDestFolder = "anotherCopy"
//    val parentId = "0B-LuKlhU4b55ZTcyallzbjQ0NEE"//findParentId(myFolder)
//    val newParentId = findParentId(myTEstFolder)
//    mycopy(parentId, myFolder+ "Copy", newParentId)
////    val files = findFilesInParent(parentId)
////    printFiles(files)

  }

  def findParentId(dirName: String): String = {
    System.out.println("Fetching folder id: (%s)".format(dirName))

    val service = fileTools.getService()
    // Print the names and IDs for up to 10 files.
    val result = service.files.list
      .setQ(("name = '%s' and trashed = false").format(dirName))
      .setSpaces("drive")
      .setFields("nextPageToken, files(id, name, mimeType, parents)").execute

    val files = asScala(result.getFiles)
    if (files == null || files.isEmpty) {
      System.out.println("No files found.")
    } else {

      System.out.println("%d files found.".format(files.size))
      for (file <- files) {
          System.out.println("Folder name: %s, id: %s    parents: %s".format(file.getName, file.getId, file.getParents()))
        if (file.getMimeType.equals(Runner.FOLDER_MIME_TYPE) && file.getName.equals(dirName)) {
          return file.getId
//          return file.getId
        }
      }
    }
    throw new FileNotFoundException("Resource not found: " + dirName)
  }

  def findFilesInParent(id: String): Buffer[File] = {
    val service = fileTools.getService()

    // Print the names and IDs for up to 10 files.
    val result = service.files.list.setQ("'%s' in parents".format(id)).setPageSize(100)
      .setFields("nextPageToken, files(id, name, mimeType, parents)").execute

    val files = asScala(result.getFiles)
    if (files == null || files.isEmpty) {
      System.out.println("No files found.")
    } else {
      return files
    }
    throw new FileNotFoundException("Resource not found  (id): " + id)
  }



  def copyFolder(service: Drive, originFileId: String, copyTitle: String, destinationParentId: String): Unit = {

    val created = fileTools.createFolder(copyTitle, destinationParentId)
    val filesInParent = findFilesInParent(originFileId)
    fileTools.printFiles(filesInParent)
    for (file <- filesInParent) {
      if (file.getMimeType.equals(Runner.FOLDER_MIME_TYPE)) {
        copyFolder(service, file.getId, file.getName, created)
      } else {
        fileTools.copyFile(service, file.getId, file.getName, created)
      }
    }

  }

  def mycopy(fromDirId: String, name: String, toDirId: String): Unit = {
    System.out.println("Start Copying")
    val service = fileTools.getService()
    copyFolder(service, fromDirId, name, toDirId)
//    for (file <- filesInRoot) {
//      if (file.getMimeType.equals(FOLDER_MIME_TYPE)) {
//        copyFolder(service, file.getId, file.getName, toDirId)
//      } else {
//        copyFile(service, file.getId, file.getName, toDirId)
//      }
//    }

  }

}


//
//public class DriveQuickstart {
//  private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
//  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//  private static final String TOKENS_DIRECTORY_PATH = "tokens";
//
//  /**
//   * Global instance of the scopes required by this quickstart.
//   * If modifying these scopes, delete your previously saved tokens/ folder.
//   */
//  private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
//  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
//
//  /**
//   * Creates an authorized Credential object.
//   * @param HTTP_TRANSPORT The network HTTP Transport.
//   * @return An authorized Credential object.
//   * @throws IOException If the credentials.json file cannot be found.
//   */
//  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//    // Load client secrets.
//    InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//    if (in == null) {
//      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//    }
//    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//    // Build flow and trigger user authorization request.
//    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//      HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//      .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//      .setAccessType("offline")
//      .build();
//    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//  }
//
//  public static void main(String... args) throws IOException, GeneralSecurityException {
//    // Build a new authorized API client service.
//    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//    Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//      .setApplicationName(APPLICATION_NAME)
//      .build();
//
//    // Print the names and IDs for up to 10 files.
//    FileList result = service.files().list()
//      .setPageSize(10)
//      .setFields("nextPageToken, files(id, name)")
//      .execute();
//    List<File> files = result.getFiles();
//    if (files == null || files.isEmpty()) {
//      System.out.println("No files found.");
//    } else {
//      System.out.println("Files:");
//      for (File file : files) {
//        System.out.printf("%s (%s)\n", file.getName(), file.getId());
//      }
//    }
//  }
//}