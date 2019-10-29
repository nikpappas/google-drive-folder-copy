package com.nikpappas.core

import java.util
import java.util.Collections

import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.nikpappas.auth.Auth
import com.nikpappas.file.FileTools

object FolderDiff {
    private val SCOPES = util.Arrays.asList(DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE)
    private val CREDENTIALS_FILE_PATH = "/credentials.json"
    val APPLICATION_NAME = "Google Drive Diffing Tool"

    val fileTools = new FileTools(APPLICATION_NAME, new Auth(CREDENTIALS_FILE_PATH, SCOPES))


    def compareFolders(dirId1: String, dirId2: String, dryRun: Boolean): Boolean = {
        System.out.println("Cοmparing folders %s VS %s".format(fileTools.getNameFromId(dirId1), fileTools.getNameFromId(dirId2)))
        val files1 = fileTools.getFilesInFolder(dirId1)
        val orderFunction = (f: File) => f.getName
        val files1Sorted = files1.sortBy(orderFunction)
        fileTools.printFiles(files1Sorted)
        val files2 = fileTools.getFilesInFolder(dirId2)
        val files2Sorted = files2.sortBy(orderFunction)
        fileTools.printFiles(files2Sorted)
        var foldersOk = true
        if (files1Sorted.size == files2Sorted.size) {
            for (i <- Range(0, files1.size)) {
                val file1 = files1Sorted(i)
                val file2 = files2Sorted(i)
                if (fileTools.compareFile(file1, file2)) {
                    println("Files are not the same")
                    return false
                }
                if (file1.getMimeType.equals(Runner.FOLDER_MIME_TYPE)) {
                    foldersOk = foldersOk && compareFolders(file1.getId, file2.getId, false)
                }
            }
            if (!foldersOk) {
                return false
            }
            println("Files are all the same")
            return true
        } else {
            println("Files are un equal %d in the first folder and %d in the second".format(files1Sorted.size, files2Sorted.size))
            val missingFiles = fileTools.fileListDiff(files1Sorted, files2Sorted)
            if (!dryRun) {
                fileTools.getFilesInFolder(dirId1)
                    .filter(x => missingFiles.contains(x.getName) && x.getMimeType != Runner.FOLDER_MIME_TYPE)
                    .foreach(f => {
                        val copiedFile = new File()
                        copiedFile.setName(f.getName)
                            .setParents(Collections.singletonList(dirId2))
                        System.out.println("Copying file %s %s %s".format(f.getName, f.getId, f.getMimeType))

                        fileTools.getService().files().copy(f.getId, copiedFile).execute()
                    })
                fileTools.getFilesInFolder(dirId1)
                    .filter(x => missingFiles.contains(x.getName) && x.getMimeType == Runner.FOLDER_MIME_TYPE)
                    .foreach(f => {
                        fileTools.createFolder(f.getName, dirId2)
                    })

            }
            return false
        }
        false
    }

    def main(args: Array[String]): Unit = {
        if (args.length < 2) {
            println("Usage ./folderDiff <1st dir name> <2nd dir name>")
            return
        }
        val folderId1 = fileTools.getFolderIdByName(args(0))
        val folderId2 = fileTools.getFolderIdByName(args(1))
        if (compareFolders(folderId1, folderId2, false)) {
            println("The folders are completely the same")
        } else {
            println("Some Problem was found")
        }

    }


}
