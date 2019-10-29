package nikpappas.scripts

import java.io.{FileNotFoundException, IOException, InputStreamReader}
import java.security.GeneralSecurityException
import java.util
import java.util.Collections

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.script.Script
import com.google.api.services.script.model.{Content, CreateProjectRequest, File}

object ScriptBuilder {
  private val APPLICATION_NAME = "Apps Script API Java Quickstart"
  private val JSON_FACTORY = JacksonFactory.getDefaultInstance
  private val TOKENS_DIRECTORY_PATH = "tokens"
  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved credentials folder at /secret.
   */
  private val SCOPES = Collections.singletonList("https://www.googleapis.com/auth/script.projects")
  private val CREDENTIALS_FILE_PATH = "/credentials.json"

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  @throws[IOException]
  private def getCredentials(HTTP_TRANSPORT: NetHttpTransport) = { // Load client secrets.
    val in = this.getClass().getResourceAsStream(CREDENTIALS_FILE_PATH)
    if (in == null) throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))
    // Build flow and trigger user authorization request.
    val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build
    val receiver = new LocalServerReceiver.Builder().setPort(8888).build
    new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
  }

  @throws[IOException]
  @throws[GeneralSecurityException]
  def main(args: Array[String]): Unit = { // Build a new authorized API client service.
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
    val service = new Script.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build
    val projects = service.projects
    // Creates a new script project.
    val createOp = projects.create(new CreateProjectRequest().setTitle("My Script")).execute
    // Uploads two files to the project.
    val file1 = new File().setName("hello").setType("SERVER_JS").setSource("function helloWorld() {\n  console.log(\"Hello, world!\");\n}")
    val file2 = new File().setName("appsscript").setType("JSON").setSource("{\"timeZone\":\"America/New_York\",\"exceptionLogging\":\"CLOUD\"}")
    val content = new Content().setFiles(util.Arrays.asList(file1, file2))
    val updatedContent = projects.updateContent(createOp.getScriptId, content).execute
    // Logs the project URL.
    System.out.printf("https://script.google.com/d/%s/edit\n", updatedContent.getScriptId)
  }
}
