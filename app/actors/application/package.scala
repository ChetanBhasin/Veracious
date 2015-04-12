package actors

/**
 * Created by basso on 12/04/15.
 */
package object application {
  trait AppState
  object AppSetup extends AppState
  object AppRunning extends AppState
  object AppFinish extends AppState
}
