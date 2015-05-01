@(implicit request: RequestHeader)
// This is the testing connection String
@connection(routes.TestController.connect.webSocketURL())
