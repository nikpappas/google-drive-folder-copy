package nikpappas.file

import java.io.IOException
import java.util.Collections
import java.util.stream.Collectors

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File

import scala.collection.JavaConverters.asScala
import nikpappas.auth.Auth
import nikpappas.core.Runner

import scala.collection.mutable
import scala.collection.mutable.Buffer

class FileTools(applicationName: String, auth: Auth) {

  def inputSelectFromList(scalaFileList: mutable.Buffer[File]): String = {
    var index = 1
    for (f <- scalaFileList) {
      System.out.println("[%d]  (%s): Shared: %s, Owners %s createdTime: %s, parents: %s"
        .format(index, f.getName, f.getShared,
          f.getOwners.stream().map(x => x.getDisplayName).collect(Collectors.joining(", ")),
          f.getCreatedTime,
          f.getParents.stream().map(x => getNameFromId(x)).collect(Collectors.joining(", "))))
      index = index + 1
    }
    System.out.println("Select which folder is the correct one by specifying number:")
    var input = scala.io.StdIn.readLine()
    while(input.length==0){
      input = scala.io.StdIn.readLine()
    }
    scalaFileList(Integer.parseInt(input) - 1).getId

  }

  def getFolderIdByName(name: String): String = {
    val fileList = getService().files().list().setQ("name = '%s'".format(name)).setFields("*").execute().getFiles
    if (fileList.size() == 1) {
      fileList.get(0).getId
    } else {
      val scalaFileList = asScala(fileList)
      inputSelectFromList(scalaFileList)
    }
  }
  @throws[IOException]
  def copyFile(service: Drive, originFileId: String, copyTitle: String, destinationParentId: String): File = {

    System.out.println("Copying file... " + copyTitle)
    val copiedFile = new File()
    copiedFile.setName(copyTitle)
      .setParents(Collections.singletonList(destinationParentId))

    service.files.copy(originFileId, copiedFile).execute
  }
  def createFolder(fodlerName: String, parentId: String):String ={
    System.out.println("Creating folder... " + fodlerName)
    val folder = new File()
    folder.setMimeType(Runner.FOLDER_MIME_TYPE)
      .setName(fodlerName).setParents(Collections.singletonList(parentId))
    getService().files().create(folder).execute().getId
  }

  def printFiles(files: Buffer[File]): Unit = {
    for (file <- files) {
      printFile(file)
    }
  }
  def printFile(file: File): Unit ={
    System.out.printf("(%s) %s -- %s [%s]\n", file.getId, file.getName, file.getMimeType, file.getParents)
  }
  def getService(): Drive = {
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport

    new Drive.Builder(HTTP_TRANSPORT, auth.jsonFactory, auth.getCredentials(HTTP_TRANSPORT)).setApplicationName(applicationName).build()
  }

  def getFilesInFolder(dirId: String): Buffer[File] = {
    val folder = getService().files().get(dirId).execute()
    System.out.println(folder.getSpaces())
    val files = getService().files().list().setQ("'%s' in parents".format(dirId)).setFields("*").execute().getFiles
    asScala(files)
  }

  def getNameFromId(id: String): String = {
    getService().files().get(id).execute().getName
  }

  def compareFile(a: File, b: File): Boolean = {
    System.out.println("File A")
    printFile(a)
    System.out.println("File B")
    printFile(b)
    val fieldGetters = List[File => String](
      (f: File) => f.getName,
      (f: File) => f.getParents.get(0),
      (f: File) => f.getMimeType,
      (f: File) => f.getSize.toString
    )


    for(getter <- fieldGetters){
      if (!compareField(getter, a,b)){
        return false
      }
    }
    true
  }
  def fileListDiff(fs1 :Buffer[File], fs2 :Buffer[File]): Set[String] = {
    val fileNames1= fs1.map(x => x.getName).toSet
    val fileNames2= fs2.map(x => x.getName).toSet
    val diff = fileNames1.diff(fileNames2)
    println(diff)
    diff
  }

  def compareField(fieldGetter: (File) => String, a: File, b: File): Boolean = {
    fieldGetter(a).equals(fieldGetter(b))
  }
}
