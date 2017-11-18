# webappquickstart


## Build
```
mvn clean install -DskipTests
```

## Start services
* ConfigServer
* RegistryServer
* OAuth2Server
* AuthCodeApplication
* CreditCardTransaction

## Test
### Access Resource using OAuth2 token
1. Use grant type = password
   * curl "http://anyclient:clientsecret@localhost:9999/uaa/oauth/token" -d "password=user_abc&username=user_abc&grant_type=password&scope=openid"
   * curl -H "Authorization: Bearer <access_token>" "http://localhost:6000/authcode/merchantId"
2. use grant type = authorization_code
   * In browser go to http://localhost:9999/uaa/oauth/authorize?response_type=code&client_id=anyclient&redirect_uri=http://stupidurl
   * Fill user_abc/user_abc to login
   * Get "code" param from redirected page
   * curl "localhost:9999/uaa/oauth/token" -d "grant_type=authorization_code&client_id=anyclient&client_secret=clientsecret&redirect_uri=http://stupidurl&code=[code]"
   * curl -H "Authorization: Bearer <access_token>" "http://localhost:6000/authcode/merchantId"

### Microservices call using client_credentials
Microservices automatically sign in OAuth2 server and authenticate using client_credentials
```
curl "localhost:7000/transactions/any"
```
3. Via API Gateway
  * In browser go to http://localhost:4000/authcode-service/authcode/long