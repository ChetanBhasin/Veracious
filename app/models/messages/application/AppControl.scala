package models.messages.application

import models.messages.batchProcessing.BatchProcessorMessage
import models.messages.client.ClientManagerMessage

/**
 * Created by basso on 24/04/15.
 */
trait AppControl
object AppShutDown extends AppControl

/** This one is sent to BatchProcessor and ClientManager */
object FinishWork extends BatchProcessorMessage with ClientManagerMessage
