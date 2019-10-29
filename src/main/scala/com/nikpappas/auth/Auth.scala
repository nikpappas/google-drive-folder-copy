package com.nikpappas.auth

import java.io.{FileNotFoundException, IOException, InputStreamReader}

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.DriveScopes

class Auth(credentialsFilePath: String, scopes: java.util.List[java.lang.String]) {
  private final val TOKENS_DIRECTORY_PATH = "tokens"
  final val jsonFactory = JacksonFactory.getDefaultInstance

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  @throws[IOException]
  def getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential = { // Load client secrets.
    val in = this.getClass().getResourceAsStream(credentialsFilePath)
    if (in == null) throw new FileNotFoundException("Resource not found: " + credentialsFilePath)
    val clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in))
    // Build flow and trigger user authorization request.
    val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, jsonFactory, clientSecrets, scopes).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build
    val receiver = new LocalServerReceiver.Builder().setPort(8888).build
    new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
  }
}
