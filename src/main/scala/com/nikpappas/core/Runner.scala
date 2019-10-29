package com.nikpappas.core

import java.util

import com.google.api.services.drive.DriveScopes
import com.nikpappas.auth.Auth
import com.nikpappas.file.FileTools

object Runner {
  final val FOLDER_MIME_TYPE: String = "application/vnd.google-apps.folder"

  private val SCOPES = util.Arrays.asList(DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE)
  private val CREDENTIALS_FILE_PATH = "/credentials.json"
  val APPLICATION_NAME = "Google Drive API Java Quickstart"


  def main(args: Array[String]): Unit = {

    val driveWalker = new DriveWalker(new FileTools(APPLICATION_NAME,new Auth(CREDENTIALS_FILE_PATH,SCOPES)))
    driveWalker.main()
  }
}
