@(implicit request: RequestHeader)
// This is the testing connection String
@connection(routes.TestController.connect.webSocketURL(), routes.TestController.submitBatch.toString, routes.TestController.getResult.toString)
