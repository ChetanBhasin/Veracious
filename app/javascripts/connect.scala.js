@(implicit request: RequestHeader)
// This is the production level connection string
@connection(routes.Application.connect.webSocketURL())
