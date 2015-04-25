package actors

/**
 * Created by basso on 12/04/15.
 *
 * This package contains:
 *  1. AppAccess factory which returns an AppAccess that lets you access the sytesm
 *  2. The Implementation proxy for AppAccess
 *  3. ApplicationManager which is a parent actor for the system
 *  4. The AppModule trait for the core modules of the system
 */
package object application {
  sealed trait AppState
  object AppSetup extends AppState
  object AppRunning extends AppState
  object AppFinish extends AppState

}
